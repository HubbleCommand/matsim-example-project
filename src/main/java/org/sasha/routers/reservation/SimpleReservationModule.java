package org.sasha.routers.reservation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.vehicles.VehicleType;

public class SimpleReservationModule extends AbstractModule {
    public SimpleReservationModule(){}

    @Override
    public void install() {
        //V1 doesn't actually do reservation
        addTravelDisutilityFactoryBinding("car").to(SimpleReservationAsTravelDisutilityFactory.class);
    }
}
