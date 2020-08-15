package org.sasha.events.handlers;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleDepartsAtFacilityEventHandler;
import org.matsim.vehicles.Vehicle;

//Only get total congestion
public class SimpleCongestionDetectionEventHandler implements
        LinkLeaveEventHandler,
        LinkEnterEventHandler,
        VehicleDepartsAtFacilityEventHandler,
        VehicleEntersTrafficEventHandler,
        PersonDepartureEventHandler,
        VehicleLeavesTrafficEventHandler {
    private static final Logger logger = Logger.getLogger(SimpleCongestionDetectionEventHandler.class);
    private Map<Id<Vehicle>,Double> earliestLinkExitTime = new HashMap<>() ;
    private double totalCongestionForThisIteration = 0;             //Total time spent in congestion
    private double totalExpectedTravelDuration = 0;                 //Total expected travel time
    private Network network;
    private String outputDirectory;

    public SimpleCongestionDetectionEventHandler(Network network, String outputDirectory){
        this.network = network;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
        //logger.debug("LinkEnterEvent Vehicle : " + event.getVehicleId());
        //System.out.println("LinkEnterEvent Vehicle : " + event.getVehicleId());
        Link link = network.getLinks().get( event.getLinkId() ) ;
        double linkTravelTime = link.getLength() / link.getFreespeed( event.getTime() );
        this.totalExpectedTravelDuration += linkTravelTime;

        this.earliestLinkExitTime.put( event.getVehicleId(), event.getTime() + linkTravelTime ) ;
    }

    @Override
    public void handleEvent(LinkLeaveEvent event) {
        if(this.earliestLinkExitTime.get( event.getVehicleId()) != null){
            double excessTravelTime = event.getTime() - this.earliestLinkExitTime.get( event.getVehicleId() ) ;
            this.totalCongestionForThisIteration += excessTravelTime;

            if (excessTravelTime > 1000){
                System.out.println( "Large excess travel time: " + excessTravelTime ) ;
            }

            //TODO remove vehicle with this:
            //this.earliestLinkExitTime.remove(event.getVehicleId());
        } else {
            //System.out.println( "No enter event for leave event with vehicle : " + event.getVehicleId() + " on link : " + event.getLinkId()) ;
        }
    }

    @Override
    public void handleEvent(VehicleEntersTrafficEvent event) {
        /*logger.debug("Vehicle : " + event.getVehicleId());
        System.out.println("Vehicle : " + event.getVehicleId());
        this.earliestLinkExitTime.put( event.getVehicleId(), event.getTime() );*/
    }

    @Override
    public void handleEvent(VehicleDepartsAtFacilityEvent event) {
        //From the example, used PersonDepartureEvent, which doesn't hold the vehicle!
        //Id<Vehicle> vehId = Id.create( event.getPersonId(), Vehicle.class ) ; // unfortunately necessary since vehicle departures are not uniformly registered
        //This however didn't work when using vehicles in my rcar mode, as the departure didn't seem to be registered

        //
        /*logger.debug("Vehicle : " + event.getVehicleId());
        System.out.println("Vehicle : " + event.getVehicleId());
        this.earliestLinkExitTime.put( event.getVehicleId(), event.getTime() ) ;*/
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        event.getAttributes();
    }

    @Override
    public void handleEvent(VehicleLeavesTrafficEvent event) {
        this.earliestLinkExitTime.remove(event.getVehicleId());
    }

    @Override
    public void reset(int iteration) {
        logger.warn("Iteration ends for congestion detector...");
        String value1 = String.format("%.1f", totalCongestionForThisIteration);
        String value2 = String.format("%.1f", totalExpectedTravelDuration);
        logger.warn("Total congestion for this iteration (" + iteration + "): " + value1 + "\n");
        logger.warn("Total travel time for this iteration (" + iteration + "): " + value2 + "\n");
        System.out.println("Total congestion for this iteration (" + iteration + "): " + value1 + "\n");
        System.out.println("Total travel time for this iteration (" + iteration + "): " + value2 + "\n");
        this.totalCongestionForThisIteration = 0;
        this.totalExpectedTravelDuration = 0;
        this.earliestLinkExitTime.clear();
    }
}
