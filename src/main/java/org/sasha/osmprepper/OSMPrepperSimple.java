package org.sasha.osmprepper;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkSimplifier;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;
import org.matsim.core.network.algorithms.NetworkCleaner;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;

//Code taken from https://simunto.com/matsim/tutorials/eifer2019/slides_day1.pdf
public class OSMPrepperSimple {
    private final String inputMapFilename;
    private final String outputMapFilename;
    private final String sourceCoordTransform;
    private final String targetCoordTransform;
    private final String[] modes;

    public OSMPrepperSimple(String inputMapFilename, String outputMapFilename, String sourceCoordTransform, String targetCoordTransform, String[] modes){
        this.inputMapFilename = inputMapFilename;
        this.outputMapFilename = outputMapFilename;
        this.sourceCoordTransform = sourceCoordTransform;
        this.targetCoordTransform = targetCoordTransform;
        this.modes = modes;
    }

    public static void main(String[] args) {
        String inputMapFilename = "";
        String outputMapFilename = "";
        String sourceCoordTransform = "";
        String targetCoordTransform = "";
        String[] modes = new String[1];

        if ( args!=null && false ) {    //FIXME remove false if want to be runnable
            if (args.length != 5) {
                System.err.println("Usage: cmd map.osm outputMap.xml.gz EPSG:4326 EPSG:2056 car:bike:pt");
                System.exit(401);
            } else {
                inputMapFilename = args[0] ;
                outputMapFilename = args[1] ;
                sourceCoordTransform = args[2] ;
                targetCoordTransform = args[3] ;
                modes = args[4].split(":");
            }
        } else {            //FIXME remove this else statement want to be runnable
            inputMapFilename = "D:\\Files\\Uni\\Projet Bachelor\\matsim-sim\\scenarios\\geneva\\mappbfs\\genf_area_small.osm" ;
            outputMapFilename = "D:\\Files\\Uni\\Projet Bachelor\\matsim-sim\\scenarios\\geneva\\mappbfs\\tmp\\network_clean_coordtransf.xml.gz";
            sourceCoordTransform = "EPSG:4326" ;
            targetCoordTransform =  "EPSG:25832";
            String modesStr = "car";
            modes = modesStr.split(":");
        }

        OSMPrepperSimple app = new OSMPrepperSimple(inputMapFilename, outputMapFilename, sourceCoordTransform, targetCoordTransform, modes);
        app.run();
    }

    private void run(){
        CoordinateTransformation coordinateTransformation = TransformationFactory.getCoordinateTransformation(sourceCoordTransform, targetCoordTransform);

        Network network = NetworkUtils.createNetwork();

        /**
         * Look at
         * https://github.com/matsim-org/matsim-libs/blob/master/matsim/src/main/java/org/matsim/core/utils/io/OsmNetworkReader.java
         * For clues.
         * WGS84 is not optimal for MATSim, which is why we coord transform to something (better?)
         * */
        //OsmNetworkReader reader = new OsmNetworkReader(network, coordinateTransformation);
        //Another method, but files are much bigger...
        OsmNetworkReader reader = new OsmNetworkReader(network, coordinateTransformation, true, true);
        //reader.setKeepPaths(true);
        //reader.setMemoryOptimization(true);

        reader.parse(inputMapFilename);

        //These lines are incredible! Makes the network file about 1/6 of the size!
        new NetworkSimplifier().run(network);
        new NetworkCleaner().run(network);

        //https://github.com/matsim-org/matsim-code-examples/issues/271
        //Add additional data to links (here need to add reservation mode to links)
        System.out.println("Doing personal config stuffs...");
        int count=1;
        for(Link link : network.getLinks().values()) {
            link.setAllowedModes(new HashSet<>(Arrays.asList(modes)));
        }

        new NetworkWriter(network).write(outputMapFilename);
    }
}
