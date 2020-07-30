package org.sasha.population;


import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * @author kn
 * @author jlaudan
 * @author sasha, but I didn't really do anything, just added thing to set fraction in args
 *
 * For "resizing" a population to a specific area, look at:
 *      https://github.com/matsim-org/matsim-code-examples/blob/11.x/src/main/java/org/matsim/codeexamples/population/reducePopulationToAreaOfInterest/RunReducePopulationToAreaOfInterestExample.java
 */

// https://github.com/matsim-org/matsim-code-examples/blob/11.x/src/main/java/org/matsim/codeexamples/population/downsamplePopulation/RunPopulationDownsamplingExample.java
// This will be used to resize the population created
@Deprecated
public class PopulationResizing {
    private final String inputPopFilename;
    private final String outputPopFilename;
    private final double fraction;

    private PopulationResizing(String inputPopFilename, String outputPopFilename, double fraction) {
        this.inputPopFilename = inputPopFilename;
        this.outputPopFilename = outputPopFilename;
        this.fraction = fraction;
    }

    public static void main(final String[] args) {

        String outputPopFilename = "";
        String inputPopFilename = "";
        double fraction = 0;

        if ( args!=null && false ) {    //FIXME remove false if want to be runnable
            if (args.length != 3) {
                System.err.println("Usage: cmd inputPop.xml.gz outputPop.xml.gz 0.3");
                System.exit(401);
            } else {
                inputPopFilename = args[0] ;
                outputPopFilename = args[1] ;
                fraction = Double.parseDouble(args[3]);
            }
        } else {//FIXME remove this else statement want to be runnable
            //FIXME always has null pointer exception, no point using, just use GUI tool for population sample
            inputPopFilename = "D:\\Files\\Uni\\Projet Bachelor\\matsim-sim\\scenarios\\geneva\\plansCPPwTFCT_routefixmaybe.xml" ;
            outputPopFilename = "D:\\tmp\\plans_genf_all_10pct.xml" ;
            fraction = .10;
        }

        PopulationResizing app = new PopulationResizing(inputPopFilename, outputPopFilename, fraction);
        app.run();
    }

    private void run() {

        // create an empty scenario using an empty configuration
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

        // the writer will be called by the reader and write the new population file. As parameter the fraction of the
        // input population is passed. In our case we will downsize the population to 1%.
        StreamingPopulationWriter writer = new StreamingPopulationWriter(fraction);

        // the reader will read in an existing population file
        StreamingPopulationReader reader = new StreamingPopulationReader(scenario);
        reader.addAlgorithm(writer);

        try {
            writer.startStreaming(outputPopFilename);
            reader.readFile(inputPopFilename);
        } finally {
            writer.closeStreaming();
        }
    }
}
