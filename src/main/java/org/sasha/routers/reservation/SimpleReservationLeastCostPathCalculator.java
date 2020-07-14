package org.sasha.routers.reservation;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.RoutingModule;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
// import org.matsim.core.router.EmptyStageActivityTypes;
import org.matsim.core.router.RoutingModule;
// import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.facilities.Facility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;
import org.sasha.reserver.ReservationManager;

//
public final class SimpleReservationLeastCostPathCalculator implements LeastCostPathCalculator {
    private final Network network;
    //private final TravelTime travelTime;
    private LeastCostPathCalculator pathCalculator;

    SimpleReservationLeastCostPathCalculator(Network network, TravelDisutility travelCosts, TravelTime travelTimes) {
        this.network = network;

        /*if(travelTimes == null){

        } else {
            this.travelTime = travelTimes;
        }*/

        TravelDisutility travelDisutility = new SimpleReservationAsTravelDisutility();
        this.pathCalculator = new DijkstraFactory().createPathCalculator(this.network, travelCosts, travelTimes);
    }

    @Override
    public Path calcLeastCostPath(Node fromNode, Node toNode, double starttime, Person person, Vehicle vehicle) {

        Path path = pathCalculator.calcLeastCostPath(fromNode, toNode, starttime, person, vehicle);

        //Can reserve here before returning path
        double timeToBeElapsed = 0;
        for(Link link : path.links) {
            double currentLinkTime = link.getFreespeed() / link.getLength();
            double currentLinkExitTime = timeToBeElapsed + currentLinkTime;
            ReservationManager.getInstance().makeReservation(timeToBeElapsed, currentLinkExitTime, link);
            timeToBeElapsed += currentLinkTime;
        }

        return path;
    }
}
