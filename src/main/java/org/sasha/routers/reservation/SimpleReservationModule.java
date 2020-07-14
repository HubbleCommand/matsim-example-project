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


import java.net.URL;
import java.util.Map;

import com.google.inject.Key;
import com.google.inject.name.Names;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
//If need ExamplesUtils, add to class path thingy (just hover over it dingus)
//import org.matsim.examples.ExamplesUtils;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.utils.objectattributes.attributable.Attributes;

public class SimpleReservationModule extends AbstractModule {
    public SimpleReservationModule(){}

    @Override
    public void install() {
        //V1 doesn't actually do reservation
        //addTravelDisutilityFactoryBinding("car").to(SimpleReservationAsTravelDisutilityFactory.class);

        //V2 Complex with routing module
        /*addRoutingModuleBinding("car").toProvider(new SimpleReservationRoutingModuleProvider(
                binder().getProvider(Key.get(RoutingModule.class, Names.named(TransportMode.pt))),
                scenario.getPopulation().getFactory(),
                teleport
        ));*/
    }
}
