package org.sasha.reserver;

import org.matsim.api.core.v01.network.Link;

import java.util.ArrayList;

public final class ReservationManager {
    private static ReservationManager INSTANCE;
    private String info = "Initial info class";
    private int timeInterval = 300; //Default 5-minute time interval, stored as seconds
    private ArrayList<ReservationSlot> reservations;

    //ArrayList cannot have stuff added to an index bigger than it's current size
    //Hashmap will be easier

    private ReservationManager() {
    }

    public synchronized static ReservationManager getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ReservationManager();
        }

        return INSTANCE;
    }

    // getters and setters

    //Only need time and link where reservation is needed
    public void makeReservation(int timeStart, int timeEnd, Link link){
        int startSlot = time % timeInterval;
        for(int i = startSlot; i < ((timeEnd % timeInterval) - (timeStart % timeInterval)); i++){
            if() {      // If no slot for this time exists
                reservations.add(new ReservationSlot());
            } else {    // Else update Slot Reserved Number
                reservations.set(i)
            }
        }
    }

    public ReservationSlot getReservation(int time){
        //Each slot is worth, base 300 seconds = 5 minutes
        //If recieve time in seconds, do modulo
        return reservations.get(time % timeInterval);
    }
}
