package org.sasha.routers.reservation;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.vehicles.Vehicle;
import org.sasha.reserver.ReservationManager;

//Doc: https://www.matsim.org/apidocs/core/0.9.0/org/matsim/core/router/util/TravelDisutility.html
public class SimpleReservationAsTravelDisutility implements TravelDisutility {
    private ReservationManager reservationManager;

    public SimpleReservationAsTravelDisutility() {
        this.reservationManager = ReservationManager.getInstance();
    }

    public SimpleReservationAsTravelDisutility(ReservationManager reservationManager) {
        //this.reservationManager = reservationManager.getInstance();
        this.reservationManager = ReservationManager.getInstance();
    }

    //TODO put these different cost calculations in different classes, that is basically the design pattern here
    //Very simple cost calculator
    //If link has space left for reservations, no cost
    //If there are too many reservations, have cost
    public double calculateSimpleRelativeReservedCost(double linkCapacity, double reservedCapacity){
        if(linkCapacity > reservedCapacity){
            return 0;
        } else {
            return (reservedCapacity - linkCapacity) * 100;
        }
    }

    public double calculateAdvancedRelativeReservedCost(double linkCapacity, double reservedCapacity){
        return Math.abs(reservedCapacity - linkCapacity) * 100;
    }

    @Override
    public double getLinkTravelDisutility(final Link link, final double time, final Person person, final Vehicle vehicle) {
        //Get link capacity
        double linkCapacity = link.getCapacity(time);

        //Get the current number of reservations for the link
        //double reservedCapacity = this.reservationManager.getInstance().getReservations(time, link);
        double reservedCapacity = ReservationManager.getInstance().getReservations(time, link);
        //double reservedCapacity = 0;
        //Compute cost
        return calculateSimpleRelativeReservedCost(linkCapacity, reservedCapacity);
    }

    @Override
    public double getLinkMinimumTravelDisutility(Link link) {
        return link.getLength() / link.getFreespeed();
    }
}
