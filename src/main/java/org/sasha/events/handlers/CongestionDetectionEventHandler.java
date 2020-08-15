package org.sasha.events.handlers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleDepartsAtFacilityEventHandler;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.utils.charts.XYLineChart;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.vehicles.Vehicle;
import org.sasha.routers.reservation.SimpleReservationRoutingModule;
import scala.Int;

/**
 * This EventHandler implementation counts the travel time of all agents
 * and provides the average travel time per agent.
 * Actually, handling Departures and Arrivals should be sufficient for this (may 2014)
 * @author dgrether
 * @author sasha / HubbleCommand for the writeChart, handling values for each iteration and average per plan
 *
 * To register events: addEventHandlerBinding().toInstance( new CongestionDetectionEventHandler( scenario.getNetwork() )  )
 * To register controller events: addControlerListenerBinding().toInstance(new CongestionDetectionEventHandler(scenario.getNetwork()));
 *
 * FIXME doesn't do anything for iteration 0
 */

@Deprecated //Fixme I don't think this works unfortunately
public class CongestionDetectionEventHandler implements
        LinkEnterEventHandler,
        LinkLeaveEventHandler,
        PersonArrivalEventHandler,
        PersonDepartureEventHandler,
        IterationEndsListener,
        VehicleEntersTrafficEventHandler,
        VehicleDepartsAtFacilityEventHandler {
    private static final Logger logger = Logger.getLogger(CongestionDetectionEventHandler.class);
    private ArrayList<Double> iterationCongestion = new ArrayList<>();
    private ArrayList<Integer> iterationPlans = new ArrayList<>();
    private Map<Id<Vehicle>, Id<Person>> vehicleToPerson = new HashMap<>();//Stores which person is using the vehicle.
    private Map<Id<Vehicle>, ArrayList<ArrayList<CongElemData>>> congestionTripsNLinks = new HashMap<>();//Stores, per vehicle, the trips, and in trips each link travelled
    private Network network;
    private double congestionTime = 0;                                          //Congestion for the current iteration
    private int numberOfDepartureEvents = 0;
    private Map<Id<Vehicle>,Double> earliestLinkExitTime = new HashMap<>() ;
    private String filenameTotal;
    private String filenameAverage;
    private String outputDirectory;

    private class CongElemData{
        //Double is used instead of double to allow for null values
        public CongElemData(Id<Link> linkId, Double timeEntered, Double timeExited, Double minTravelTime, Double excessTravelTime, Double length){
            this.linkId = linkId;
            this.timeEntered = timeEntered;
            this.timeExited = timeExited;
            this.minTravelTime = minTravelTime;
            this.excessTravelTime = excessTravelTime;
            this.length = length;
        }
        public Id<Link> linkId;
        public Double timeEntered;
        public Double timeExited;
        public Double minTravelTime;
        public Double excessTravelTime;
        public Double length;
    }

    public CongestionDetectionEventHandler( Network network, String outputDirectory ) {
        this.network = network ;
        this.filenameTotal = "totalCongestion.png";
        this.filenameAverage = "averageCongestion.png";
        this.outputDirectory = outputDirectory;
    }

    public CongestionDetectionEventHandler( Network network, String filenameTotal, String filenameAverage, String outputDirectory ) {
        this.network = network ;
        this.filenameTotal = filenameTotal;
        this.filenameAverage = filenameAverage;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        logger.warn("Iteration ends for congestion detector... NOTITEND\n");
        System.out.println("Iteration ends for congestion detector... NOTITEND\n");
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {

        Link link = network.getLinks().get( event.getLinkId() ) ;
        double linkTravelTime = link.getLength() / link.getFreespeed( event.getTime() );
        this.earliestLinkExitTime.put( event.getVehicleId(), event.getTime() + linkTravelTime ) ;

        //Need to add new link element to existing trip
        CongElemData congElemData = new CongElemData(event.getLinkId(), event.getTime(), null, event.getTime(), null, network.getLinks().get(event.getLinkId()).getLength());

        ArrayList<ArrayList<CongElemData>> tripsData = this.congestionTripsNLinks.get(event.getVehicleId());
        tripsData.get(tripsData.size() - 1).add(congElemData);

        //Rebuild
        this.congestionTripsNLinks.put(event.getVehicleId(), tripsData);
    }

    @Override
    public void handleEvent(LinkLeaveEvent event) {
        double excessTravelTime = event.getTime() - this.earliestLinkExitTime.get( event.getVehicleId() ) ;
        congestionTime += excessTravelTime;

        //System.out.println( "excess travel time: " + excessTravelTime ) ;

        //Need to update existing nested CongElemData, last link
        ArrayList<ArrayList<CongElemData>> tripsData = this.congestionTripsNLinks.get(event.getVehicleId());

        //Get last trip
        ArrayList<CongElemData> trip = tripsData.get(tripsData.size() - 1);

        //Get last link of trip
        /*CongElemData congElemData = trip.get(trip.size() - 1);
        congElemData.timeExited = event.getTime();
        congElemData.excessTravelTime = excessTravelTime; // = congElemData.timeExited - congElemData.timeEntered - congElemData.minTravelTime
        */
        //Can just do this, object will be updated!
        trip.get(trip.size() - 1).timeExited = event.getTime();
        trip.get(trip.size() - 1).excessTravelTime = excessTravelTime;

        //Rebuild tables
        tripsData.set(tripsData.size() - 1, trip);
        this.congestionTripsNLinks.put(event.getVehicleId(), tripsData);
    }

    @Override
    public void handleEvent(VehicleDepartsAtFacilityEvent event) {

    }

    @Override
    public void handleEvent(VehicleEntersTrafficEvent event) {
        //event.
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        //TODO move this to one of the above event handlers, as this one doesn't use
        // the correct vehicle ID!
        Id<Vehicle> vehId = Id.create( event.getPersonId(), Vehicle.class ) ; // unfortunately necessary since vehicle departures are not uniformly registered
        this.earliestLinkExitTime.put( vehId, event.getTime() ) ;
        this.numberOfDepartureEvents += 1;

        //Assumption, a vehicle is always used by the same person!
        vehicleToPerson.put(vehId, event.getPersonId());

        //Need to add new trip
        CongElemData congElemData = new CongElemData(event.getLinkId(), event.getTime(), null, event.getTime(), null, null);

        if(this.congestionTripsNLinks.containsKey(vehId)){
            ArrayList<ArrayList<CongElemData>> data = new ArrayList<>(this.congestionTripsNLinks.get(vehId));
            data.add(new ArrayList<>(Arrays.asList(congElemData)));
            this.congestionTripsNLinks.put(vehId, data);
        } else {
            ArrayList<ArrayList<CongElemData>> data = new ArrayList<>();
            data.add(new ArrayList<>(Arrays.asList(congElemData)));
            this.congestionTripsNLinks.put(vehId, data);
        }
        //System.out.println("Person: " + event.getPersonId() + " left in vehicle : " + vehId);
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {
        //Don't think there's anything to do here...
    }

    @Override
    public void reset(int iteration) {
        logger.warn("Iteration ends for congestion detector...");
        logger.warn("Total congestion for this iteration (" + iteration + "): " + congestionTime + "\n");
        logger.warn("Total number of plans for iteration (" + iteration + "): " + numberOfDepartureEvents + "\n");
        logger.warn("Average congestion per plan : " + congestionTime / numberOfDepartureEvents + "\n");
        //iterationCongestion.add(congestionTime);
        //iterationPlans.add(numberOfDepartureEvents);
        //this.iterationCongestion.put(iteration, congestionTime);
        //this.iterationPlans.put(iteration, numberOfDepartureEvents);
        this.congestionTime = 0;
        this.numberOfDepartureEvents = 0;
        this.earliestLinkExitTime.clear();
        //logger.warn("Total congestion for this iteration (" + iteration + "): " + congestionTime + "");
        /*iterationCongestion.add(congestionTime);
        iterationPlans.add(numberOfDepartureEvents);
        //this.iterationCongestion.put(iteration, congestionTime);
        //this.iterationPlans.put(iteration, numberOfDepartureEvents);
        this.congestionTime = 0;
        this.numberOfDepartureEvents = 0;
        this.earliestLinkExitTime.clear();*/

        writeIterationResults(iteration);

        congestionTripsNLinks.clear();
    }

    private class TripData {
        public int trip_number;
        public Id<Vehicle> vehicle_id;
        public Id<Person> person_id;
        public Double start_time;
        public Double end_time;
        public Double congested_time;
        public Double trip_distance;

        TripData(int trip_number, Id<Vehicle> vehicle_id, Id<Person> person_id, Double start_time, Double end_time, Double congested_time, Double trip_distance){
            this.trip_number = trip_number;
            this.vehicle_id = vehicle_id;
            this.person_id = person_id;
            this.start_time = start_time;
            this.end_time = end_time;
            this.congested_time = congested_time;
            this.trip_distance = trip_distance;
        }
    }

    public void writeIterationResults(int iteration){
        //Write trip congestion results for this iteration
        BufferedWriter congestionWriter = IOUtils.getBufferedWriter(this.outputDirectory + "/ITERS/it." + iteration + "/congestion.csv");

        try{
            congestionWriter.write("DO NOT TRUST DISTANCE HERE, SEEMS TO BE WRONG\n");
            congestionWriter.write("trip_number\tvehicle_id\tperson_id\tstart_time\tend_time\tcongested_time\ttrip_distance\n");
            //FIXME this doesn't output the right value, although the correct number of plans is written
            // congestionWriter.write("Number of trips to write : " + this.congestionTripsNLinks.size() + "\n");
            //congestionWriter.write(vehicleToPerson.toString());

            int totalCongestion = 0;
            int numberOfTrips = 0;
            ArrayList<TripData> tripData = new ArrayList<>();

            for(Map.Entry<Id<Vehicle>, ArrayList<ArrayList<CongElemData>>> entry : this.congestionTripsNLinks.entrySet()){
                ArrayList<ArrayList<CongElemData>> trips = entry.getValue();    //Trips
                for(ArrayList<CongElemData> trip : trips){
                    numberOfTrips += 1;

                    double congestionForThisTrip = 0;
                    double endTime = 0;
                    double tripDistance = 0;
                    //congestionForThisTrip = n.stream().mapToDouble(i -> i.excessTravelTime).sum();
                    for(CongElemData linkData : trip){
                        Double congestedTime = linkData.excessTravelTime;
                        if(congestedTime != null) {
                            if (!congestedTime.isNaN()) {
                                congestionForThisTrip += linkData.excessTravelTime;
                            }
                        }

                        Double endTimeLoc = linkData.timeExited;
                        if(endTimeLoc != null) {
                            if (!endTimeLoc.isNaN()) {
                                endTime = endTimeLoc;
                            }
                        }

                        if(linkData.length != null){
                            tripDistance += linkData.length;
                        }
                    }
                    totalCongestion += congestionForThisTrip;

                    double startTime = trip.get(0).timeEntered;

                    /*congestionWriter.write(String.format(
                            "%s\t%s\t%s\t%s\t%s\t%s\n",
                            numberOfTrips, entry.getKey(), vehicleToPerson.get(entry.getKey()), startTime, endTime, congestionForThisTrip, tripDistance
                    ));*/

                    //Collect trip data
                    tripData.add(new TripData(
                            numberOfTrips, entry.getKey(), vehicleToPerson.get(entry.getKey()), startTime, endTime, congestionForThisTrip, tripDistance));

                    /*if(congestionForThisTrip / (endTime - startTime) > 0.5){
                        throw new RuntimeException("HOW IS HALF OF THE TIME SPENT IN CONGESTION LOOK INTO");
                    }*/
                }
            }

            //A first comparator, sorts be trip distance
            //https://www.codebyamir.com/blog/sort-list-of-objects-by-field-java
            /*Collections.sort(tripData, new Comparator<TripData>() {
                @Override
                public int compare(TripData o1, TripData o2) {
                    return (int)(o1.trip_distance - o2.trip_distance);
                }
            });*/

            Map<Integer, Double> tripCongestionFraction = new HashMap<>();
            Map<Integer, Double> tripDurations = new HashMap<>();
            Map<Integer, Double> tripCongestion = new HashMap<>();
            Map<Integer, Double> tripDistances = new HashMap<>();

            for(int q = 0; q < tripData.size(); q++){
                TripData trip = tripData.get(q);
                congestionWriter.write(String.format(
                        "%s,%s,%s,%s,%s,%s,%s\n",
                        trip.trip_number, trip.vehicle_id, trip.person_id, trip.start_time, trip.end_time, trip.congested_time, trip.trip_distance
                ));

                tripDurations.put(q, trip.end_time - trip.start_time);
                tripCongestion.put(q,trip.congested_time);
                tripDistances.put(q, trip.trip_distance);
                tripCongestionFraction.put(q, trip.congested_time / (trip.end_time - trip.start_time));
            }

            //Write charts
            XYLineChart chartsAll = new XYLineChart("Trip Data","Trip","Trip Number");
            chartsAll.addSeries("Durations", tripDurations);
            chartsAll.addSeries("Congestion", tripCongestion);
            chartsAll.addSeries("Distance", tripDistances);
            chartsAll.saveAsPng(this.outputDirectory + "/ITERS/it." + iteration + "/statsMeAll.png", 1920, 1080);

            XYLineChart chartsCongDur = new XYLineChart("Trip Duration","Trip","Time (seconds)");
            chartsCongDur.addSeries("Congestion", tripCongestion);
            chartsCongDur.addSeries("Duration of trip", tripDurations);
            chartsCongDur.saveAsPng(this.outputDirectory + "/ITERS/it." + iteration + "/statsConDur.png", 1920, 1080);

            XYLineChart chartsCong = new XYLineChart("Trip Congestion","Trip","Time (seconds)");
            chartsCong.addSeries("Congestion", tripCongestion);
            chartsCong.addSeries("Fraction of congestion", tripCongestionFraction);
            chartsCong.saveAsPng(this.outputDirectory + "/ITERS/it." + iteration + "/statsCong.png", 1920, 1080);

            XYLineChart chartsCongFrac = new XYLineChart("Trip Congestion","Trip","Time (seconds)");
            chartsCongFrac.addSeries("Fraction of congestion", tripCongestionFraction);
            chartsCongFrac.saveAsPng(this.outputDirectory + "/ITERS/it." + iteration + "/statsCongFract.png", 1920, 1080);

            XYLineChart chartsDist = new XYLineChart("Trip Distance","Trip","Distance (meters)");
            chartsDist.addSeries("Distance of trip", tripDistances);
            chartsDist.saveAsPng(this.outputDirectory + "/ITERS/it." + iteration + "/statsDist.png", 1920, 1080);

            //Write average congestion for iteration
            congestionWriter.write("Number of trips : " + numberOfTrips + "\n");
            congestionWriter.write("Total time spent in congestion : " + totalCongestion + "\n");
            if(numberOfTrips != 0){
                congestionWriter.write("Average congestion per trip : " + (totalCongestion / numberOfTrips) + "\n");
                //double totalTimeA = tripDurations.values().stream().reduce(0.0, (a, b) -> a + b);
                //double totalCongestionA = tripCongestion.values()
                double totalFractionCongestion = tripCongestionFraction.values().stream().reduce(0.0, (a, b) -> a + b);
                congestionWriter.write("Average congestion fraction per trip : " + (totalFractionCongestion / tripCongestionFraction.size()) + "\n");
            }

            congestionWriter.close();
        } catch (IOException ioException){
            System.out.println("Could not write congestion data!");
        }
    }

    public void writeCharts(String filenameTotal, String filenameAverage){
        logger.warn("Writing charts..." + "\n");
        /*if(filenameAverage.equals(filenameTotal)){  //Make sure file names aren't identical to avoid overwritting
            filenameAverage = filenameAverage + "2";
        }*/
        logger.warn("Writing charts..." + "\n");
        logger.warn("Number of iterations to write: " + iterationCongestion.size() + "\n");
        double[] iterationNumbers = new double[iterationCongestion.size()];
        double[] timeCongested = new double[iterationCongestion.size()];
        double[] averageTimeCongested = new double[iterationCongestion.size()];

        for (int i = 0; i < iterationCongestion.size(); i++){
            iterationNumbers[i] = i;
            logger.warn("Congestion this iteration : " + iterationCongestion.get(i).toString() + "\n");
            timeCongested[i] = iterationCongestion.get(i);

            averageTimeCongested[i] = iterationCongestion.get(i) / iterationPlans.get(i);
        }

        XYLineChart chartTotal = new XYLineChart("Total Congestion", "iteration", "total congestion");
        chartTotal.addSeries("time", iterationNumbers, timeCongested);  //title, iteration number, average congestion per trip
        chartTotal.saveAsPng(filenameTotal, 800, 600);

        XYLineChart chartAverage = new XYLineChart("Average Congestion per Trip", "iteration", "average congestion");
        chartAverage.addSeries("time", iterationNumbers, averageTimeCongested);  //title, iteration number, average congestion per trip
        chartAverage.saveAsPng(filenameAverage, 800, 600);
        logger.warn("Made charts...\n");
    }
}
