package org.sasha.routers.reservation;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.facilities.Facility;

import java.util.Collections;
import java.util.List;
/**
 * @author sasha, based off of nagel's examples
 *
 */
//TODO finish
public class SimpleReservationRoutingModule implements RoutingModule {

    private Object iterationData;

    private final Network network;
    private final LeastCostPathCalculator routeAlgo;
    private TravelTime travelTime;
    private TravelDisutility travelDisutility;
    private LeastCostPathCalculator pathCalculator;

    public SimpleReservationRoutingModule(
            TravelTime travelTime,
            //TravelDisutility travelDisutility, //Include this if many disutility functions end up being made
            final Network network,
            final LeastCostPathCalculator routeAlgo) {
        this.network = network;
        this.routeAlgo = routeAlgo;
        this.travelTime = travelTime;
        travelDisutility = new SimpleReservationAsTravelDisutility();
        pathCalculator = new DijkstraFactory().createPathCalculator(network, travelDisutility, this.travelTime);
    }

    @Override
    public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime, Person person) {
        // calculate a route based on iterationData

        //Use base router like Dijkstra

        //Reserve route with ReservationManager

        System.out.println(iterationData);
        return Collections.emptyList();
    }
}
