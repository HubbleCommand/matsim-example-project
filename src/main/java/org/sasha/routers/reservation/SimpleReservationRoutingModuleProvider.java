package org.sasha.routers.reservation;

import com.google.inject.Provider;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.router.AStarEuclideanFactory;
import org.matsim.core.router.AStarLandmarksFactory;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.facilities.ActivityFacility;

public class SimpleReservationRoutingModuleProvider implements Provider<RoutingModule> {
    private Provider<RoutingModule> tripRouterProvider;
    private PopulationFactory populationFactory;
    private ActivityFacility teleport;
    private Network network;

    public SimpleReservationRoutingModuleProvider(
            //Provider<RoutingModule> tripRouterProvider,
            PopulationFactory populationFactory,
            //ActivityFacility teleport,
            Network network) {
        //this.tripRouterProvider = tripRouterProvider;
        this.populationFactory = populationFactory;
        //this.teleport = teleport;
        this.network = network;
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
                "car",
                network,
                populationFactory,
                //new DijkstraFactory().createPathCalculator(network, travelDisutility, this.travelTime)
                /*new DijkstraFactory().createPathCalculator(
                        network,
                        new SimpleReservationAsTravelDisutility(100, 1, 60),
                        new FreeSpeedTravelTime())*/
                new SimpleReservationLeastCostPathCalculator(
                        //new SimpleReservationAsTravelDisutility(),
                        //new FreeSpeedTravelTime()
                        /*new DijkstraFactory().createPathCalculator(
                                network,
                                new SimpleReservationAsTravelDisutility(),
                                new FreeSpeedTravelTime())*/
                        /*new AStarEuclideanFactory().createPathCalculator(
                                network,
                                new SimpleReservationAsTravelDisutility(),
                                new FreeSpeedTravelTime()
                        )*/
                        new AStarLandmarksFactory(1).createPathCalculator(
                                network,
                                new SimpleReservationAsTravelDisutility(),
                                new FreeSpeedTravelTime()
                        )
                )
        );
    }
}
