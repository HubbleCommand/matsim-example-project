package org.sasha.routers.reservation;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.facilities.Facility;
import org.sasha.reserverV2.ReservationManagerV2;

import java.util.Arrays;
import java.util.List;

//This shouldn't be needed as the route calculation based on reservations
// is dont in the LeastCostPathCalculator, not in the module
// and as the module is passed the LCPC, can just pass an LCPC that doesn't
// look at reservations for cost calculation!
@Deprecated
public class SimpleReserveOnlyRoutingModule implements RoutingModule {
    public static final String MAIN_MODE = "car";
    private static final Logger logger = Logger.getLogger(SimpleReservationRoutingModule.class);

    private final String mode;
    private final PopulationFactory populationFactory;

    private final Network network;
    private final LeastCostPathCalculator pathCalculator;
    int counter = 0;

    Integer previousIteration;
    Integer currentIteration;

    public SimpleReserveOnlyRoutingModule(
            String mode,
            final Network network,
            final PopulationFactory populationFactory,
            final LeastCostPathCalculator routeAlgo) {
        this.network = network;
        this.pathCalculator = routeAlgo;
        this.mode = mode;
        this.populationFactory = populationFactory;
    }

    @Override
    public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime, Person person) {
        ReservationManagerV2.getInstance().checkClearPersonReservations(person);

        Leg newLeg = this.populationFactory.createLeg( this.mode );

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

        LeastCostPathCalculator.Path path = null;

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
            path = new LeastCostPathCalculator.Path(Arrays.asList(fromLink.getFromNode(), fromLink.getToNode()), Arrays.asList(fromLink), 0, 0);

            NetworkRoute route = this.populationFactory.getRouteFactories().createRoute(NetworkRoute.class, fromLink.getId(), toLink.getId());
            route.setTravelTime(0);
            route.setDistance(0.0);
            newLeg.setRoute(route);
            newLeg.setTravelTime(0);
        }

        newLeg.setDepartureTime(departureTime);

        if(counter % 1000 == 0){
            logger.warn("Mode for current leg: " + newLeg.getMode().toString());
        }
        counter++;

        //Reserve final path
        double timeToBeElapsed = 0;
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
        }

        // FIXME this was in the example code, but IntelliJ coughs up on code analysis when committing
        //logger.warn("Finished calculating route\n");
        return Arrays.asList( /*newActivity,*/ newLeg );
    }
}
