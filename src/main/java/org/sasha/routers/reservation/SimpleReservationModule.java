package org.sasha.routers.reservation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.event.EventUtils;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.handler.EventHandler;
import org.matsim.core.router.*;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
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
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
//If need ExamplesUtils, add to class path thingy (just hover over it dingus)
//import org.matsim.examples.ExamplesUtils;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.utils.objectattributes.attributable.Attributes;
import org.sasha.events.handlers.CongestionDetectionEventHandler;
import org.sasha.reserver.ResetReservationsIterationEndsEventHandler;

public class SimpleReservationModule extends AbstractModule {
    //private final Config config;
    private final Controler controler;
    private final Scenario scenario;
    private final String reservationMode;

    public SimpleReservationModule(Config config, Controler controler, Scenario scenario, String reservationMode){
        //this.config = config;
        this.controler = controler;
        this.scenario = scenario;
        this.reservationMode = reservationMode;

        //Was trying to do something neat, but can't just add my own module to the config apparently
        //To look at later, no time for during the project
        /*ConfigGroup reservationModuleConfig = config.getModules().get("reservation");   //Adding own module in config causes errors
        if(reservationModuleConfig == null){
            reservationMode = "car";
        } else {
            String definedMode = reservationModuleConfig.getParams().get("modeReserved");
            reservationMode =  definedMode == null ? "car" : definedMode;   //If not defined, set to car
        }*/
        System.out.println("Reservation mode: " + reservationMode);
    }

    @Deprecated
    private void installProvider(){
        addRoutingModuleBinding("car").toProvider(
                new SimpleReservationRoutingModuleProvider(
                        // the module uses the trip router for the PT part.
                        // This allows to automatically adapt to user settings,
                        // including if they are specified at a later stage
                        // in the initialisation process.
                        //binder().getProvider(Key.get(RoutingModule.class, Names.named(TransportMode.pt))),
                        scenario.getPopulation().getFactory(),
                        //teleport,
                        scenario.getNetwork()
                )
        ); //Could .asSingleton() solve my woes?
        // we still need to provide a way to identify our trips
        // as being teleportation trips.
        // This is for instance used at re-routing.
        //bind(MainModeIdentifier.class).toInstance(new SimpleMainModeIdentifier(new MainModeIdentifierImpl()));
        //addEventHandlerBinding().toInstance((EventHandler) new ResetReservationsIterationEndsEventHandler());
    }

    @Override
    public void install() {
        //installProvider();
        //V0 worst, doesn't really do anything for my case
        //bindLeastCostPathCalculatorFactory().to(SimpleReservationLeastCostPathCalculatorFactory.class);

        //V1 doesn't actually do reservation
        //addTravelDisutilityFactoryBinding("car").to(SimpleReservationAsTravelDisutilityFactory.class);

        //addRoutingModuleBinding(this.reservationMode).to(SimpleReservationRoutingModule.class);

        //Va working
        addRoutingModuleBinding(this.reservationMode).toInstance(new SimpleReservationRoutingModule(
                "car",
                scenario.getNetwork(),
                scenario.getPopulation().getFactory(),
                new SimpleReservationLeastCostPathCalculator(
                        //new SimpleReservationAsTravelDisutility(),
                        //new FreeSpeedTravelTime()
                        new DijkstraFactory().createPathCalculator(
                                scenario.getNetwork(),
                                new SimpleReservationAsTravelDisutility(),
                                new FreeSpeedTravelTime())
                )
                /*new DijkstraFactory().createPathCalculator(
                        scenario.getNetwork(),
                        new SimpleReservationAsTravelDisutility(100, 1, 60),
                        new FreeSpeedTravelTime())*/
        ));
        //addEventHandlerBinding().toInstance(new ResetReservationsIterationEndsEventHandler());
        addControlerListenerBinding().toInstance(new ResetReservationsIterationEndsEventHandler());

        //Adding strategy
        //addPlanStrategyBinding()
    }
}
