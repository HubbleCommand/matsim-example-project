package org.sasha.strategy;


import org.apache.log4j.Logger;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.population.HasPlansAndId;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.replanning.selectors.PlanSelector;

public class ReservationPlanSelector implements PlanSelector<Plan, Person>, ActivityEndEventHandler {
    private static final Logger log = Logger.getLogger(ReservationPlanSelector.class);

    @Override
    public Plan selectPlan(HasPlansAndId<Plan, Person> person) {
        log.warn("calling selectPlan: PesonId: " + person.getId() + " Returning first Plan") ;

        //Return first plan. This is because the current setup only has one plan per person
        return person.getPlans().get(0);
    }

    @Override
    public void handleEvent(ActivityEndEvent event) {
        //log.error("calling handleEvent for an ActivityEndEvent") ;
    }

    @Override
    public void reset(int iteration) {
        log.warn("calling reset") ;
    }
}
