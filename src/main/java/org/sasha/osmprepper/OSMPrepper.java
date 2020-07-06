package org.sasha.osmprepper;

import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.osm.networkReader.LinkProperties;
import org.matsim.contrib.osm.networkReader.OsmTags;
import org.matsim.contrib.osm.networkReader.SupersonicOsmNetworkReader;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.utils.gis.shp2matsim.ShpGeometryUtils;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Note: use the OSMPrepperSimple, this one consistently encounters problems
// I have no idea where I originally found this code, but it is not 100% original
public class OSMPrepper {
    /*Default
    private static String UTM32nAsEpsg = "EPSG:25832";
    private static Path input = Paths.get("/path/to/your/input/data.osm.pbf");
    private static Path filterShape = Paths.get("/path/to/your/filter/shape-file.shp");
    */

    /*Try 1 with large data, resulted in out of memory error
    private static String UTM32nAsEpsg = "EPSG:25832";
    private static Path input = Paths.get("D:\\Files\\Uni\\Projet Bachelor\\Data\\rhone-alpes-latest.osm.pbf");
    private static Path filterShape = Paths.get("C:\\Users\\sasha\\Downloads\\rhone-alpes-latest-free.shp\\gis_osm_roads_free_1.shp");
    */

    //Try 2 with smaller data
    //private static String UTM32nAsEpsg = "EPSG:25832";
    private static String UTM32nAsEpsg = "EPSG:3857";
    //private static Path input = Paths.get("C:\\Users\\sasha\\Downloads\\planet_5.743,45.986_6.534,46.405.osm.pbf");
    //private static Path input = Paths.get("C:\\Users\\sasha\\Downloads\\Extracts\\berlin.osm");
    private static Path input = Paths.get("C:\\Users\\sasha\\Downloads\\genf_area_small.osm");
    private static Path filterShape = Paths.get("C:\\Users\\sasha\\Downloads\\Extracts\\planet_5.743,45.986_6.534,46.405-shp\\shape\\roads.shp");

    public static void main(String[] args) throws MalformedURLException {
        new OSMPrepper().create();
    }

    private void create() throws MalformedURLException {
        // choose an appropriate coordinate transformation. OSM Data is in WGS84. When working in central Germany,
        // EPSG:25832 or EPSG:25833 as target system is a good choice
        CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation(
                TransformationFactory.WGS84, UTM32nAsEpsg
                //TransformationFactory.WGS84, TransformationFactory.WGS84
        );

        // load the geometries of the shape file, so they can be used as a filter during network creation
        // using PreparedGeometry instead of Geometry increases speed a lot (usually)
        List<PreparedGeometry> filterGeometries = ShpGeometryUtils.loadPreparedGeometries(filterShape.toUri().toURL());

        // create an osm network reader with a filter
        // Reader needs a .OSM.PBF file
        SupersonicOsmNetworkReader reader = new SupersonicOsmNetworkReader.Builder()
                .setCoordinateTransformation(transformation)
                .setIncludeLinkAtCoordWithHierarchy((coord, hierarchyLevel) -> {

                    // take all links which are motorway, trunk, or primary-street regardless of their location
                    if (hierarchyLevel <= LinkProperties.LEVEL_PRIMARY) return true;

                    // whithin the shape, take all links which are contained in the osm-file
                    //return ShpGeometryUtils.isCoordInPreparedGeometries(coord, filterGeometries);

                    //Return false if you don't want to use the shape file
                    //However, chances are it will throw duplicate Node / Way ID errors
                    //Not totally true! When this type of error is returned, it seems to be a problem with the data itself!
                    return false;
                })
                .setAfterLinkCreated((link, osmTags, direction) -> {

                    // if the original osm-link contains a cycleway tag, add bicycle as allowed transport mode
                    // although for serious bicycle networks use OsmBicycleNetworkReader
                    if (osmTags.containsKey(OsmTags.CYCLEWAY)) {
                        Set<String> modes = new HashSet<>(link.getAllowedModes());
                        modes.add(TransportMode.bike);
                        link.setAllowedModes(modes);
                    }
                })
                .build();

        // the actual work is done in this call. Depending on the data size this may take a long time
        Network network = reader.read(input.toString());

        // clean the network to remove unconnected parts where agents might get stuck
        new NetworkCleaner().run(network);

        // write out the network into a file
        //new NetworkWriter(network).write("/path/to/your/output/network.xml.gz");
        new NetworkWriter(network).write("D:\\Files\\Uni\\Projet Bachelor\\Data\\output\\network.xml.gz");
    }
}
