package org.sasha.events.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.utils.charts.XYLineChart;
import org.matsim.vehicles.Vehicle;
import org.sasha.routers.reservation.SimpleReservationRoutingModule;

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
        LinkEnterEventHandler, LinkLeaveEventHandler, PersonDepartureEventHandler, IterationEndsListener, ShutdownListener {
    private static final Logger logger = Logger.getLogger(CongestionDetectionEventHandler.class);
    //private Map<Integer, Double> iterationCongestion = new HashMap<>();         //HashMap to store the total time spent in congestion
    //private Map<Integer, Integer> iterationPlans = new HashMap<>();             //HashMap to store number of plans per iteration, instead of getting all plans from controller or scenario or whatever

    private ArrayList<Double> iterationCongestion = new ArrayList<>();
    private ArrayList<Integer> iterationPlans = new ArrayList<>();

    private double congestionTime = 0;                                          //Congestion for the current iteration
    private int numberOfDepartureEvents = 0;
    private Map<Id<Vehicle>,Double> earliestLinkExitTime = new HashMap<>() ;
    private Network network;
    private String filenameTotal;
    private String filenameAverage;

    public CongestionDetectionEventHandler( Network network ) {
        this.network = network ;
        this.filenameTotal = "output/totalCongestion.png";
        this.filenameAverage = "output/averageCongestion.png";
    }

    public CongestionDetectionEventHandler( Network network, String filenameTotal, String filenameAverage ) {
        this.network = network ;
        this.filenameTotal = filenameTotal;
        this.filenameAverage = filenameAverage;
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
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
        Link link = network.getLinks().get( event.getLinkId() ) ;
        double linkTravelTime = link.getLength() / link.getFreespeed( event.getTime() );
        this.earliestLinkExitTime.put( event.getVehicleId(), event.getTime() + linkTravelTime ) ;
    }

    @Override
    public void handleEvent(LinkLeaveEvent event) {
        double excessTravelTime = event.getTime() - this.earliestLinkExitTime.get( event.getVehicleId() ) ;
        congestionTime += excessTravelTime;
        //System.out.println( "excess travel time: " + excessTravelTime ) ;
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        Id<Vehicle> vehId = Id.create( event.getPersonId(), Vehicle.class ) ; // unfortunately necessary since vehicle departures are not uniformly registered
        this.earliestLinkExitTime.put( vehId, event.getTime() ) ;
        this.numberOfDepartureEvents += 1;
    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        /*logger.warn("Iteration ends for congestion detector...");
        int iteration = event.getIteration();
        logger.warn("Total congestion for this iteration (" + iteration + "): " + congestionTime);
        logger.warn("Total number of plans for iteration (" + iteration + "): " + numberOfDepartureEvents);
        //iterationCongestion.add(congestionTime);
        //iterationPlans.add(numberOfDepartureEvents);
        //this.iterationCongestion.put(iteration, congestionTime);
        //this.iterationPlans.put(iteration, numberOfDepartureEvents);
        this.congestionTime = 0;
        this.numberOfDepartureEvents = 0;
        this.earliestLinkExitTime.clear();*/
    }

    @Override
    public void notifyShutdown(ShutdownEvent event) {
        logger.warn("Going to write charts...");
        logger.warn("Can't write charts with data cause the data is in another instance of the class, so would need a class intermediaray to handle the data");
        logger.warn("Just look at the console here and do the calculations yourself!");
        //I just LOVE how long it takes to get a simple thing working
        //writeCharts(this.filenameTotal, this.filenameAverage);
        logger.warn("Wrote charts!");
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
