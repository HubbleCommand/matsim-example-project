package org.sasha.routers.reservation;

import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

public class SimpleReservationAsTravelDisutilityFactory implements TravelDisutilityFactory {

    @Override
    public TravelDisutility createTravelDisutility(TravelTime timeCalculator) {
        return new SimpleReservationAsTravelDisutility();
    }
}
