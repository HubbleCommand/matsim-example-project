package org.sasha.reserver;

import org.matsim.api.core.v01.events.Event;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.core.events.handler.EventHandler;

//FIXME use IterationEndsListener or just use basic EventHandler or BasicEventHandler

public class ResetReservationsIterationEndsEventHandler implements IterationEndsListener, BasicEventHandler, EventHandler {
    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        //Need to reset the reservations from the last time
        //(maybe, this might be useless,
        //as MATSim may reinstall or reinstantiate the
        //ReservationManager, but things might get weird
        //as the ReservationManager is a singleton
        //So doing this just in case!)
        ReservationManager.getInstance().clearReservations();
    }

    @Override
    public void handleEvent(Event event) {

    }

    @Override
    public void reset(int iteration) {
        ReservationManager.getInstance().clearReservations();
    }
}
