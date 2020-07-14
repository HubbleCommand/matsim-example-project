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

import java.util.Arrays;
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

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.facilities.Facility;

/**
 * @author sasha, based off of nagel's examples
 *
 */
//TODO finish
public class SimpleReservationRoutingModule implements RoutingModule {
    //@Inject private Population population ;
    //@Inject private Scenario scenario;

    private static final Logger logger = Logger.getLogger(SimpleReservationRoutingModule.class);

    private Object iterationData;

    private final String mode;
    private final PopulationFactory populationFactory;

    private final Network network;
    //private final LeastCostPathCalculator routeAlgo;
    private TravelTime travelTime;
    private TravelDisutility travelDisutility;
    private LeastCostPathCalculator pathCalculator;

    public SimpleReservationRoutingModule(
            TravelTime travelTime, //FIXME If something goes wrong, probably here
            //TravelDisutility travelDisutility, //Include this if many disutility functions end up being made
            String mode,
            final Network network,
            final PopulationFactory populationFactory//,  //Just bring a PF here... it's SO MUCH EASIER & SIMPLER
            /*final LeastCostPathCalculator routeAlgo*/) {
        this.network = network;
        //this.routeAlgo = routeAlgo;
        this.travelTime = travelTime;
        travelDisutility = new SimpleReservationAsTravelDisutility();
        pathCalculator = new DijkstraFactory().createPathCalculator(network, travelDisutility, this.travelTime);
        this.mode = mode;
        this.populationFactory = populationFactory;
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

        //New version is basically copied from source:
        //matsim\src\main\java\org\matsim\core\router\NetworkRoutingModule.java

        Leg newLeg = this.populationFactory.createLeg( this.mode );

        Gbl.assertNotNull(fromFacility);
        Gbl.assertNotNull(toFacility);

        Link fromLink = this.network.getLinks().get(fromFacility.getLinkId());
        if ( fromLink==null ) {
            Gbl.assertNotNull( fromFacility.getCoord() ) ;
            fromLink = NetworkUtils.getNearestLink( network, fromFacility.getCoord()) ;
        }
        Link toLink = this.network.getLinks().get(toFacility.getLinkId());
        if ( toLink==null ) {
            Gbl.assertNotNull( toFacility.getCoord() ) ;
            toLink = NetworkUtils.getNearestLink(network, toFacility.getCoord());
        }
        Gbl.assertNotNull(fromLink);
        Gbl.assertNotNull(toLink);

        if (toLink != fromLink) {
            // (a "true" route)
            Node startNode = fromLink.getToNode(); // start at the end of the "current" link
            Node endNode = toLink.getFromNode(); // the target is the start of the link
            //Path path = this.routeAlgo.calcLeastCostPath(startNode, endNode, departureTime, person, null);
            Path path = this.pathCalculator.calcLeastCostPath(startNode, endNode, departureTime, person, null);

            if (path == null)
                throw new RuntimeException("No route found from node " + startNode.getId() + " to node " + endNode.getId() + " by mode " + this.mode + ".");
            NetworkRoute route = this.populationFactory.getRouteFactories().createRoute(NetworkRoute.class, fromLink.getId(), toLink.getId());
            route.setLinkIds(fromLink.getId(), NetworkUtils.getLinkIds(path.links), toLink.getId());
            route.setTravelTime(path.travelTime);
            route.setTravelCost(path.travelCost);
            route.setDistance(RouteUtils.calcDistance(route, 1.0, 1.0, this.network));
            newLeg.setRoute(route);
            newLeg.setTravelTime(path.travelTime);
        } else {
            // create an empty route == staying on place if toLink == endLink
            // note that we still do a route: someone may drive from one location to another on the link. kai, dec'15
            NetworkRoute route = this.populationFactory.getRouteFactories().createRoute(NetworkRoute.class, fromLink.getId(), toLink.getId());
            route.setTravelTime(0);
            route.setDistance(0.0);
            newLeg.setRoute(route);
            newLeg.setTravelTime(0);
        }
        newLeg.setDepartureTime(departureTime);

        /* FIXME this was in the example code,
        *  but IntelliJ coughs up on code analysis when committing
        * */
        return Arrays.asList( newLeg );

        /*PopulationFactory pf = scenario.getPopulation().getFactory();
        RouteFactories routeFactory = ((PopulationFactory)pf).getRouteFactories() ;

        Id<Link> startLinkId = fromFacility.getLinkId();
        Id<Link> endLinkId = toFacility.getLinkId();

        Link startLink = this.network.getLinks().get(startLinkId);
        Link endLink = this.network.getLinks().get(endLinkId);

        Path path = this.pathCalculator.calcLeastCostPath(startLink.getToNode(), endLink.getFromNode(), departureTime, person, null);

        Leg leg = population.getFactory().createLeg( TransportMode.car ) ;

        /*Path path = pathCalculator.calcLeastCostPath(startLink.getToNode(), destinationLink.getFromNode(),
                now, person, vehicle ) ;//END USED TO BE HERE FOR BLOCK COMMENT

        NetworkRoute carRoute = routeFactory.createRoute(NetworkRoute.class, startLink.getId(), endLinkId );
        carRoute.setLinkIds(startLink.getId(), NetworkUtils.getLinkIds( path.links), endLinkId);
        carRoute.setTravelTime( path.travelTime );

        //carRoute.setVehicleId( Id.create( (vehicleId), Vehicle.class) ) ;

        Leg carLeg = pf.createLeg("car-reserve");
        carLeg.setTravelTime( path.travelTime );
        carLeg.setRoute(carRoute);

        System.out.println(iterationData);
        return Collections.emptyList();*/
    }

    @Override
    public String toString() {
        return "[SimpleReservationRoutingModule: mode="+this.mode+"]";
    }
}
