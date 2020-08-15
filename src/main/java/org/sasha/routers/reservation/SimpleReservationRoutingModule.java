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
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.facilities.Facility;

import java.util.ArrayList;
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
import org.sasha.reserverV2.ReservationManagerV2;

/**
 * @author sasha, based off of nagel's examples
 * Look at following classes for clues:
 *      Dijkstra
 */

public class SimpleReservationRoutingModule implements RoutingModule {
    public static final String MAIN_MODE = "rcar";
    private static final Logger logger = Logger.getLogger(SimpleReservationRoutingModule.class);

    private final String mode;
    private final PopulationFactory populationFactory;

    private final Network network;
    private final LeastCostPathCalculator pathCalculator;
    int counter = 0;

    //FIXME this should be an available function,
    /*public SimpleReservationRoutingModule(){
        this.network = network;
        this.travelTime = new FreeSpeedTravelTime();
        travelDisutility = new SimpleReservationAsTravelDisutility();
        pathCalculator = new DijkstraFactory().createPathCalculator(network, travelDisutility, this.travelTime);
        this.mode = "rcar";
        this.populationFactory = populationFactory;
    }*/

    public SimpleReservationRoutingModule(
            String mode,
            final Network network,
            final PopulationFactory populationFactory,
            final LeastCostPathCalculator routeAlgo) {
        this.network = network;
        this.pathCalculator = routeAlgo;
        this.mode = mode;
        this.populationFactory = populationFactory;
    }

    public void reservePath(Path path, double time){

    }

    @Override
    public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime, Person person) {
        //Check if need to clear person reservations
        ReservationManagerV2.getInstance().checkClearPersonReservations(person);

        //To avoid problems randomly use or don't use reservations cost in route calculations?

        //Use base router like Dijkstra
        //Reserve route with ReservationManager


        //In matsim-libs look at
        //  contribs\parking\src\main\java\org\matsim\contrib\parking\parkingsearch\routing\WithinDayParkingRouter.java
        //  contribs\matrixbasedptrouter\src\main\java\org\matsim\contrib\matrixbasedptrouter\MatrixBasedPtRoutingModule.java

        //New version is basically copied from source:
        //matsim\src\main\java\org\matsim\core\router\NetworkRoutingModule.java

        Leg newLeg = this.populationFactory.createLeg( this.mode );
        //newLeg.setMode();

        //Gbl.assertNotNull(fromFacility);
        //Gbl.assertNotNull(toFacility);

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

        Path path = null;

        if (toLink != fromLink) {
            // (a "true" route)
            Node startNode = fromLink.getToNode(); // start at the end of the "current" link
            Node endNode = toLink.getFromNode(); // the target is the start of the link
            path = this.pathCalculator.calcLeastCostPath(startNode, endNode, departureTime, person, null);

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
            path = new Path(Arrays.asList(fromLink.getFromNode(), fromLink.getToNode()), Arrays.asList(fromLink), 0, 0);

            NetworkRoute route = this.populationFactory.getRouteFactories().createRoute(NetworkRoute.class, fromLink.getId(), toLink.getId());
            route.setTravelTime(0);
            route.setDistance(0.0);
            newLeg.setRoute(route);
            newLeg.setTravelTime(0);
        }

        //TODO iterate over path links, if many are over reserved, then try leaving 5 minutes later
        //Can create dummy activity to shift person leave time
        Activity newActivity = this.populationFactory.createActivityFromLinkId("waitingr", fromFacility.getLinkId());
        newActivity.setEndTime(departureTime + 10000);

        //Set departure time doesn't seem to do anything here... need to create dummy activity as done above
        newLeg.setDepartureTime(departureTime);

        if(counter % 1000 == 0){
            logger.warn("Mode for current leg: " + newLeg.getMode().toString());
        }
        counter++;

        //Reserve final path
        /*double timeToBeElapsed = 0;
        for(Link link : path.links) {
            double currentLinkTraverseTime = timeToBeElapsed + (link.getLength() / link.getFreespeed());
            double currentLinkExitTime = timeToBeElapsed + currentLinkTraverseTime;
            ReservationManagerV2.getInstance().makeReservation(
                    timeToBeElapsed + departureTime,
                    currentLinkExitTime + departureTime,
                    link,
                    person
                    //,this.currentIteration
            );
            timeToBeElapsed += currentLinkTraverseTime;
        }*/

        double itineraryTimeToBeElapsed = 0;
        for(Link link : path.links) {
            double timeToTraverseLink = link.getLength() / link.getFreespeed();

            ReservationManagerV2.getInstance().makeReservation(
                    departureTime + itineraryTimeToBeElapsed,
                    departureTime + itineraryTimeToBeElapsed + timeToTraverseLink,
                    link,
                    person
            );

            itineraryTimeToBeElapsed += timeToTraverseLink;
        }

        // FIXME this was in the example code, but IntelliJ coughs up on code analysis when committing
        //logger.warn("Finished calculating route\n");
        return Arrays.asList( /*newActivity,*/ newLeg );

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
