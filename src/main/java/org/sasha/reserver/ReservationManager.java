package org.sasha.reserver;

import org.matsim.api.core.v01.network.Link;

import java.util.ArrayList;
import java.util.HashMap;

//Look at the ReservationSlot class for more useful links
public final class ReservationManager {
    private static ReservationManager INSTANCE;
    private double timeInterval = 300; //Default 5-minute time interval, stored as seconds

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

    //Only need time and link where reservation is needed
    public void makeReservation(double timeStart, double timeEnd, Link link){
        int startSlot = (int) (timeStart % timeInterval);
        for(int i = startSlot; i < ((timeEnd % timeInterval) - (timeStart % timeInterval)); i++){
            if(reservations.containsKey(i)) {      // If a slot for this time exists
                //int currentReservations = reservations.get(i).getReservations(link);
                //reservations.put(i, new ReservationSlot());

                //Don't need to put new instance of Reservation or update, HashMap contains a reference to the Object,
                // so updating the object will update the value of the object that the HashMap references
                //NOTE: this is because the object's class has a method that updates it's values
                // https://stackoverflow.com/questions/8195261/update-element-in-arraylist-hashmap-using-java
                reservations.get(i).makeReservation(link);
            } else {                                // Else add new slot
                reservations.put(i, new ReservationSlot(link));
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
            int reservations = reserve.getReservations(link);
            return reservations ;
        } else {
            //reservations.put(timeToCheck, new ReservationSlot(link));
            return 0;
        }
    }

    public String getReservations(){
        return reservations.toString();
    }

    //TODO need to relate the capacity in vehicles / min or whater to the time interval here
    public double getTimeInterval(){
        return this.timeInterval;
    }

    //Used to clear the reservations at the end of an iteration
    public void clearReservations(){
        reservations.clear();
    }
}
