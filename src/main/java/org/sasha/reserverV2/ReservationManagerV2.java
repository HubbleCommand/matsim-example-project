package org.sasha.reserverV2;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.sasha.routers.reservation.SimpleReservationRoutingModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ReservationManagerV2 {
    private static final Logger logger = Logger.getLogger(ReservationManagerV2.class);
    private static ReservationManagerV2 INSTANCE;   //TODO this needs to be volatile to be core safe!
    private double timeInterval = 300; //Default 5-minute time interval, stored as seconds
    private int handledRemovals = 0;

    //ArrayList cannot have stuff added to an index bigger than it's current size
    //Hashmap will be easier
    private HashMap<Integer, ReservationSlotV2> reservations; //Integer here stores the slot, relative to the time
    //private HashMap<Integer, HashSet<Id<Person>>> personToSlot;
    private HashMap<Id<Person>, HashSet<Integer>> personSlots;  //For each person key, holds which slots they were in
    private HashMap<Id<Person>, Integer> lastIterationPersonReserved;
    private Integer currentIteration;

    private ReservationManagerV2() {
        reservations = new HashMap<>(20);
        //lastIterationPersonReserved = new HashMap<>(25000);
        //personSlots = new HashMap<Id<Person>, HashSet<Integer>>(25000);
        lastIterationPersonReserved = new HashMap<>();
        personSlots = new HashMap<>();
    }

    public synchronized static ReservationManagerV2 getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new ReservationManagerV2();
        }

        return INSTANCE;
    }

    public void updateCurrentIterationNumber(Integer iteration){
        this.currentIteration = iteration;
    }

    public void checkClearPersonReservations(Person person){
        //Otherwise, Person doesn't exist yet, nothing to do!
        if(this.lastIterationPersonReserved.containsKey(person.getId())){
            //Otherwise, this person's reservations are for the current iteration, so nothing to remove!
            //TODO: OH SHIT WAS THIS NOT WORKING THE WHOLE TIME!!!
            // WHAT WAS HAPPENING! IT SEEMED TO BE WORKING!!!
            // IT SHOULD BE:
            // if(!this.lastIterationPersonReserved.get(person.getId()).equals(currentIteration)){
            // NOTE: this was working as it was, the console was propery logging.
            // Some people did have existing reservations to delete and not delete, so it WAS working,
            // even though it was jank as fuck!
            // Actually funny enough this should be working!
            // As the lastIterationPersonReserved is updated with currentIteration,
            // the hashmap may store the reference instead of the value?
            // Regardless, this is to analyse later!
            if(this.lastIterationPersonReserved.get(person.getId()) != currentIteration){
                //Don't want to print for every person! This could be causing the bottlenecks!
                //logger.warn("Person " + person.getId() + " had reservations for a previous iteration. Removing their reservations!\n");

                //This person has reservations, but for a previous iteration
                // need to delete all of their reservations in the slots!

                //Costly way, iterating over ALL slots (there are about 288 slots)
                /*for(Map.Entry<Integer, ReservationSlotV2> entry : reservations.entrySet()){
                    entry.getValue().removePersonReservations(person.getId());
                }*/

                //Efficient way, only tackle the slots that they actually have reservations in (maybe 100 or less instead of 288)
                if(personSlots.containsKey(person.getId())){
                    for(Integer entry : personSlots.get(person.getId())){
                        reservations.get(entry).removePersonReservations(person.getId());
                    }
                }

                //Remove person from person-to-slot
                if(handledRemovals % 1000 == 0){
                    logger.warn("An n thousands person, " + person.getId() + " had reservations for a previous iteration. Removing their reservations!\n");
                }
                handledRemovals++;
                personSlots.remove(person.getId());
            }
        }
    }

    //Only need time and link where reservation is needed
    public void makeReservation(double timeStart, double timeEnd, Link link, Person person){
        //Need to check if person has previously made reservations
        //NOPE this needs to be done in routing module, BEFORE they calculate their new route

        //If they have made reservations for this iteration, all is good

        //If they HAVEN'T made reservations for this iteration, need to clear this person's reservations
        // in all the slots, and then update their lastIterationPersonReserved to the current iteration

        //Add person as having made reservations for this iteration
        this.lastIterationPersonReserved.put(person.getId(), currentIteration);

        int startSlot = (int) (timeStart % timeInterval);
        HashSet<Integer> currentPersonSlots = new HashSet<>();
        for(int i = startSlot; i < ((timeEnd % timeInterval) - (timeStart % timeInterval)); i++){
            //Add slot to person
            currentPersonSlots.add(i);
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

        //This should work, we just care about which slots they are in, not
        // any more detail SHOULD be needed
        if(personSlots.containsKey(person.getId())){
            HashSet<Integer> existingSlots = personSlots.get(person.getId());
            existingSlots.addAll(currentPersonSlots);   //Add the new slots
            this.personSlots.put(person.getId(), existingSlots);
        } else {
            this.personSlots.put(person.getId(),currentPersonSlots);
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
            return reserve.getReservations(link);
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
