package org.sasha.strategy;

import org.matsim.api.core.v01.population.HasPlansAndId;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.ReplanningContext;
import org.sasha.reserverV2.ReservationManagerV2;


//Look at CarSharing example
public class ReservationPlanStrategy implements PlanStrategy {
    @Override
    public void run(HasPlansAndId<Plan, Person> person) {
        ReservationManagerV2.getInstance().removeReservationsForPerson(person.getId());
    }

    @Override
    public void init(ReplanningContext replanningContext) {

    }

    @Override
    public void finish() {

    }
}
