package org.sasha.reserverV2;

import org.apache.log4j.Logger;
import org.matsim.core.controler.listener.ReplanningListener;

@Deprecated //This cannot do what I want it to do.... :(
public class TestReset implements
        //ReplanningEventHandler,   //Only throws when withinday replanning
        ReplanningListener        //Doesn't give necessary information
        //PlansReplanning           //Why does this one even exist?
{
    private static final Logger logger = Logger.getLogger(TestReset.class);

    /*@Override
    public void handleEvent(ReplanningEvent event) {
        System.out.println("Replanning for person: " + event.getPersonId());
        logger.warn("Replanning for person: " + event.getPersonId());
        //ReservationManager.getInstance().removeReservationsForPerson(event.getPersonId());
    }*/

    @Override
    public void notifyReplanning(org.matsim.core.controler.events.ReplanningEvent event) {
        //event.getReplanningContext().
    }
}
