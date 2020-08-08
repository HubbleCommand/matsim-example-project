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


        if(pathsCalculated % 100 == 0){
            //logger.warn("Calculated " + pathsCalculated + " paths \n");
        }
        pathsCalculated ++;

        return path;
    }
}
