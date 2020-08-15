package org.sasha.reserver;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.events.Event;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.ReplanningEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.ReplanningListener;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.core.events.handler.EventHandler;
import org.matsim.core.replanning.ReplanningContext;
import org.sasha.reserverV2.ReservationManagerV2;
import org.sasha.routers.reservation.SimpleReservationRoutingModule;
import org.sasha.writers.ReservationWriter;

//FIXME use IterationEndsListener or just use basic EventHandler or BasicEventHandler

public class ResetReservationsIterationEndsEventHandler implements IterationEndsListener, ReplanningListener, EventHandler {
    private static final Logger logger = Logger.getLogger(ResetReservationsIterationEndsEventHandler.class);

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        logger.warn("Number of reservationsV2 for this iteration : " + ReservationManagerV2.getInstance().getReservationsSum() + "\n");

        ReservationManagerV2.getInstance().updateCurrentIterationNumber(event.getIteration());

        //new ReservationWriter().write("/output/ITERS/it." + event.getIteration() + "/reservations.xml");

        //FIXME cannot clear all reservations, just of those that are replanning
        // as it is, reservations made during the previous iteration of those not replanning
        // will not be taken into account!
        // However, we still need to remove some plans, as those that are replanning have opened up their reserved slots!
        // Need to only remove the reservations of those that are replanning
        // This also cannot be done here as far as I can tell...

        //ReservationManagerV2.getInstance().clearReservations();
    }

    @Override
    public void reset(int iteration) {
        //Can't reset iterations here, for same reasons as above
        //reset() does not provide the necessary data
    }

    @Override
    public void notifyReplanning(ReplanningEvent event) {
        event.getServices();
        ReplanningContext replanningContext = event.getReplanningContext();
        replanningContext.getIteration();
        //ReplanningContext;
    }
}
