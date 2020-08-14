package org.sasha.strategy;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.replanning.PlanStrategyModule;
import org.matsim.core.replanning.ReplanningContext;
import org.sasha.reserverV2.ReservationManagerV2;

public class ReservationResetStrategyModule implements PlanStrategyModule {
    private static final Logger logger = Logger.getLogger(ReservationResetStrategyModule.class);

    ReservationResetStrategyModule(){

    }

    @Override
    public void prepareReplanning(ReplanningContext replanningContext) {

    }

    @Override
    public void handlePlan(Plan plan) {
        //Remove person's reservations
        logger.warn("Resetting person : " + plan.getPerson().getId() + "'s reservations!");
        ReservationManagerV2.getInstance().removeReservationsForPerson(plan.getPerson().getId());
    }

    @Override
    public void finishReplanning() {

    }
}
