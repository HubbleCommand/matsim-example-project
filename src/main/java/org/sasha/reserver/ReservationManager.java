package org.sasha.reserver;

import org.matsim.api.core.v01.network.Link;

import java.util.ArrayList;
import java.util.HashMap;

//Look at the ReservationSlot class for more useful links
public final class ReservationManager {
    private static ReservationManager INSTANCE;
    private String info = "Initial info class";
    private int timeInterval = 300; //Default 5-minute time interval, stored as seconds

    //ArrayList cannot have stuff added to an index bigger than it's current size
    //Hashmap will be easier
    private HashMap<Integer, ReservationSlot> reservations; //Integer here stores the slot, relative to the time

    private ReservationManager() {
        reservations = new HashMap<>();
    }

    public synchronized static ReservationManager getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ReservationManager();
        }

        return INSTANCE;
    }

    // getters and setters

    //Only need time and link where reservation is needed
    public void makeReservation(double timeStart, double timeEnd, Link link){
        int startSlot = (int) (timeStart % timeInterval);
        for(int i = startSlot; i < ((timeEnd % timeInterval) - (timeStart % timeInterval)); i++){
            if(reservations.containsKey(i)) {      // If a slot for this time exists
                reservations.put(i, new ReservationSlot());
            } else {                                // Else add new slot
                reservations.put(i, new ReservationSlot());
            }
        }
    }

    public ReservationSlot getAllReservationAtTime(double time){
        //Each slot is worth, base 300 seconds = 5 minutes
        //If recieve time in seconds, do modulo
        return reservations.get((int) (time % timeInterval));
    }

    public int getReservations(double time, Link link){
        //Each slot is worth, base 300 seconds = 5 minutes
        //If recieve time in seconds, do modulo
        int timeToCheck = (int) (time % timeInterval);
        ReservationSlot reserve = reservations.get(timeToCheck);
        //THIS WAS RETURNING NULL
        //IF THERE IS NO K,V ENTRY FOR timeToCheck, then will return null
        if(reserve != null){
            return reserve.getReservations(link);
        } else {
            return 0;
        }
    }
}
