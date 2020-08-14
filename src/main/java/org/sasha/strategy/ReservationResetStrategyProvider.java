package org.sasha.strategy;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.PlanStrategyImpl;
import org.matsim.core.replanning.modules.ReRoute;
import org.matsim.core.replanning.selectors.BestPlanSelector;
import org.matsim.core.router.TripRouter;
import org.matsim.core.router.TripRouterFactoryBuilderWithDefaults;
import org.sasha.routers.reservation.SimpleReservationLeastCostPathCalculatorFactory;

import javax.inject.Inject;
import javax.inject.Provider;

public class ReservationResetStrategyProvider implements Provider<PlanStrategy> {
    @Inject private Provider<TripRouter> tripRouterProvider;
    private Scenario sc;
    private static final Logger logger = Logger.getLogger(ReservationResetStrategyProvider.class);
    Provider<TripRouter> factory;

    public ReservationResetStrategyProvider(Scenario sc){
        this.sc = sc;

        //Omfg, need to link here AS WELL
        //Y DO THIS MATSIM HOW MANY TIMES DO I NEED TO TELL YOU TO USE MY ROUTING MODULE FOR MY MODE
        //I BETTER not have to fucking do this I swear...
        TripRouterFactoryBuilderWithDefaults builder = new TripRouterFactoryBuilderWithDefaults();
        builder.setLeastCostPathCalculatorFactory(new SimpleReservationLeastCostPathCalculatorFactory());
        this.factory = builder.build( sc );
    }

    @Override
    public PlanStrategy get() {
        logger.warn("Getting ReservationResetStrategyProvider!");
        //PlanStrategyImpl.Builder builder = new PlanStrategyImpl.Builder(new ReservationPlanSelector());
        PlanStrategyImpl.Builder builder = new PlanStrategyImpl.Builder(new BestPlanSelector<>());

        ReservationResetStrategyModule mod = new ReservationResetStrategyModule();
        builder.addStrategyModule(mod);
        //builder.addStrategyModule(new ReRoute(sc, tripRouterProvider));
        builder.addStrategyModule(new ReRoute(sc, this.factory));

        return builder.build();
    }
}
