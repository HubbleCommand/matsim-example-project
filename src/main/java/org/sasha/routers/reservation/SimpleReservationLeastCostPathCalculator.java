package org.sasha.routers.reservation;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.RoutingModule;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
// import org.matsim.core.router.EmptyStageActivityTypes;
import org.matsim.core.router.RoutingModule;
// import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.facilities.Facility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

@Deprecated //No longer need this as well TODO remove
public final class SimpleReservationLeastCostPathCalculator implements LeastCostPathCalculator {
    //@Inject private Map<String,TravelTime> travelTimes ;

    private final Network network;
    //private final TravelTime travelTime;
    private LeastCostPathCalculator pathCalculator;
    private Map<Id<Node>,Double> costToNode = new HashMap<Id<Node>, Double>();
    private Map<Id<Node>,Id<Node>> previousNodes = new HashMap<Id<Node>, Id<Node>>();
    PriorityQueue<Id<Node>> queue = new PriorityQueue<Id<Node>>(11, new Comparator<Id<Node>>() {

        @Override
        public int compare(Id<Node> o1, Id<Node> o2) {
            return costToNode.get(o1).compareTo(costToNode.get(o2));
        }

    });
    SimpleReservationLeastCostPathCalculator(Network network, TravelDisutility travelCosts, TravelTime travelTimes) {
        this.network = network;

        /*if(travelTimes == null){

        } else {
            this.travelTime = travelTimes;
        }*/

        TravelDisutility travelDisutility = new SimpleReservationAsTravelDisutility();
        this.pathCalculator = new DijkstraFactory().createPathCalculator(this.network, travelDisutility, travelTimes);
    }

    @Override
    public Path calcLeastCostPath(Node fromNode, Node toNode, double starttime, Person person, Vehicle vehicle) {

        Path path = pathCalculator.calcLeastCostPath(fromNode, toNode, starttime, person, vehicle);

        //Can reserve here before returning path

        return path;
        /*
        initializeNetwork(fromNode.getId());

        while (!queue.isEmpty()) {
            Id<Node> currentId = queue.poll();
            if (currentId == toNode.getId()) return createPath(toNode.getId(),fromNode.getId());
            Node currentNode = network.getNodes().get(currentId);
            for (Link link:  currentNode.getOutLinks().values()){
                Node currentToNode = link.getToNode();
                double distance = link.getLength() + this.costToNode.get(currentId);
                if (distance < this.costToNode.get(currentToNode.getId())){
                    this.costToNode.put(currentToNode.getId(), distance);
                    update(currentToNode.getId());
                    this.previousNodes.put(currentToNode.getId(), currentId);
                }
            }
        }

        return null;*/
    }

    private Path createPath(Id<Node> toNodeId, Id<Node> fromNodeId) {
        List<Node> nodes = new ArrayList<Node>();
        List<Link> links = new ArrayList<Link>();
        Node lastNode = network.getNodes().get(toNodeId);
        while (!lastNode.getId().equals(fromNodeId)){
            if (!lastNode.getId().equals(toNodeId))
                nodes.add(0, lastNode);
            Node newLastNode = network.getNodes().get(this.previousNodes.get(lastNode.getId()));
            Link l = NetworkUtils.getConnectingLink(newLastNode,lastNode);
            links.add(0, l);
            lastNode = newLastNode;
        }


        return new Path(nodes,links,0.0,0.0);
    }

    private void initializeNetwork(Id<Node> startNode) {
        for (Node node : network.getNodes().values()){
            this.costToNode.put(node.getId(), Double.POSITIVE_INFINITY);
            this.previousNodes.put(node.getId(), null);
        }
        this.costToNode.put(startNode, 0.0);
        this.queue.add(startNode);

    }
    private void update(Id<Node> nodeToUpdate){
        this.queue.remove(nodeToUpdate);
        this.queue.add(nodeToUpdate);
    }
}
