package org.sasha.reserver;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import java.util.ArrayList;
import java.util.HashMap;

public class ReservationSlot {
    private int slot;
    private HashMap<Link, Integer> reservations; //Maps the link to the number of reservations
    //TODO: use Link or LinkImpl?
    //Need getID()

    private HashMap<Id<Link>, Integer> reservations2; //Maps the link to the number of reservations

    public ReservationSlot(){

    }

    public int getReservations(Link link){
        return reservations.get(link);
    }

    public void makeReservation(Link link){
        if(reservations.containsKey(link)){
            reservations.put(link, reservations.get(link) + 1);
        } else {
            reservations.put(link, 1);
        }
    }
}
