package org.sasha.events.handlers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
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
 * This EventHandler implementation counts the travel time of
 * all agents and provides the average travel time per
 * agent.
 * Actually, handling Departures and Arrivals should be sufficient for this (may 2014)
 * @author dgrether
 * @author sasha / HubbleCommand for the writeChart, handling values for each iteration and average per plan
 *
 * To register events: addEventHandlerBinding().toInstance( new CongestionDetectionEventHandler( scenario.getNetwork() )  )
 * To register controller events: addControlerListenerBinding().toInstance(new CongestionDetectionEventHandler(scenario.getNetwork()));
 * TODO: be able to write iteration data to charts (need data class intermediary)
 */

public class CongestionDetectionEventHandler implements
        LinkEnterEventHandler, LinkLeaveEventHandler, PersonArrivalEventHandler, PersonDepartureEventHandler, IterationEndsListener {
    private static final Logger logger = Logger.getLogger(CongestionDetectionEventHandler.class);
    //private Map<Integer, Double> iterationCongestion = new HashMap<>();         //HashMap to store the total time spent in congestion
    //private Map<Integer, Integer> iterationPlans = new HashMap<>();             //HashMap to store number of plans per iteration, instead of getting all plans from controller or scenario or whatever

    private ArrayList<Double> iterationCongestion = new ArrayList<>();
    private ArrayList<Integer> iterationPlans = new ArrayList<>();

    private ArrayList<Double> congestionTimes = new ArrayList<>();

    //Stores which person is using the vehicle.
    private Map<Id<Vehicle>, Id<Person>> vehicleToPerson = new HashMap<>();

    //Stores, per vehicle, the trips, and in trips each link travelled
    private Map<Id<Vehicle>, ArrayList<ArrayList<CongElemData>>> congestionTripsNLinks = new HashMap<>();

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        logger.warn("Iteration ends for congestion detector... NOTITEND");
        System.out.println("Iteration ends for congestion detector... NOTITEND");
    }

    private class CongElemData{
        //Double is used instead of double to allow for null values
        public CongElemData(Id<Link> linkId, Double timeEntered, Double timeExited, Double minTravelTime, Double excessTravelTime){
            this.linkId = linkId;
            this.timeEntered = timeEntered;
            this.timeExited = timeExited;
            this.minTravelTime = minTravelTime;
            this.excessTravelTime = excessTravelTime;
        }
        public Id<Link> linkId;
        public Double timeEntered;
        public Double timeExited;
        public Double minTravelTime;
        public Double excessTravelTime;
    }

    private double congestionTime = 0;                                          //Congestion for the current iteration
    private int numberOfDepartureEvents = 0;
    private Map<Id<Vehicle>,Double> earliestLinkExitTime = new HashMap<>() ;
    private Network network;
    private String filenameTotal;
    private String filenameAverage;
    private String outputDirectory;

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
    public void handleEvent(LinkEnterEvent event) {

        Link link = network.getLinks().get( event.getLinkId() ) ;
        double linkTravelTime = link.getLength() / link.getFreespeed( event.getTime() );
        this.earliestLinkExitTime.put( event.getVehicleId(), event.getTime() + linkTravelTime ) ;

        //Need to add new link element to existing trip
        CongElemData congElemData = new CongElemData(event.getLinkId(), event.getTime(), null, event.getTime(), null);

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
    public void handleEvent(PersonDepartureEvent event) {
        Id<Vehicle> vehId = Id.create( event.getPersonId(), Vehicle.class ) ; // unfortunately necessary since vehicle departures are not uniformly registered
        this.earliestLinkExitTime.put( vehId, event.getTime() ) ;
        this.numberOfDepartureEvents += 1;

        //Assumption, a vehicle is always used by the same person!
        vehicleToPerson.put(vehId, event.getPersonId());

        //Need to add new trip
        CongElemData congElemData = new CongElemData(event.getLinkId(), event.getTime(), null, event.getTime(), null);

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
        logger.warn("Total congestion for this iteration (" + iteration + "): " + congestionTime);
        logger.warn("Total number of plans for iteration (" + iteration + "): " + numberOfDepartureEvents);
        logger.warn("Average congestion per plan : " + congestionTime / numberOfDepartureEvents);
        //iterationCongestion.add(congestionTime);
        //iterationPlans.add(numberOfDepartureEvents);
        //this.iterationCongestion.put(iteration, congestionTime);
        //this.iterationPlans.put(iteration, numberOfDepartureEvents);
        this.congestionTime = 0;
        this.numberOfDepartureEvents = 0;
        this.earliestLinkExitTime.clear();
        //logger.warn("Total congestion for this iteration (" + iteration + "): " + congestionTime);
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

    public void writeIterationResults(int iteration){
        //Write trip congestion results for this iteration
        BufferedWriter congestionWriter = IOUtils.getBufferedWriter(this.outputDirectory + "/ITERS/it." + iteration + "/congestion.txt");

        try{
            congestionWriter.write("trip_number\tvehicle_id\tperson_id\tstart_time\tend_time\tcongested_time");
            congestionWriter.write("Number of trips to write : " + this.congestionTripsNLinks.size());
            congestionWriter.write(vehicleToPerson.toString());

            int totalCongestion = 0;
            int numberOfTrips = 0;
            for(Map.Entry<Id<Vehicle>, ArrayList<ArrayList<CongElemData>>> entry : this.congestionTripsNLinks.entrySet()){
                //Trips
                ArrayList<ArrayList<CongElemData>> trips = entry.getValue();

                for(ArrayList<CongElemData> trip : trips){
                    numberOfTrips += 1;

                    double congestionForThisTrip = 0;
                    double endTime = 0;
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
                    }
                    totalCongestion += congestionForThisTrip;

                    double startTime = trip.get(0).timeEntered;
                    //double endTime = trip.get(trip.size() - 1).timeExited;

                    congestionWriter.write(String.format(
                            "%s\t%s\t%s\t%s\t%s\t%s\n",
                            numberOfTrips, entry.getKey(), vehicleToPerson.get(entry.getKey()), startTime, endTime, congestionForThisTrip
                    ));
                }
            }

            //Write average congestion for iteration
            congestionWriter.write("Number of trips : " + numberOfTrips + "\n");
            congestionWriter.write("Total time spent in congestion : " + totalCongestion + "\n");
            if(numberOfTrips != 0){
                congestionWriter.write("Average congestion per trip : " + (totalCongestion / numberOfTrips) + "\n");
            }

            congestionWriter.close();
        } catch (IOException ioException){
            System.out.println("Could not write congestion data!");
        }
    }

    public void writeCharts(String filenameTotal, String filenameAverage){
        logger.warn("Writing charts...");
        /*if(filenameAverage.equals(filenameTotal)){  //Make sure file names aren't identical to avoid overwritting
            filenameAverage = filenameAverage + "2";
        }*/
        logger.warn("Writing charts...");
        logger.warn("Number of iterations to write: " + iterationCongestion.size());
        double[] iterationNumbers = new double[iterationCongestion.size()];
        double[] timeCongested = new double[iterationCongestion.size()];
        double[] averageTimeCongested = new double[iterationCongestion.size()];

        for (int i = 0; i < iterationCongestion.size(); i++){
            iterationNumbers[i] = i;
            logger.warn("Congestion this iteration : " + iterationCongestion.get(i).toString());
            timeCongested[i] = iterationCongestion.get(i);

            averageTimeCongested[i] = iterationCongestion.get(i) / iterationPlans.get(i);
        }

        XYLineChart chartTotal = new XYLineChart("Total Congestion", "iteration", "total congestion");
        chartTotal.addSeries("time", iterationNumbers, timeCongested);  //title, iteration number, average congestion per trip
        chartTotal.saveAsPng(filenameTotal, 800, 600);

        XYLineChart chartAverage = new XYLineChart("Average Congestion per Trip", "iteration", "average congestion");
        chartAverage.addSeries("time", iterationNumbers, averageTimeCongested);  //title, iteration number, average congestion per trip
        chartAverage.saveAsPng(filenameAverage, 800, 600);
        logger.warn("Made charts...");
    }
}
