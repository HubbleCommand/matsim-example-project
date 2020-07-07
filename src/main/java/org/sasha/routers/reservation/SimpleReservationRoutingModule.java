package org.sasha.routers.reservation;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.router.RoutingModule;
import org.matsim.facilities.Facility;

/**
 * @author nagel
 *
 */
public class SimpleReservationRoutingModule implements RoutingModule {

    private Object iterationData;

    @Override
    public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime, Person person) {
        // calculate a route based on iterationData

        //Use base router like Dijkstra

        //Reserve route with ReservationManager

        System.out.println(iterationData);
        return Collections.emptyList();
    }
}
