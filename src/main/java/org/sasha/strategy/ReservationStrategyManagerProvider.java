package org.sasha.strategy;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlansConfigGroup.ActivityDurationInterpretation;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.algorithms.PlanAlgorithm;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.PlanStrategyImpl;
import org.matsim.core.replanning.StrategyManager;
import org.matsim.core.replanning.modules.AbstractMultithreadedModule;
import org.matsim.core.replanning.modules.ReRoute;
import org.matsim.core.replanning.selectors.BestPlanSelector;
import org.matsim.core.replanning.selectors.ExpBetaPlanSelector;
import org.matsim.core.replanning.selectors.RandomPlanSelector;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.charts.XYScatterChart;
//import org.matsim.core.utils.misc.OptionalTime;
//import org.matsim.testcases.MatsimTestCase;
/**
 *
 *
 * Useful doc
 *      https://www.matsim.org/apidocs/core/12.0/org/matsim/core/replanning/package-summary.html
 *      BetaTravelTest6IT.java
 */
public class ReservationStrategyManagerProvider implements Provider<StrategyManager> {
    private Scenario sc;

    public ReservationStrategyManagerProvider(Scenario sc){
        this.sc = sc;
    }

    @Override
    public StrategyManager get() {
        StrategyManager manager = new StrategyManager();
        manager.setMaxPlansPerAgent(5);

        //Strategy 1 : choose best plan
        //PlanStrategyImpl strategy1 = new PlanStrategyImpl(new BestPlanSelector<>());
        //PlanStrategyImpl strategy1 = PlanStrategyImpl.Builder
        /*PlanStrategyImpl.Builder strategy1 = new PlanStrategyImpl.Builder(new BestPlanSelector<>());
        strategy1.addStrategyModule(new ReservationStrategyModule(sc));*/
        PlanStrategy strategy1 = new PlanStrategyImpl.Builder(new BestPlanSelector<>())
                //NO! Don't want to reset reservations here! Only when rerouting or changing time!
                //.addStrategyModule(new ReservationStrategyModule(this.sc))
                //.addStrategyModule(new BestPlanSelector<String, String>())
                .build();

        PlanStrategy strategy2 = new PlanStrategyImpl.Builder(new BestPlanSelector<>())
                .addStrategyModule(new ReservationStrategyModule(this.sc))
                //.addStrategyModule(new ReRoute(sc, ))
                .build();
        //manager.addStrategy(strategy1, );
        manager.addStrategyForDefaultSubpopulation(strategy1, 0.9);

        /*// strategy1
        PlanStrategy strategy1 = new PlanStrategy(new KeepSelected());
        strategy1.addStrategyModule(new TimeAllocationMutator());

        // strategy2
        PlanStrategy strategy2 = new PlanStrategy(new RandomPlanSelector());
        strategy2.addStrategyModule(new ReRoute());

        // strategy3
        PlanStrategy strategy3 = new PlanStrategy(new RandomPlanSelector());
        strategy3.addStrategyModule(new TimeAllocationMutator());
        strategy3.addStrategyModule(new ReRoute());

        // strategy4
        PlanStrategy strategy4 = new PlanStrategy(new BestScoreSelector());

        // strategy5
        PlanStrategy strategy5 = new PlanStrategy(new ExpBetaSelector());

        // add the strategies to the manager
        manager.addStrategy(strategy1, 0.05);
        manager.addStrategy(strategy2, 0.07);
        manager.addStrategy(strategy3, 0.08);
        manager.addStrategy(strategy4, 0.75);
        manager.addStrategy(strategy5, 0.05);*/

        return manager;
    }
}
