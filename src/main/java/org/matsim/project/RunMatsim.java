/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.project;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.decongestion.DecongestionAnalysisModule;
import org.matsim.contrib.decongestion.DecongestionConfigGroup;
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.replanning.StrategyManager;
import org.matsim.core.replanning.StrategyManagerModule;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.run.RunBerlinScenario;
import org.sasha.events.handlers.CongestionDetectionEventHandler;
import org.sasha.reserverV2.TestReset;
import org.sasha.routers.reservation.*;
import org.sasha.strategy.ReservationResetStrategyProvider;
import org.sasha.strategy.ReservationStrategyManagerProvider;
//If need ExamplesUtils, just hover over it so that IntelliJ can automatically import it through Maven
//import org.matsim.examples.ExamplesUtils;

/**
 * @author sasha, based off of nagel's examples
 * */

/**
 * (Doesn't seem to be useful) ride mode using teleportation
 * 		https://github.com/matsim-org/matsim-code-examples/blob/12.x/src/main/java/org/matsim/codeexamples/programming/rideAsTeleportedCongestedMode/RunRideAsTeleportedCongestedModeExample.java
 *
 * (Might be useful?)
 * 		MobSim pluggable trip router	:	https://github.com/matsim-org/matsim-code-examples/blob/11.x/src/main/java/org/matsim/codeexamples/mobsim/pluggableTripRouter/RunPluggableTripRouterExample.java
 *		WithinDay replanning agents		:	https://github.com/matsim-org/matsim-code-examples/blob/12.x/src/main/java/org/matsim/codeexamples/withinday/withinDayReplanningAgents/RunWithinDayReplanningAgentExample.java
 *		Custom MobSim Agents			:	https://github.com/matsim-org/matsim-code-examples/blob/11.x/src/main/java/org/matsim/codeexamples/mobsim/ownMobsimAgentUsingRouter/RunOwnMobsimAgentUsingRouterExample.java
 *
 * (Not needed anymore) Look here for doc on making custom scoring function:
 * 		https://github.com/matsim-org/matsim-code-examples/tree/12.x/src/main/java/org/matsim/codeexamples/scoring/example16customscoring
 *
 * For router examples :
 * 		https://github.com/matsim-org/matsim-code-examples/tree/12.x/src/main/java/org/matsim/codeexamples/router
 *
 * 	Documentation on doing stuff before sim (PrepareForSim / PrepareForMobSim)
 * 		https://github.com/matsim-org/matsim-code-examples/tree/12.x/src/main/java/org/matsim/codeexamples/programming/ownPrepareForSimExample
 * 		https://github.com/matsim-org/matsim-code-examples/tree/12.x/src/main/java/org/matsim/codeexamples/mobsim/ownMobsimAgentWithPerception
 * 		https://github.com/matsim-eth/avtestbed-wip/blob/master/matsim/src/main/java/org/matsim/core/controler/PrepareForSimMultimodalImpl.java
 *
 * 	Examples on working with multiple modes:
 * 		https://github.com/matsim-org/matsim-code-examples/blob/12.x/src/main/java/org/matsim/codeexamples/integration/RunMultipleModesExample.java
 *
 * 	Analyse plans:
 * 		https://github.com/matsim-org/matsim-code-examples/blob/12.x/src/main/java/org/matsim/codeexamples/population/analyzePlans/AnalyzePlans.java
 *
 *
 */
public class RunMatsim{
	public void runBerlinScenario(String[] args){
		Config config = RunBerlinScenario.prepareConfig( args ) ;
		Scenario scenario = RunBerlinScenario.prepareScenario( config ) ;
		Controler controler = RunBerlinScenario.prepareControler( scenario ) ;

		controler.addOverridingModule( new OTFVisLiveModule() ) ;

		controler.run();
	}

	public void runMATSimSampleScenario(String[] args){
		Config config;
		if ( args==null || args.length==0 || args[0]==null ){
			config = ConfigUtils.loadConfig( "scenarios/geneva-10pct/config_wmodes.xml" );
		} else {
			config = ConfigUtils.loadConfig( args );
		}
		config.controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );
		// possibly modify config here

		config.getModules();

		Scenario scenario = ScenarioUtils.loadScenario(config) ;
		// possibly modify scenario here

		//Randomly set person mode
		//Doing this in Python as this is just too uncertain
		/*scenario.getPopulation().getPersons().forEach((k , v) ->{
			v.getSelectedPlan().getPlanElements().forEach(item -> {
				if(item instanceof Activity){
					item.getAttributes().
				}
			});
		});*/


		//Try to get the # of plans, but this has to be done with events, as plans may change per iteration with replanning!
		/*for(Map.Entry p: scenario.getPopulation().getPersons().entrySet()) {		p.getValue();	}*/

		Controler controler = new Controler( scenario ) ;

		//Install other stuffs
		/*controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				addEventHandlerBinding().toInstance(new CongestionDetectionEventHandler(scenario.getNetwork(), config.controler().getOutputDirectory()));
			}
		});*/

		//Install official congestion analyser
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				//this.bind(DelayAnalysis.class).asEagerSingleton();
				//this.addEventHandlerBinding().to(DelayAnalysis.class);
				this.bind(DecongestionConfigGroup.class).asEagerSingleton();
			}
		});
		//To use this, need to bind DecongestionConfigGroup like above
		controler.addOverridingModule(new DecongestionAnalysisModule());
		//controler.addOverridingModule(new DecongestionModule());

		//Install strategies
		//controler.addOverridingModule(new StrategyManagerModule());
		/*controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				//this.addPlanStrategyBinding().t;
				//this.
				//ReRoute.class.notifyAll();
				bind(StrategyManager.class).toProvider(new ReservationStrategyManagerProvider(
					scenario
				));
				//this.addPlanStrategyBinding("ReRoute").toProvider(new ReservationResetStrategyProvider(scenario));
			}
		});*/

		/*controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				this.addControlerListenerBinding().toInstance(new TestReset());
			}
		});*/

		int networkSize 	= scenario.getNetwork().getLinks().size();			//Network size
		int populationSite 	= scenario.getPopulation().getPersons().size();		//Population size

		//Install my own module
		controler.addOverridingModule(new SimpleReservationModule(
				config,
				controler,
				scenario,
				"rcar",
				config.qsim().getFlowCapFactor())	//Use current flow capacity factor
		);
		System.out.println("Installed my module !");
		System.out.println("Flow capacity factor : " + config.qsim().getFlowCapFactor());

		//Add this if want OTFVis live view thingy while simulation runs
		//Can also just ask OTFVis to not sync in the interface
		//controler.addOverridingModule( new OTFVisLiveModule() ) ;

		controler.run();
	}

	public static void main(String[] args) {
		new RunMatsim().runMATSimSampleScenario(args);

		// Choice between the 1pct and 10pct is one in the interface
		//new RunMatsim().runBerlinScenario(args);
	}
}
