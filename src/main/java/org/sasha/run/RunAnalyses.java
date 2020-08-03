package org.sasha.run;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.travelsummary.events2traveldiaries.EventsToTravelDiaries;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsManagerImpl;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.vehicles.MatsimVehicleReader;
import playground.vsp.congestion.analysis.CongestionAnalysisEventHandler;
import playground.vsp.congestion.analysis.CongestionAnalysisWriter;
import playground.vsp.congestion.analysis.CongestionEventsWriter;
import java.io.File;

import org.apache.log4j.Logger;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.algorithms.EventWriterXML;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;

import playground.vsp.congestion.handlers.CongestionHandlerImplV10;
import playground.vsp.congestion.handlers.CongestionHandlerImplV3;
import playground.vsp.congestion.handlers.MarginalCongestionPricingHandler;
import java.io.File;
import java.io.IOException;

/**
 * @author sasha
 * This class will handle all the post-simulation analyses
 *
 * (analysis contrib)
 * To get TripWriter working, look at following classes:
 *      RunEventsToTravelDiaries
 *      EventsToTravelDiaries
 *
 * (vsp and / or decongestion contribs)
 * To get CongestionWriter or analyser whatever working, look at following classes:
 *
 */
public class RunAnalyses {
    private static final Logger log = Logger.getLogger(RunAnalyses.class);
    static String runDirectory;

    public static void main(String[] args){
        String eventsFileName = null;
        Config config = null;
        String appendage = "";
        String outputDirectory = null;

        if(true){ //Just set things here
            args = new String[4];
            args[0] = "D:\\Files\\Uni\\Projet Bachelor\\matsim-sim\\scenarios\\equil\\config.xml";
            args[1] = "D:\\Files\\Uni\\Projet Bachelor\\matsim-sim\\scenarios\\equil\\output\\output_events.xml.gz";
            args[2] = "";
            args[3] = "";
        }
        config = ConfigUtils.loadConfig(args[0]);
        outputDirectory = "D:\\tmp";//config.controler().getOutputDirectory();
        eventsFileName = args[1];

        Scenario scenario = ScenarioUtils.createScenario(config);

        new MatsimNetworkReader(scenario.getNetwork()).parse(config.network().getInputFileURL(config.getContext()));

        if (config.transit().isUseTransit()) {

            new TransitScheduleReader(scenario)
                    .readFile(config.transit().getTransitScheduleFile());

            new MatsimVehicleReader(scenario.getTransitVehicles())
                    .readFile(config.transit().getVehiclesFile());

        }

        EventsToTravelDiaries handler = new EventsToTravelDiaries(scenario);

        EventsManager events = new EventsManagerImpl();

        events.addHandler(handler);

        new MatsimEventsReader(events).readFile(eventsFileName);

        try {
            handler.writeSimulationResultsToTabSeparated(outputDirectory, "1");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Write congestion stuffs
        CongestionAnalysisEventHandler congestionAnalysisEventHandler = new CongestionAnalysisEventHandler(scenario, false);
        CongestionAnalysisWriter congestionAnalysisWriter = new CongestionAnalysisWriter(congestionAnalysisEventHandler, "D:\\tmp2");
        congestionAnalysisWriter.writeDetailedResults("car");
        congestionAnalysisWriter.writeAffectedAgentId2totalAmount();
        congestionAnalysisWriter.writeCausingAgentId2totalAmount();
        congestionAnalysisWriter.writeAvgTollPerDistance("car");
        congestionAnalysisWriter.writeAvgTollPerTimeBin("car");

        //FIXME cant get this working with any scenario outputs
        //Write congestion events
        //Look at CongestionEventsWriter class
        //RunAnalyses runAnalyses = new RunAnalyses();
        //runAnalyses.run();
    }

    private void run() {
        runDirectory = "D:\\Files\\Uni\\Projet Bachelor\\matsim-sim\\scenarios\\equil\\output\\";
        log.info("Loading scenario...");
        Config config = ConfigUtils.loadConfig(runDirectory + "output_config.xml");
        config.network().setInputFile(runDirectory + "output_network.xml.gz");
        config.plans().setInputFile(runDirectory + "output_plans.xml.gz");
        MutableScenario scenario = (MutableScenario) ScenarioUtils.loadScenario(config);
        log.info("Loading scenario... Done.");

        String outputDirectory = "D:\\tmp3";//runDirectory + "analysis_it." + config.controler().getLastIteration() + "/";
        File file = new File(outputDirectory);
        file.mkdirs();

        EventsManager events = EventsUtils.createEventsManager();

        EventWriterXML eventWriter = new EventWriterXML(outputDirectory + config.controler().getLastIteration() + ".events_ExternalCongestionCost_Offline.xml.gz");
        CongestionHandlerImplV10 congestionHandler = new CongestionHandlerImplV10(events, scenario);
        //MarginalCongestionPricingHandler marginalCostTollHandler = new MarginalCongestionPricingHandler(events, scenario);

        events.addHandler(eventWriter);
        events.addHandler(congestionHandler);
        //events.addHandler(marginalCostTollHandler);

        log.info("Reading events file...");
        MatsimEventsReader reader = new MatsimEventsReader(events);
        reader.readFile(runDirectory + "ITERS/it." + config.controler().getLastIteration() + "/" + config.controler().getLastIteration() + ".events.xml.gz");
        log.info("Reading events file... Done.");

        eventWriter.closeFile();

        congestionHandler.writeCongestionStats(outputDirectory + config.controler().getLastIteration() + ".congestionStats_Offline.csv");
    }
}
