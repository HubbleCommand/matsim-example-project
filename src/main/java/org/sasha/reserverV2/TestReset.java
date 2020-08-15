package org.sasha.reserverV2;

import org.apache.log4j.Logger;
import org.matsim.core.controler.corelisteners.PlansReplanning;
import org.matsim.core.controler.listener.ReplanningListener;
import org.matsim.withinday.events.ReplanningEvent;
import org.matsim.withinday.events.handler.ReplanningEventHandler;

@Deprecated //This cannot do what I want it to do.... :(
public class TestReset implements
        ReplanningEventHandler,     //Only throws when withinday replanning NO WTF WAS I THINKING
        //REPLANNING EVENT HANDLER SHOULD BE CALLED HERE
        ReplanningListener,         //Doesn't give necessary information
        PlansReplanning             //Why does this one even exist?

{
    private static final Logger logger = Logger.getLogger(TestReset.class);

    @Override
    public void notifyReplanning(org.matsim.core.controler.events.ReplanningEvent event) {
        System.out.println("Replanning here for some reason...");
        //event.getReplanningContext().
    }

    @Override
    public void handleEvent(ReplanningEvent event) {
        System.out.println("Replanning for person: " + event.getPersonId());
        logger.warn("Replanning for person: " + event.getPersonId());
        //Remove person's existing reservations
    }
}
