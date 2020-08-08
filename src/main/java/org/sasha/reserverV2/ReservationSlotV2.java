package org.sasha.reserverV2;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ReservationSlotV2 {
    private HashMap<Id<Link>, ArrayList<Id<Person>>> reservations2people;

    public ReservationSlotV2(){
        reservations2people = new HashMap<>();
    }

    public ReservationSlotV2(Link link, Id<Person> personId){
        reservations2people = new HashMap<>(){{put(link.getId(), new ArrayList<>(){{add(personId);}});}};
    }

    //Removes all the reservations made by a specific person
    public void removePersonReservations(Id<Person> person){
        for(Map.Entry<Id<Link>, ArrayList<Id<Person>>> entry : reservations2people.entrySet()){
            entry.getValue().remove(person);
        }
    }

    public int getReservationsSum(){
        return reservations2people.values().stream().mapToInt(x -> x.size()).sum();
    }

    public int getReservations(Link link){
        if(link == null){
            return 0;
        } else {
            if(reservations2people == null){
                return 0;
            } else {
                if(reservations2people.containsKey(link.getId())){
                    int reservationsForLink = reservations2people.get(link.getId()).size();
                    return reservationsForLink;
                } else {
                    return 0;
                }
            }
        }
    }

    public void makeReservation(Link link, Id<Person> person){
        if(reservations2people.containsKey(link.getId())){
            ArrayList<Id<Person>> newList = new ArrayList(reservations2people.get(link.getId()));
            newList.add(person);
            reservations2people.put(link.getId(), newList);
        } else {
            reservations2people.put(link.getId(), new ArrayList<>(){{add(person);}});
        }
    }

    @Override
    public String toString() {
        return reservations2people.toString();
    }
}
