package org.sasha.strategy;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.*;
import org.matsim.api.core.v01.replanning.PlanStrategyModule;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.utils.objectattributes.attributable.Attributes;
import org.matsim.vehicles.Vehicle;
import org.sasha.reserver.ReservationManager;
import org.sasha.routers.reservation.SimpleReservationLeastCostPathCalculator;
import org.sasha.routers.reservation.SimpleReservationRoutingModule;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.replanning.PlanStrategyModule;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.api.core.v01.network.Node;

/**
 * This might be a better example...
 * https://github.com/matsim-org/matsim-code-examples/blob/11.x/src/main/java/org/matsim/codeexamples/strategies/multiThreadedPlanStrategy/RunWithMultithreadedModule.java
 */
public class ReservationStrategyModule implements PlanStrategyModule, ActivityEndEventHandler {
    //TODO Probably least cost path calculator here instead
    // look @ "How to write your own extension" part on ObjectAttributes and Customizable
    // as gives insights on how to work with Attributes
    SimpleReservationLeastCostPathCalculator leastCostPathCalculator;
    SimpleReservationRoutingModule routingModule;

    private static final Logger logger = Logger.getLogger(ReservationStrategyModule.class);

    Scenario sc;
    Network net;
    Population pop;

    public ReservationStrategyModule(Scenario scenario,
                                     SimpleReservationLeastCostPathCalculator leastCostPathCalculator,
                                     SimpleReservationRoutingModule routingModule) {
        this.sc = scenario;
        this.net = this.sc.getNetwork();
        this.pop = this.sc.getPopulation();
        this.leastCostPathCalculator = leastCostPathCalculator;
        this.routingModule = routingModule;
    }

    @Override
    public void handleEvent(ActivityEndEvent event) {

    }

    @Override
    public void prepareReplanning(ReplanningContext replanningContext) {

    }

    @Override
    public void handlePlan(Plan plan) {
        //plan.getPlanElements()
        int index = 0;
        for(PlanElement planElement : plan.getPlanElements()){
            Attributes attributes = planElement.getAttributes();

            System.out.println(attributes.toString());

            //attributes.getAttribute("");
            /*for(String planAttribute : ){
                
            }*/
            
            //routingModule.calcRoute(planElement)

            if(planElement instanceof Activity){
                double activityStartTime = ((Activity) planElement).getStartTime();

                //TODO calculate least costly route at time:
                Coord fromCoord = ((Activity) planElement).getCoord();
                Node fromNode = NetworkUtils.getNearestLink( net, fromCoord).getFromNode() ;

                //Get next activity
                Leg betweenActivities = null;
                Activity nextActivity = null;
                //Start iterating over plans starting at current index, as we want to find the next activity
                for(int i = index; i < plan.getPlanElements().size() - 1; i++){
                    if(plan.getPlanElements().get(i) instanceof Activity && nextActivity == null) {
                        nextActivity = (Activity) plan.getPlanElements().get(i);
                        //break;
                    }
                    if(plan.getPlanElements().get(i) instanceof Leg && betweenActivities == null){
                        betweenActivities = (Leg) plan.getPlanElements().get(i);
                    }
                }
                if(nextActivity != null && betweenActivities != null){
                    Node toNode = NetworkUtils.getNearestLink(net, nextActivity.getCoord()).getToNode();

                    //TODO if least cost calculated route for this time is too high, or if there are lots of links that are
                    // oversaturated, then need to try to calculate routes for slightly different times
                    //betweenActivities.getMode()
                    //NOTE: don't use calcLeastCostPath, as this reserves the route!
                    LeastCostPathCalculator.Path path = leastCostPathCalculator.tentativePathCalculation(
                            fromNode,
                            toNode,
                            ((Activity) planElement).getStartTime(),
                            plan.getPerson(),
                            null //TODO need vehicle here? It's put to null elsewhere...
                        );

                    double timeToBeElapsed = 0;
                    for(Link link : path.links) {
                        double currentLinkTime = timeToBeElapsed + (link.getLength() / link.getFreespeed());
                        double currentLinkExitTime = timeToBeElapsed + currentLinkTime;
                        ReservationManager.getInstance().makeReservation(timeToBeElapsed, currentLinkExitTime, link);
                        timeToBeElapsed += currentLinkTime;

                        logger.warn("Reserved link " + link.getId() + " at time " + timeToBeElapsed + "\n");
                    }

                    //Find if has oversaturated links

                    //TODO set activity start / end time with new start / end time
                    //((Activity) planElement).setStartTime(12000)
                }
            }

            index++;
        }
    }

    @Override
    public void finishReplanning() {

    }

    @Override
    public void reset(int iteration) {

    }
}
