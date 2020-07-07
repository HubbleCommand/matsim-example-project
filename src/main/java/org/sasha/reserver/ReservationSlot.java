package org.sasha.reserver;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import java.util.ArrayList;
import java.util.HashMap;

/*
*
* NetworkUtils: http://ci.matsim.org:8080/job/MATSim_M2/ws/matsim/target/site/apidocs/org/matsim/core/network/NetworkUtils.html
* Link :
*   http://ci.matsim.org:8080/job/MATSim_M2/ws/matsim/target/site/apidocs/org/matsim/api/core/v01/network/Link.html
*   https://www.matsim.org/apidocs/core/0.9.0/org/matsim/api/core/v01/network/Link.html
* */
public class ReservationSlot {
    private int slot;
    private HashMap<Link, Integer> reservations; //Maps the link to the number of reservations
    //TODO: use Link or LinkImpl?
    //Need getID()

    private HashMap<Id<Link>, Integer> reservations2; //Maps the link to the number of reservations

    public ReservationSlot(){
        reservations = new HashMap<Link, Integer>();
        reservations2 = new HashMap<Id<Link>, Integer>();
    }

    public int getReservations(Link link){
        return reservations.get(link);
    }
    public int getReservations(Id<Link> linkID){
        return reservations2.get(linkID);
    }

    public void makeReservation(Link link){
        if(reservations.containsKey(link)){
            reservations.put(link, reservations.get(link) + 1);
        } else {
            reservations.put(link, 1);
        }
    }

    public void makeReservation(Id<Link> linkID){
        if(reservations2.containsKey(linkID)){
            reservations2.put(linkID, reservations2.get(linkID) + 1);
        } else {
            reservations2.put(linkID, 1);
        }
    }
}
