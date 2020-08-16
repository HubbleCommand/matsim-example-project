package org.sasha.events.handlers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.decongestion.handler.DelayAnalysis;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleDepartsAtFacilityEventHandler;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.vehicles.Vehicle;

//Only get total congestion
//Basically just the DelayAnalysis in the decongestion example, and the
// https://github.com/matsim-org/matsim-code-examples/blob/12.x/src/main/java/org/matsim/codeexamples/events/eventsHandling/CongestionDetectionEventHandler.java
// but with a better Vehicle Leaves handler, and assorted changes,
// including a trip counter to do averages per trip
public class SimpleCongestionDetectionEventHandler implements
        //PersonDepartureEventHandler and VehicleDepartsAtFacilityEventHandler are deemed not necessary
        LinkLeaveEventHandler,
        LinkEnterEventHandler,
        VehicleEntersTrafficEventHandler,
        VehicleLeavesTrafficEventHandler {
    private static final Logger logger = Logger.getLogger(SimpleCongestionDetectionEventHandler.class);

    private Map<Id<Vehicle>,Double> earliestLinkExitTime = new HashMap<>() ;
    private Map<Id<Vehicle>, Double> vehicleId2enterTime = new HashMap<>();

    private double totalCongestionForThisIteration = 0;             //Total time spent in congestion
    private double totalExpectedTravelDuration = 0;                 //Total expected travel time
    private double totalTravelledTime = 0;                          //Total travel time
    private double totalTravelledDistance = 0;                      //Total distance travelled by all agents
    private double tripCounter = 0;                                 //Counts the number of trips

    private Network network;
    private String outputDirectory;

    public SimpleCongestionDetectionEventHandler(Network network, String outputDirectory){
        this.network = network;
        this.outputDirectory = outputDirectory;
    }

    private Link getLinkFromNetwork(Id<Link> linkId){
        return this.network.getLinks().get(linkId);
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
        //logger.debug("LinkEnterEvent Vehicle : " + event.getVehicleId());
        //System.out.println("LinkEnterEvent Vehicle : " + event.getVehicleId());
        Link link = network.getLinks().get( event.getLinkId() ) ;
        double linkTravelTime = link.getLength() / link.getFreespeed( event.getTime() );

        this.earliestLinkExitTime.put( event.getVehicleId(), event.getTime() + linkTravelTime ) ;

        this.vehicleId2enterTime.put(event.getVehicleId(), event.getTime());
    }

    @Override
    public void handleEvent(LinkLeaveEvent event) {
        //THESE TWO SHOULD BE IN VEHICLE LinkLeaveEvent and DEPARTSTRAFFIC
        Link link = network.getLinks().get( event.getLinkId() ) ;
        double linkTravelTime = link.getLength() / link.getFreespeed( event.getTime() );
        this.totalExpectedTravelDuration += linkTravelTime;
        this.totalTravelledDistance += link.getLength();

        if(this.earliestLinkExitTime.get( event.getVehicleId()) != null){
            double excessTravelTime = event.getTime() - this.earliestLinkExitTime.get( event.getVehicleId() ) ;

            /*if(excessTravelTime > 5){
                this.totalCongestionForThisIteration += excessTravelTime;
            }

            if (excessTravelTime > 500){
                System.out.println( "Large excess travel time: " + excessTravelTime ) ;
            }*/

        } else {
            //System.out.println( "No enter event for leave event with vehicle : " + event.getVehicleId() + " on link : " + event.getLinkId()) ;
        }
        if(this.vehicleId2enterTime.get(event.getVehicleId()) != null){
            //V2
            double traveltimeThisAgent = event.getTime() - this.vehicleId2enterTime.get(event.getVehicleId());
            double freespeedTravelTime = 1 + Math.ceil(this.network.getLinks().get(event.getLinkId()).getLength() / this.network.getLinks().get(event.getLinkId()).getFreespeed());
            double delayThisAgent = traveltimeThisAgent - freespeedTravelTime;

            if (delayThisAgent < 0.) {
                delayThisAgent = 0.;

            } else if (delayThisAgent > 0.) {
                //this.totalCongestionForThisIteration = this.totalCongestionForThisIteration + delayThisAgent;
                if(delayThisAgent > 1){
                    this.totalCongestionForThisIteration = this.totalCongestionForThisIteration + delayThisAgent;
                }
            }

            this.totalTravelledTime = this.totalTravelledTime + traveltimeThisAgent;
        }

        this.earliestLinkExitTime.remove(event.getVehicleId());
        this.vehicleId2enterTime.remove(event.getVehicleId());
    }

    @Override
    public void handleEvent(VehicleEntersTrafficEvent event) {
        //System.out.println("Vehicle : " + event.getVehicleId());

        //If congestion levels too high, then remove...
        this.vehicleId2enterTime.put(event.getVehicleId(), event.getTime());
        this.earliestLinkExitTime.put( event.getVehicleId(), event.getTime() );

        Link link = network.getLinks().get( event.getLinkId() ) ;
        double linkTravelTime = link.getLength() / link.getFreespeed( event.getTime() );
        this.totalExpectedTravelDuration += (linkTravelTime * event.getRelativePositionOnLink());
        this.totalTravelledDistance += (link.getLength() * event.getRelativePositionOnLink());

        this.tripCounter += 1;
    }

    @Override
    public void handleEvent(VehicleLeavesTrafficEvent event) {
        //Do da do
        Link link = network.getLinks().get( event.getLinkId() ) ;
        double linkTravelTime = link.getLength() / link.getFreespeed( event.getTime() );
        this.totalExpectedTravelDuration += (linkTravelTime * event.getRelativePositionOnLink());
        this.totalTravelledDistance += (link.getLength() * event.getRelativePositionOnLink());

        //Remove vehicle from times
        this.earliestLinkExitTime.remove(event.getVehicleId());
        this.vehicleId2enterTime.remove(event.getVehicleId());
    }

    @Override
    public void reset(int iteration) {
        logger.warn("Iteration ends for congestion detector...");
        try{
            String buffer = ",,,";
            BufferedWriter congestionWriter = IOUtils.getBufferedWriter(this.outputDirectory + "/ITERS/it." + iteration + "/" + iteration + "-SCDEVH-congestion.csv");
            congestionWriter.write(String.format("Total travel time (s)     : %s,%s\n", buffer, totalTravelledTime));
            congestionWriter.write(String.format("Total expected travel time (s) : %s,%s\n", buffer, totalExpectedTravelDuration));
            congestionWriter.write(String.format("Total congestion time (s): %s,%s\n", buffer, totalCongestionForThisIteration));
            congestionWriter.write(String.format("Total travel time (h)     : %s,%s\n", buffer, (totalTravelledTime/60/60)));
            congestionWriter.write(String.format("Total expected travel time (h) : %s,%s\n", buffer, (totalExpectedTravelDuration/60/60)));
            congestionWriter.write(String.format("Total congestion time (h): %s,%s\n", buffer, (totalCongestionForThisIteration/60/60)));
            congestionWriter.write(String.format("Total travelled distance (m): %s,%s\n", buffer, totalTravelledDistance));
            congestionWriter.write(String.format("Total number of trips: %s,%s\n", buffer, tripCounter));
            congestionWriter.write(String.format("Average distance (m) per trip: %s,%s\n", buffer, (totalTravelledDistance/tripCounter)));
            congestionWriter.write(String.format("Average travel time (s) per trip: %s,%s\n", buffer, (totalTravelledTime/tripCounter)));
            congestionWriter.write(String.format("Average expected time (s) per trip: %s,%s\n", buffer, (totalExpectedTravelDuration/tripCounter)));
            congestionWriter.write(String.format("Average congestion time (s) per trip: %s,%s\n", buffer, (totalCongestionForThisIteration/tripCounter)));
            congestionWriter.write(String.format("Percentage congestion: %s,%s\n", buffer, (totalCongestionForThisIteration/totalTravelledTime)));
            congestionWriter.write(String.format("Percentage congestion per trip: %s,%s\n", buffer, ((totalCongestionForThisIteration/tripCounter)/(totalTravelledTime/tripCounter))));
            congestionWriter.close();
        } catch (IOException e){
            logger.warn("Could not write congestion results!\n");
        }

        this.totalCongestionForThisIteration = 0;
        this.totalExpectedTravelDuration = 0;
        this.totalTravelledTime = 0;
        this.totalTravelledDistance = 0;
        this.tripCounter = 0;

        this.earliestLinkExitTime.clear();
    }
}
