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
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.run.RunBerlinScenario;
import org.sasha.routers.reservation.SimpleReservationRouterFactory;
import org.sasha.routers.reservation.SimpleReservationRoutingModule;

/**
 * @author nagel
 *
 */
public class RunMatsim{
	public void runBerlinScenario(String[] args){
		Config config = RunBerlinScenario.prepareConfig( args ) ;
		// possibly modify config here

		Scenario scenario = RunBerlinScenario.prepareScenario( config ) ;
		// possibly modify scenario here

		Controler controler = RunBerlinScenario.prepareControler( scenario ) ;
		// possibly modify controler here, e.g. add your own module

		//Add this if want OTFVis live view thingy while simulation runs
		//Can also just ask OTFVis to not sync in the interface
		//controler.addOverridingModule( new OTFVisLiveModule() ) ;

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

		// ---

		Scenario scenario = ScenarioUtils.loadScenario(config) ;

		// possibly modify scenario here

		// ---

		Controler controler = new Controler( scenario ) ;

		// possibly modify controler here

		//Add this if want OTFVis live view thingy while simulation runs
		//Can also just ask OTFVis to not sync in the interface
		//controler.addOverridingModule( new OTFVisLiveModule() ) ;

		// ---

		controler.run();
	}

	//installation of custom things done here to avoid duplicate code in runners
	public void setupController(Controler controller){
		//Add custom router
		controller.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				//Example originally followed was
				// https://github.com/matsim-org/matsim-code-examples/tree/ad3b07980f33ebf063aa19dc54b61a278d249f08/src/main/java/org/matsim/codeexamples/programming/leastCostPath
				// https://github.com/matsim-org/matsim-code-examples/tree/11.x/src/main/java/org/matsim/codeexamples/programming/leastCostPath
				//However this method is too complicated:
				//Requires a complete re-implementation of a routing function
				// which is not what is wanted, we just want to be able to use
				// our own cost function and do the reservation once the route
				// is calculated
				//bindLeastCostPathCalculatorFactory().to(SimpleReservationRouterFactory.class);
				addRoutingModuleBinding("car").to(SimpleReservationRoutingModule.class);
			}
		});
		//Add const function that takes Reservation into account
		//Look @ parking contrib

		//Add event listeners for stuffs / scoring functions
		//
	}

	public static void main(String[] args) {
		new RunMatsim().runMATSimSampleScenario(args);

		// Choice between the 1pct and 10pct is one in the interface
		//new RunMatsim().runBerlinScenario(args);
	}
}
