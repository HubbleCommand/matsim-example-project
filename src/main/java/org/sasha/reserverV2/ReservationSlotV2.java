package org.sasha.reserverV2;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

import java.util.*;

public class ReservationSlotV2 {
    //private HashMap<Id<Link>, ArrayList<Id<Person>>> reservations2people;
    //Change to HashSet!
    private HashMap<Id<Link>, HashSet<Id<Person>>> reservations2people;
    private HashMap<Id<Link>, Integer> reservations; //(instead of reservations2people)
    /*TODO go back to Id, Integer HashMap:
        When go to delete a reservation, do:
        for(Id<Link> linkEntry : personToReservedLinks.get(person).entrySet()){
                Integer currentCount = reservations.get(linkEntry);
                newCount = currentCount - 1;
                reservations.put(linkEntry, newCount);
        }
    */


    //This will make it quicker at deletion, as we will know which links
    // the person has reservations on!
    private HashMap<Id<Person>, HashSet<Id<Link>>> personToReservedLinks;

    public ReservationSlotV2(){
        reservations2people = new HashMap<>();
        //personToReservedLinks = new HashMap<>(25000);
        personToReservedLinks = new HashMap<>();
        reservations = new HashMap<>();
    }

    public ReservationSlotV2(Link link, Id<Person> personId){
        //reservations2people = new HashMap<>(){{put(link.getId(), new ArrayList<>(){{add(personId);}});}};
        reservations2people = new HashMap<>(){{put(link.getId(), new HashSet<>(){{add(personId);}});}};
        personToReservedLinks = new HashMap<>(){{put(personId, new HashSet<>(){{add(link.getId());}});}};
    }

    //Removes all the reservations made by a specific person
    public void removePersonReservations(Id<Person> person){
        //V1 inefficient, especially when lots of people have reservations to clear! (need to go over a lot of links)
        //for(Map.Entry<Id<Link>, ArrayList<Id<Person>>> entry : reservations2people.entrySet()){
        /*for(Map.Entry<Id<Link>, HashSet<Id<Person>>> entry : reservations2people.entrySet()){
            //entry.getValue().remove(person);  //This will only remove the first instace... even though it shouldn't pose a problem
            entry.getValue().removeAll(Arrays.asList(person));
        }*/

        //V2 more efficient, instead of iterating over ALL the links that have reservations,
        // we will simple look at the links that the person has reservations for
        //For each link that they have reservations for
        HashSet<Id<Link>> linksPersonHasReservationsOn = personToReservedLinks.get(person);

        if(linksPersonHasReservationsOn != null){
            for(Id<Link> linkEntry : linksPersonHasReservationsOn){
                //Remove the current person's reservations
                HashSet<Id<Person>> existingReservationsForLink = reservations2people.get(linkEntry);
                //removeAll is a safety measure to ensure that the person's potentially multiple reservations are removed
                // however this shouldn't happen. A List could have been used, but HashSet have better performance
                // with larger sets of data
                existingReservationsForLink.removeAll(Arrays.asList(person));
                reservations2people.put(linkEntry, existingReservationsForLink);
            }
        }

        /*for(Map.Entry<Id<Person>, HashSet<Id<Link>>> entry : personToReservedLinks.entrySet()){
            //Clear the reservations!
            HashSet<Id<Person>> existingReservationsForLink = reservations2people.get(entry.getValue());
            existingReservationsForLink.removeAll(Arrays.asList(person));
            reservations2people.put()
        }*/
        //Can then clear the person's entry in person To Reserved Links
        personToReservedLinks.remove(person);
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
            /*ArrayList<Id<Person>> newList = new ArrayList(reservations2people.get(link.getId()));
            newList.add(person);
            reservations2people.put(link.getId(), newList);*/
            HashSet<Id<Person>> newList = new HashSet(reservations2people.get(link.getId()));
            newList.add(person);
            reservations2people.put(link.getId(), newList);
        } else {
            //reservations2people.put(link.getId(), new ArrayList<>(){{add(person);}});
            reservations2people.put(link.getId(), new HashSet<>(){{add(person);}});
        }

        //Update person to links reservations
        if(personToReservedLinks.containsKey(person)){
            HashSet<Id<Link>> newList = new HashSet<>(personToReservedLinks.get(person));
            newList.add(link.getId());
            personToReservedLinks.put(person, newList);
        } else {
            personToReservedLinks.put(person, new HashSet<>(){{add(link.getId());}});
        }
    }

    @Override
    public String toString() {
        return reservations2people.toString();
    }
}
