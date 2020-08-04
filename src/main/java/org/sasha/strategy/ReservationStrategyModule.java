package org.sasha.strategy;

import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Route;
import org.matsim.api.core.v01.replanning.PlanStrategyModule;
import org.matsim.core.replanning.ReplanningContext;
import org.matsim.utils.objectattributes.attributable.Attributes;
import org.sasha.routers.reservation.SimpleReservationRoutingModule;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.replanning.PlanStrategyModule;
import org.matsim.core.replanning.ReplanningContext;

/**
 * This might be a better example...
 * https://github.com/matsim-org/matsim-code-examples/blob/11.x/src/main/java/org/matsim/codeexamples/strategies/multiThreadedPlanStrategy/RunWithMultithreadedModule.java
 */
public class ReservationStrategyModule implements PlanStrategyModule, ActivityEndEventHandler {
    //TODO Probably least cost path calculator here instead
    //TODO look @ "How to write your own extension" part on ObjectAttributes and Customizable
    //TODO as gives insights on how to work with Attributes
    SimpleReservationRoutingModule routingModule;

    private static final Logger log = Logger.getLogger(ReservationStrategyModule.class);

    Scenario sc;
    Network net;
    Population pop;

    public ReservationStrategyModule(Scenario scenario, SimpleReservationRoutingModule routingModule) {
        this.sc = scenario;
        this.net = this.sc.getNetwork();
        this.pop = this.sc.getPopulation();
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
        for(PlanElement planElement : plan.getPlanElements()){
            Attributes attributes = planElement.getAttributes();

            System.out.println(attributes.toString());

            attributes.getAttribute("");
            /*for(String planAttribute : ){
                
            }*/
            
            //routingModule.calcRoute(planElement)
        }
    }

    @Override
    public void finishReplanning() {

    }

    @Override
    public void reset(int iteration) {

    }
}
