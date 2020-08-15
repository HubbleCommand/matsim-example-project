package org.sasha.routers.reservation;

/*public class SimpleMainModeIdentifier {
}*/
import java.util.List;

import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.router.MainModeIdentifier;

/**
 * @author thibautd
 */
public class SimpleMainModeIdentifier implements MainModeIdentifier {
    private final MainModeIdentifier defaultModeIdentifier;

    public SimpleMainModeIdentifier(final MainModeIdentifier defaultModeIdentifier) {
        this.defaultModeIdentifier = defaultModeIdentifier;
    }

    @Override
    public String identifyMainMode(List<? extends PlanElement> tripElements) {
        for ( PlanElement pe : tripElements ) {
            if ( pe instanceof Leg && ((Leg) pe).getMode().equals( "car" ) ) {
                return "car";
            }
            if ( pe instanceof Leg && ((Leg) pe).getMode().equals( "rcar" ) ) {
                return "rcar";
            }
        }
        // if the trip doesn't contain a teleportation leg,
        // fall back to the default identification method.
        return defaultModeIdentifier.identifyMainMode( tripElements );
    }
}
