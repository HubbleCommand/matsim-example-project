package org.sasha.routers.reservation;

import org.apache.log4j.Logger;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.population.routes.RouteFactories;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.facilities.Facility;

import java.util.Collections;
import java.util.List;

//From WithinDayParkingRouter
import javax.inject.Inject;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.router.TimeAsTravelDisutility;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.name.Named;

/**
 * @author sasha, based off of nagel's examples
 *
 */
//TODO finish
public class SimpleReservationRoutingModule implements RoutingModule {
    @Inject private Population population ;
    @Inject private Scenario scenario;

    private static final Logger logger = Logger.getLogger(SimpleReservationRoutingModule.class);

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
        logger.warn("Got here!");
        // calculate a route based on iterationData

        //Use base router like Dijkstra

        //Reserve route with ReservationManager

        //In matsim-libs look at
        //  contribs\parking\src\main\java\org\matsim\contrib\parking\parkingsearch\routing\WithinDayParkingRouter.java
        //  contribs\matrixbasedptrouter\src\main\java\org\matsim\contrib\matrixbasedptrouter\MatrixBasedPtRoutingModule.java

        PopulationFactory pf = scenario.getPopulation().getFactory();
        RouteFactories routeFactory = ((PopulationFactory)pf).getRouteFactories() ;

        Id<Link> startLinkId = fromFacility.getLinkId();
        Id<Link> endLinkId = toFacility.getLinkId();

        Link startLink = this.network.getLinks().get(startLinkId);
        Link endLink = this.network.getLinks().get(endLinkId);

        Path path = this.pathCalculator.calcLeastCostPath(startLink.getToNode(), endLink.getFromNode(), departureTime, person, null);

        Leg leg = population.getFactory().createLeg( TransportMode.car ) ;

        /*Path path = pathCalculator.calcLeastCostPath(startLink.getToNode(), destinationLink.getFromNode(),
                now, person, vehicle ) ;*/

        NetworkRoute carRoute = routeFactory.createRoute(NetworkRoute.class, startLink.getId(), endLinkId );
        carRoute.setLinkIds(startLink.getId(), NetworkUtils.getLinkIds( path.links), endLinkId);
        carRoute.setTravelTime( path.travelTime );

        //carRoute.setVehicleId( Id.create( (vehicleId), Vehicle.class) ) ;

        Leg carLeg = pf.createLeg("car-reserve");
        carLeg.setTravelTime( path.travelTime );
        carLeg.setRoute(carRoute);

        System.out.println(iterationData);
        return Collections.emptyList();
    }
}
