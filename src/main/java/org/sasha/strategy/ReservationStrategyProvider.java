package org.sasha.strategy;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.PlanStrategyImpl;
import org.sasha.routers.reservation.SimpleReservationRoutingModule;
import org.sasha.routers.reservation.SimpleReservationRoutingModuleProvider;

import javax.inject.Inject;
import javax.inject.Provider;

public class ReservationStrategyProvider implements Provider<PlanStrategy> {
    private EventsManager eventsManager;
    private Scenario scenario;

    private static final Logger log = Logger.getLogger(ReservationPlanSelector.class);

    @Inject
    ReservationStrategyProvider(EventsManager eventsManager, Scenario scenario) {
        this.eventsManager = eventsManager;
        this.scenario = scenario;
    }

    @Override
    public PlanStrategy get() {

        log.error("calling PlanStradegy.get()");
        // A PlanStrategy is something that can be applied to a Person (not a Plan).
        // It first selects one of the plans:
        ReservationPlanSelector planSelector = new ReservationPlanSelector();
        PlanStrategyImpl.Builder builder = new PlanStrategyImpl.Builder(planSelector);

        // the plan selector may, at the same time, collect events:
        eventsManager.addHandler(planSelector);

        // if you just want to select plans, you can stop here.


        // Otherwise, to do something with that plan, one needs to add modules into the strategy.  If there is at least
        // one module added here, then the plan is copied and then modified.
        //TODO THE CODE HERE MUST BE UNCOMMENTED FOR IT TO WORK!!!
        /*ReservationStrategyModule mod = new ReservationStrategyModule(
                scenario,
                new SimpleReservationRoutingModuleProvider(scenario.getPopulation().getFactory(), scenario.getNetwork()).get());
        builder.addStrategyModule(mod);*/

        // these modules may, at the same time, be events listeners (so that they can collect information):
        //eventsManager.addHandler(mod);

        return builder.build();
    }
}
