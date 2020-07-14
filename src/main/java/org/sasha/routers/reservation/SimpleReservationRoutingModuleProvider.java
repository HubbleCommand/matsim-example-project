package org.sasha.routers.reservation;

import com.google.inject.Provider;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.facilities.ActivityFacility;

public class SimpleReservationRoutingModuleProvider implements Provider<RoutingModule> {
    private Provider<RoutingModule> tripRouterProvider;
    private PopulationFactory populationFactory;
    private ActivityFacility teleport;
    private Network network;

    public SimpleReservationRoutingModuleProvider(
            Provider<RoutingModule> tripRouterProvider,
            PopulationFactory populationFactory,
            //ActivityFacility teleport,
            Network network) {
        this.tripRouterProvider = tripRouterProvider;
        this.populationFactory = populationFactory;
        //this.teleport = teleport;
        this.network = network;
    }

    @Override
    public RoutingModule get() {
        //return new SimpleReservationRoutingModule(tripRouterProvider, populationFactory, teleport);

        //Use a default TravelTime thingy.
        //I have nothing to add in how travel times are calculated
        //Or do I based on reservation?
        return new SimpleReservationRoutingModule(new FreeSpeedTravelTime(), "car", network, populationFactory);
    }
}
