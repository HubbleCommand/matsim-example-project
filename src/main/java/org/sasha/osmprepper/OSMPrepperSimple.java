package org.sasha.osmprepper;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;

import java.net.MalformedURLException;

//Code taken from https://simunto.com/matsim/tutorials/eifer2019/slides_day1.pdf
public class OSMPrepperSimple {
    public static void main(String[] args) throws MalformedURLException {
        CoordinateTransformation wgs84_utm32N = TransformationFactory.getCoordinateTransformation("EPSG:4326", "EPSG:25832");
        Network network = NetworkUtils.createNetwork();

        OsmNetworkReader reader = new OsmNetworkReader(network, wgs84_utm32N);
        //reader.setHierarchyLayer(51.9, 9.5, 51.2, 10.3, 3); // 3 = primary roads
        //reader.setHierarchyLayer(51.6, 9.8, 51.5, 10.0, 8); // 8 = residential roads
        reader.setKeepPaths(true);
        reader.setMemoryOptimization(true);

        reader.parse("C:\\Users\\sasha\\Downloads\\genf_area_small.osm");

        new NetworkWriter(network).write("C:\\Users\\sasha\\Downloads\\tmp\\network_detailed.xml.gz");
    }
}
