package org.sasha.routers.reservation;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;

import org.matsim.api.core.v01.population.Person;
// import org.matsim.core.router.EmptyStageActivityTypes;
// import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.util.LeastCostPathCalculator;

import org.matsim.api.core.v01.network.Link;
import org.matsim.vehicles.Vehicle;
import org.sasha.reserver.ReservationManager;

//
public final class SimpleReservationLeastCostPathCalculator implements LeastCostPathCalculator {
    private static final Logger logger = Logger.getLogger(SimpleReservationLeastCostPathCalculator.class);
    private LeastCostPathCalculator pathCalculator;
    private int pathsCalculated = 0;

    SimpleReservationLeastCostPathCalculator(LeastCostPathCalculator leastCostPathCalculator){
        this.pathCalculator = leastCostPathCalculator;
    }

    public Path tentativePathCalculation(Node fromNode, Node toNode, double starttime, Person person, Vehicle vehicle){
        Path path = pathCalculator.calcLeastCostPath(fromNode, toNode, starttime, person, vehicle);

        //Reserve path here before returning it
        double timeToBeElapsed = 0;

        for(Link link : path.links) {
            double currentLinkTime = timeToBeElapsed + (link.getLength() / link.getFreespeed());
            double currentLinkExitTime = timeToBeElapsed + currentLinkTime;
            //ReservationManager.getInstance().makeReservation(timeToBeElapsed, currentLinkExitTime, link);
            timeToBeElapsed += currentLinkTime;

            //logger.warn("Reserved link " + link.getId() + " at time " + timeToBeElapsed + "\n");
        }
        //pathsCalculated ++;

        return path;
    }

    @Override
    public Path calcLeastCostPath(Node fromNode, Node toNode, double starttime, Person person, Vehicle vehicle) {
        Path path = pathCalculator.calcLeastCostPath(fromNode, toNode, starttime, person, vehicle);

        //TODO FIXME see why this was set to 0
        //Reserve path here before returning it
        double timeToBeElapsed = 0;
        //double timeToBeElapsed = starttime;

        //TODO remove, this wasn't the issue with reservations
        // this is actually wrong anyways, of the from & to node are the same, then there will probably be no path calculated
        //Actually, verify the generated Geneva population as this error is thrown by them.
        /*if(path.links.size() == 0){
            throw new RuntimeException("WHAT HOW THE HELL IS THERE NO LINKS IN THE PATH HERE");
        }*/

        //TODO move to routing module, that is where it makes the most sense
        for(Link link : path.links) {
            //double currentLinkTime = timeToBeElapsed + (link.getFreespeed() / link.getLength());
            /*double currentLinkTime = timeToBeElapsed + (link.getLength() / link.getFreespeed());
            double currentLinkExitTime = timeToBeElapsed + currentLinkTime;
            ReservationManager.getInstance().makeReservation(timeToBeElapsed, currentLinkExitTime, link);
            timeToBeElapsed += currentLinkTime;*/

            double currentLinkTraverseTime = timeToBeElapsed + (link.getLength() / link.getFreespeed());
            double currentLinkExitTime = timeToBeElapsed + currentLinkTraverseTime;
            ReservationManager.getInstance().makeReservation(
                    timeToBeElapsed + starttime, currentLinkExitTime + starttime, link
            );
            timeToBeElapsed += currentLinkTraverseTime;

            //logger.warn("Reserved link " + link.getId() + " at time " + timeToBeElapsed + "\n");
            /*if(pathsCalculated % 1000 == 0){

            }*/
        }

        if(pathsCalculated % 100 == 0){
            //logger.warn("Calculated " + pathsCalculated + " paths \n");
        }
        pathsCalculated ++;

        return path;
    }
}
