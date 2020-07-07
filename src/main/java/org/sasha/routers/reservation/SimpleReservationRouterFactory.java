package org.sasha.routers.reservation;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.Dijkstra;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

@Deprecated //This should not be used, too complicated

public class SimpleReservationRouterFactory implements LeastCostPathCalculatorFactory {
    @Override
    public LeastCostPathCalculator createPathCalculator(Network network, TravelDisutility travelCosts, TravelTime travelTimes) {
        //Want to avoid this,
        return new SimpleReservationRouter(network,null,null);

        //This still won't work as no way to actually reserve once route calculated
        //return new Dijkstra();
    }
}
