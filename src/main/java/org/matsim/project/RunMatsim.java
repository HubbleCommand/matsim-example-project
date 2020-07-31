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
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.handler.EventHandler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.run.RunBerlinScenario;
import org.sasha.events.handlers.CongestionDetectionEventHandler;
import org.sasha.reserver.ResetReservationsIterationEndsEventHandler;
import org.sasha.routers.reservation.*;


import com.google.inject.Key;
import com.google.inject.name.Names;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.router.RoutingModule;

import java.util.Map;
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
			config = ConfigUtils.loadConfig( "scenarios/equil/config.xml" );
		} else {
			config = ConfigUtils.loadConfig( args );
		}
		config.controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );
		// possibly modify config here
		config.qsim().getFlowCapFactor();	//TODO use for Disutility Function!!!

		config.getModules();

		Scenario scenario = ScenarioUtils.loadScenario(config) ;
		// possibly modify scenario here

		//Try to get the # of plans, but this has to be done with events, as plans may change per iteration with replanning!
		/*for(Map.Entry p: scenario.getPopulation().getPersons().entrySet()) {		p.getValue();	}*/

		Controler controler = new Controler( scenario ) ;

		//Install my own module
		System.out.println("Installing my module ...");
		controler.addOverridingModule(new SimpleReservationModule(config, controler, scenario, "car"));
		System.out.println("Installed my module !");

		//Add this if want OTFVis live view thingy while simulation runs
		//Can also just ask OTFVis to not sync in the interface
		//controler.addOverridingModule( new OTFVisLiveModule() ) ;

		setupCongestionDetector(controler, scenario);

		controler.run();
	}

	private void setupCongestionDetector(Controler controler, Scenario scenario){
		//So I guess this version just allows to read event results? I honestly have no fucking idea
		//why this was in the examples, because it doesn't seem to actually register the event handlers
		/*EventsManager events = EventsUtils.createEventsManager();

		CongestionDetectionEventHandler congestionDetectionEventHandler = new CongestionDetectionEventHandler(scenario.getNetwork());
		events.addHandler(congestionDetectionEventHandler);*/

		//Is this needed?
		/*MatsimEventsReader reader = new MatsimEventsReader(events);
		String inputFile = "output/example/output_events.xml.gz";
		reader.readFile(inputFile);*/

		//congestionDetectionEventHandler.writeCharts("output/totalCongestion.png", "output/averageCongestion.png");

		//System.out.println("Events file read!");

		//V2 actually works! Somewhat! Need to refer to logfile for results cause (read TODO in CongestionDetectionEventHandler)
		controler.addOverridingModule(new AbstractModule(){
			@Override public void install() {
				/*this.addEventHandlerBinding().toInstance( new MyEventHandler1() );
				this.addEventHandlerBinding().toInstance( new MyEventHandler2( ) );
				this.addEventHandlerBinding().toInstance( new MyEventHandler3() );*/
				this.addEventHandlerBinding().toInstance( new CongestionDetectionEventHandler( scenario.getNetwork() )  );

				//This is needed for Startup and Shutdown listeners
				//addControlerListenerBinding().toInstance(new CongestionDetectionEventHandler(scenario.getNetwork()));
			}
		});
	}

	public static void main(String[] args) {
		new RunMatsim().runMATSimSampleScenario(args);

		// Choice between the 1pct and 10pct is one in the interface
		//new RunMatsim().runBerlinScenario(args);
	}
}
