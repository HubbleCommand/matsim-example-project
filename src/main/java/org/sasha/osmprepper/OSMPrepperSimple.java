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
    public static void main(String[] args) {
        CoordinateTransformation wgs84_utm32N = TransformationFactory.getCoordinateTransformation("EPSG:4326", "EPSG:25832");
        Network network = NetworkUtils.createNetwork();

        OsmNetworkReader reader = new OsmNetworkReader(network, wgs84_utm32N);
        reader.setKeepPaths(true);
        reader.setMemoryOptimization(true);

        reader.parse("D:\\Files\\Uni\\Projet Bachelor\\matsim-sim\\scenarios\\geneva\\mappbfs\\genf_area_small.osm");

        //These lines are incredible! Makes the network file about 1/6 of the size!
        new NetworkSimplifier().run(network);
        new NetworkCleaner().run(network);

        //https://github.com/matsim-org/matsim-code-examples/issues/271
        //Add additional data to links (here need to add reservation mode to links)
        int count=1;
        for(Link link : network.getLinks().values()) {
            link.setAllowedModes(new HashSet<>(Arrays.asList("car","car-reserved")));
        }

        new NetworkWriter(network).write("D:\\Files\\Uni\\Projet Bachelor\\matsim-sim\\scenarios\\geneva\\mappbfs\\tmp\\network_detailed_cleaned_wmodes.xml.gz");
    }
}
