package org.sasha.reserverV2;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

import java.util.HashMap;
import java.util.Map;

public class ReservationManagerV2 {
    private static ReservationManagerV2 INSTANCE;
    private double timeInterval = 300; //Default 5-minute time interval, stored as seconds

    //ArrayList cannot have stuff added to an index bigger than it's current size
    //Hashmap will be easier
    private HashMap<Integer, ReservationSlotV2> reservations; //Integer here stores the slot, relative to the time

    private ReservationManagerV2() {
        reservations = new HashMap<>();
    }

    public synchronized static ReservationManagerV2 getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ReservationManagerV2();
        }

        return INSTANCE;
    }

    //Only need time and link where reservation is needed
    public void makeReservation(double timeStart, double timeEnd, Link link, Person person){
        int startSlot = (int) (timeStart % timeInterval);
        for(int i = startSlot; i < ((timeEnd % timeInterval) - (timeStart % timeInterval)); i++){
            if(reservations.containsKey(i)) {      // If a slot for this time exists
                //int currentReservations = reservations.get(i).getReservations(link);
                //reservations.put(i, new ReservationSlot());

                //Don't need to put new instance of Reservation or update, HashMap contains a reference to the Object,
                // so updating the object will update the value of the object that the HashMap references
                //NOTE: this is because the object's class has a method that updates it's values
                // https://stackoverflow.com/questions/8195261/update-element-in-arraylist-hashmap-using-java
                reservations.get(i).makeReservation(link, person.getId());
            } else {                                // Else add new slot
                reservations.put(i, new ReservationSlotV2(link, person.getId()));
            }
        }
    }

    public ReservationSlotV2 getAllReservationAtTime(double time){
        //Each slot is worth, base 300 seconds = 5 minutes
        //If recieve time in seconds, do modulo
        return reservations.get((int) (time % timeInterval));
    }

    public int getReservations(double time, Link link){
        //Each slot is worth, base 300 seconds = 5 minutes
        //If recieve time in seconds, do modulo
        int timeToCheck = (int) (time % timeInterval);
        ReservationSlotV2 reserve = reservations.get(timeToCheck);
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

    public int getReservationsSum(){
        int reservationSum = 0;
        for(Map.Entry<Integer, ReservationSlotV2> entry : reservations.entrySet()){
            reservationSum += entry.getValue().getReservationsSum();
        }
        return reservationSum;
    }

    public String getReservations(){
        return reservations.toString();
    }

    public HashMap<Integer, ReservationSlotV2> getSlots(){
        return this.reservations;
    }

    public double getTimeInterval(){
        return this.timeInterval;
    }

    public void removeReservationsForPerson(Id<Person> person){
        for(Map.Entry<Integer, ReservationSlotV2> slot : reservations.entrySet()){
            slot.getValue().removePersonReservations(person);
        }
    }
}
