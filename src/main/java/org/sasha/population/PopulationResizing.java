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
 */

// https://github.com/matsim-org/matsim-code-examples/blob/11.x/src/main/java/org/matsim/codeexamples/population/downsamplePopulation/RunPopulationDownsamplingExample.java
// This will be used to resize the population created
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

        if ( args!=null ) {
            if (args.length != 3) {
                System.err.println("Usage: cmd inputPop.xml.gz outputPop.xml.gz 0.3");
                System.exit(401);
            } else {
                inputPopFilename = args[0] ;
                outputPopFilename = args[1] ;
                fraction = Double.parseDouble(args[3]);
            }
        } else {
            inputPopFilename = args[0] ;
            outputPopFilename = args[1] ;
            fraction = Double.parseDouble(args[3]);
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
