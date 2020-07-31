package org.sasha.reserver;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.vehicles.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
*
* NetworkUtils: http://ci.matsim.org:8080/job/MATSim_M2/ws/matsim/target/site/apidocs/org/matsim/core/network/NetworkUtils.html
* Link :
*   http://ci.matsim.org:8080/job/MATSim_M2/ws/matsim/target/site/apidocs/org/matsim/api/core/v01/network/Link.html
*   https://www.matsim.org/apidocs/core/0.9.0/org/matsim/api/core/v01/network/Link.html
* */
public class ReservationSlot {
    private int slot;
    //private HashMap<Link, Integer> reservations; //Maps the link to the number of reservations
    private Map<Id<Link>, Integer> reservations2;
    //TODO: use Link or LinkImpl?
    //FIXME determine if use Link or Id<Link>, Need getID()

    public ReservationSlot(){
        //reservations = new HashMap<Link, Integer>();
        reservations2 = new HashMap<>();
    }

    public ReservationSlot(Link link){
        /*reservations = new HashMap<Link, Integer>(){{
            put(link, 1);
        }};*/
        reservations2 = new HashMap<>(){{put(link.getId(), 1);}};
    }

    public int getReservations(Link link){
        /*if(link == null){
            return 0;
        } else {
            if(reservations == null){
                reservations = new HashMap<Link, Integer>(){{
                    put(link, 1);
                }};
                return 0;
            } else {
                int reservationsForLink = reservations.get(link);
                return reservationsForLink;
            }
        }*/
        if(link == null){
            return 0;
        } else {
            if(reservations2 == null){
                reservations2 = new HashMap<>(){{
                    put(link.getId(), 1);
                }};
                return 0;
            } else {
                //This was why it wasn't working! the HashMap.get() was returning null, and an int (any primitive type)
                //Cannot be insantiated or equal null, so threw NullPointerException!
                //TODO clean code
                if(reservations2.containsKey(link.getId())){
                    int reservationsForLink = reservations2.get(link.getId());
                    return reservationsForLink;
                } else {
                    return 0;
                }
            }
        }
    }

    public void makeReservation(Link link){
        /*
        if(reservations.containsKey(link)){
            reservations.put(link, reservations.get(link) + 1);
        } else {
            reservations.put(link, 1);
        }*/
        if(reservations2.containsKey(link.getId())){
            reservations2.put(link.getId(), reservations2.get(link.getId()) + 1);
        } else {
            reservations2.put(link.getId(), 1);
        }
    }
}
