package org.sasha.routers.reservation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.event.EventUtils;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.decongestion.handler.DelayAnalysis;
import org.matsim.contrib.dvrp.router.DefaultLeastCostPathCalculatorWithCache;
import org.matsim.contrib.dvrp.router.DistanceAsTravelDisutility;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.handler.EventHandler;
import org.matsim.core.replanning.StrategyManager;
import org.matsim.core.router.*;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.*;
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
import org.sasha.events.handlers.SimpleCongestionDetectionEventHandler;
import org.sasha.reserver.ResetReservationsIterationEndsEventHandler;
import org.sasha.strategy.ReservationStrategyManagerProvider;

public class SimpleReservationModule extends AbstractModule {
    private final Config config;
    private final Controler controler;
    private final Scenario scenario;
    private final String reservationMode;
    private final String noReservationMode;
    private double flowCapacityFactor;

    public SimpleReservationModule(Config config, Controler controler, Scenario scenario, String noReservationMode,String reservationMode, double flowCapacityFactor){
        this.config = config;
        this.controler = controler;
        this.scenario = scenario;
        this.noReservationMode = noReservationMode;
        this.reservationMode = reservationMode;
        this.flowCapacityFactor = flowCapacityFactor;

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

    private void installProviders(){
        /*addRoutingModuleBinding("car").toProvider(
                new SimpleReservationRoutingModuleProvider(
                        // the module uses the trip router for the PT part.
                        // This allows to automatically adapt to user settings,
                        // including if they are specified at a later stage
                        // in the initialisation process.
                        //binder().getProvider(Key.get(RoutingModule.class, Names.named(TransportMode.pt))),
                        scenario.getPopulation().getFactory(),
                        //teleport,
                        scenario.getNetwork(),
                        this.flowCapacityFactor
                )
        );*/ //Could .asSingleton() solve my woes?
        // we still need to provide a way to identify our trips
        // as being teleportation trips.
        // This is for instance used at re-routing.
        //bind(MainModeIdentifier.class).toInstance(new SimpleMainModeIdentifier(new MainModeIdentifierImpl()));
        //addEventHandlerBinding().toInstance((EventHandler) new ResetReservationsIterationEndsEventHandler());
        addRoutingModuleBinding("car").toProvider(new SimpleReservationRoutingModuleProvider(
                new AStarLandmarksFactory(1).createPathCalculator(
                        scenario.getNetwork(),
                        new DistanceAsTravelDisutility(),
                        new FreeSpeedTravelTime()
                ),
                scenario.getPopulation().getFactory(),
                scenario.getNetwork(),
                this.flowCapacityFactor,
                "car"
        ));
        addRoutingModuleBinding("rcar").toProvider(new SimpleReservationRoutingModuleProvider(
                new SimpleReservationLeastCostPathCalculator(
                        new AStarLandmarksFactory(1).createPathCalculator(
                                scenario.getNetwork(),
                                new SimpleReservationAsTravelDisutility(
                                        100,
                                        500,
                                        this.flowCapacityFactor,
                                        60
                                ),
                                new FreeSpeedTravelTime()
                        )
                ),
                scenario.getPopulation().getFactory(),
                scenario.getNetwork(),
                this.flowCapacityFactor,
                "rcar"
            )
        );
    }

    private void installScoringFunctions(){
        ScoringFunctionFactory instance = new ScoringFunctionFactory(){
            @Inject private ScoringParametersForPerson params;
            @Inject private Network network ;
            @Override public ScoringFunction createNewScoringFunction(Person person) {
                final ScoringParameters parameters = params.getScoringParameters( person );
                SumScoringFunction sumScoringFunction = new SumScoringFunction() ;
                sumScoringFunction.addScoringFunction(new CharyparNagelActivityScoring(parameters));
                sumScoringFunction.addScoringFunction(new CharyparNagelLegScoring(parameters, network));
                sumScoringFunction.addScoringFunction(new CharyparNagelAgentStuckScoring(parameters));

                //double income = (double) person.getAttributes().getAttribute( NET_INCOME_PER_MONTH );
                //double margUtlOfMoney = 2000. / income;;
                //log.warn( "margUtlOfMoney=" + margUtlOfMoney ) ;
                //sumScoringFunction.addScoringFunction( new CharyparNagelMoneyScoring( margUtlOfMoney ) ) ;

                return sumScoringFunction ;
            }
        } ;
        this.bindScoringFunctionFactory().toInstance(instance) ;
    }

    private void installCongestionAnalyser(){
        /*addEventHandlerBinding().toInstance(new CongestionDetectionEventHandler(
                        scenario.getNetwork(),
                        config.controler().getOutputDirectory()
                )
        );*/
        addEventHandlerBinding().toInstance(new SimpleCongestionDetectionEventHandler(
                this.scenario.getNetwork(),
                config.controler().getOutputDirectory()
        ));
    }

    private void installStrategies(){
        //this.addPlanStrategyBinding().t;
        //this.
        //ReRoute.class.notifyAll();
        bind(StrategyManager.class).toProvider(new ReservationStrategyManagerProvider(
                scenario
        ));
        //this.addPlanStrategyBinding("ReRoute").toProvider(new ReservationResetStrategyProvider(scenario));
    }

    @Override
    public void install() {
        //No point in installing scoring functions, would need a more advanced router!
        this.installScoringFunctions();
        this.installProviders();
        this.installCongestionAnalyser();
        //This should NOT be necessary according to current documentation
        //addTravelTimeBinding("rcar").to(networkTravelTime());
        //addTravelDisutilityFactoryBinding("rcar").to(carTravelDisutilityFactoryKey());

        //V0 worst, doesn't really do anything for my case
        //bindLeastCostPathCalculatorFactory().to(SimpleReservationLeastCostPathCalculatorFactory.class);

        //V1 doesn't actually do reservation
        //addTravelDisutilityFactoryBinding("car").to(SimpleReservationAsTravelDisutilityFactory.class);

        //addRoutingModuleBinding(this.reservationMode).to(SimpleReservationRoutingModule.class);

        //Va working
        //Routing module for those that don't calculate routes based on reservation, but
        // still reserve routes so that the reservation system takes them into account
        /*addRoutingModuleBinding("car").toInstance(new SimpleReservationRoutingModule(
                "car",
                scenario.getNetwork(),
                scenario.getPopulation().getFactory(),
                new AStarLandmarksFactory(1).createPathCalculator(
                        scenario.getNetwork(),
                        new DistanceAsTravelDisutility(),
                        new FreeSpeedTravelTime()
                )
        ));
        addRoutingModuleBinding("rcar").toInstance(new SimpleReservationRoutingModule(
                "car",
                scenario.getNetwork(),
                scenario.getPopulation().getFactory(),
                new SimpleReservationLeastCostPathCalculator(
                        new AStarLandmarksFactory(1).createPathCalculator(
                                scenario.getNetwork(),
                                new SimpleReservationAsTravelDisutility(
                                        100,
                                        500,
                                        this.flowCapacityFactor,
                                        60
                                ),
                                new FreeSpeedTravelTime())
                        )
                )
        );*/
        System.out.println("Routing module should be installed !");
        addControlerListenerBinding().toInstance(new ResetReservationsIterationEndsEventHandler());

        bind(MainModeIdentifier.class).toInstance(new SimpleMainModeIdentifier(new MainModeIdentifierImpl()));
    }
}
