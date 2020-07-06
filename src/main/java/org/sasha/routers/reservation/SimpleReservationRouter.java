package org.sasha.routers.reservation;

import org.matsim.core.router.RoutingModule;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

//The commented out imports for router do not seem to exist. TODO: look into
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
// import org.matsim.core.router.EmptyStageActivityTypes;
import org.matsim.core.router.RoutingModule;
// import org.matsim.core.router.StageActivityTypes;
import org.matsim.facilities.Facility;

public class SimpleReservationRouter implements RoutingModule {
    private Object iterationData;

    @Inject
    public SimpleReservationRouter(SimpleSimulationObserver observer) {
        this.iterationData = observer.getIterationData();
    }

    @Override
    public List<? extends PlanElement> calcRoute(Facility fromFacility,
                                                 Facility toFacility, double departureTime, Person person) {
        // calculate a route based on iterationData
        System.out.println(iterationData);
        return Collections.emptyList();
    }
}
