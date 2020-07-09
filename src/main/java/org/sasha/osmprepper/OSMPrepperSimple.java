package org.sasha.osmprepper;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkSimplifier;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;
import org.matsim.core.network.algorithms.NetworkCleaner;

import java.net.MalformedURLException;

//Code taken from https://simunto.com/matsim/tutorials/eifer2019/slides_day1.pdf
public class OSMPrepperSimple {
    public static void main(String[] args) {
        CoordinateTransformation wgs84_utm32N = TransformationFactory.getCoordinateTransformation("EPSG:4326", "EPSG:25832");
        Network network = NetworkUtils.createNetwork();

        OsmNetworkReader reader = new OsmNetworkReader(network, wgs84_utm32N);
        reader.setKeepPaths(true);
        reader.setMemoryOptimization(true);

        reader.parse("D:\\Files\\Uni\\Projet Bachelor\\matsim-sim\\scenarios\\geneva\\mappbfs\\genf_area_small.osm");
        new NetworkSimplifier().run(network);

        //This line is incredible! Makes the fie about 1/6 of the size!
        new NetworkCleaner().run(network);

        new NetworkWriter(network).write("D:\\Files\\Uni\\Projet Bachelor\\matsim-sim\\scenarios\\geneva\\mappbfs\\tmp\\network_detailed_cleaned.xml.gz");
    }
}
