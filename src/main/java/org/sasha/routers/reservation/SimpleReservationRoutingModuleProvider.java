package org.sasha.routers.reservation;

import com.google.inject.Provider;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.router.AStarEuclideanFactory;
import org.matsim.core.router.AStarLandmarksFactory;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.facilities.ActivityFacility;

public class SimpleReservationRoutingModuleProvider implements Provider<RoutingModule> {
    private Provider<RoutingModule> tripRouterProvider;
    private PopulationFactory populationFactory;
    private ActivityFacility teleport;
    private Network network;
    private double flowCapacityFactor;
    private LeastCostPathCalculator leastCostPathCalculator;
    private String mode;

    public SimpleReservationRoutingModuleProvider(
            LeastCostPathCalculator leastCostPathCalculator,
            //Provider<RoutingModule> tripRouterProvider,
            PopulationFactory populationFactory,
            //ActivityFacility teleport,
            Network network,
            double flowCapacityFactor,
            String mode) {
        //this.tripRouterProvider = tripRouterProvider;
        this.populationFactory = populationFactory;
        //this.teleport = teleport;
        this.network = network;
        this.flowCapacityFactor = flowCapacityFactor;
        this.leastCostPathCalculator = leastCostPathCalculator;
        this.mode = mode;
    }

    @Override
    public RoutingModule get() {
        //Use a default TravelTime thingy.
        //I have nothing to add in how travel times are calculated
        //matsim\src\main\java\org\matsim\core\trafficmonitoring\FreeSpeedTravelTime.java
        return new SimpleReservationRoutingModule(
                /*//new FreeSpeedTravelTime(),
                "car",
                network,
                populationFactory);*/
                //new FreeSpeedTravelTime(),
                this.mode,
                this.network,
                this.populationFactory,
                //new DijkstraFactory().createPathCalculator(network, travelDisutility, this.travelTime)
                /*new DijkstraFactory().createPathCalculator(
                        network,
                        new SimpleReservationAsTravelDisutility(100, 1, 60),
                        new FreeSpeedTravelTime())*/
                /*
                new SimpleReservationLeastCostPathCalculator(
                        new AStarLandmarksFactory(1).createPathCalculator(
                                network,
                                new SimpleReservationAsTravelDisutility(
                                        100,
                                        1000,
                                        this.flowCapacityFactor,
                                        60
                                ),
                                new FreeSpeedTravelTime()
                        )
                )
                 */
                this.leastCostPathCalculator
        );
    }
}
