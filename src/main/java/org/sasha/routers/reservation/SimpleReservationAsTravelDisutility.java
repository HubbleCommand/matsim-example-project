package org.sasha.routers.reservation;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.vehicles.Vehicle;
import org.sasha.reserver.ReservationManager;

//Doc: https://www.matsim.org/apidocs/core/0.9.0/org/matsim/core/router/util/TravelDisutility.html
public class SimpleReservationAsTravelDisutility implements TravelDisutility {
    private static final Logger LOG = Logger.getLogger(SimpleReservationAsTravelDisutility.class);
    private ReservationManager reservationManager;

    public SimpleReservationAsTravelDisutility() {
        this.reservationManager = ReservationManager.getInstance();
    }

    //Very simple cost calculator
    //If link has space left for reservations, no cost
    //If there are too many reservations, have cost
    public double calculateRelativeReservedCost(double linkCapacity, double reservedCapacity, Link link){
        if(reservedCapacity - linkCapacity <= 0){
            //Returning 0 here will result in NullPointerExceptions
            return getLinkMinimumTravelDisutility(link);
        } else {
            return getLinkMinimumTravelDisutility(link) + ((reservedCapacity - linkCapacity) * 100);
        }
    }

    @Override
    public double getLinkTravelDisutility(final Link link, final double time, final Person person, final Vehicle vehicle) {
        //Get link capacity
        double linkCapacity = link.getCapacity(time);

        if(linkCapacity != 0){
            LOG.warn("Link has capacity!");
            //Get the current number of reservations for the link
            //double reservedCapacity = this.reservationManager.getInstance().getReservations(time, link);
            double reservedCapacity = ReservationManager.getInstance().getReservations(time, link);
            //double reservedCapacity = 0;

            //Compute cost
            return calculateRelativeReservedCost(linkCapacity, reservedCapacity, link);
        } else {
            LOG.warn("Link has no capacity!");
            return getLinkMinimumTravelDisutility(link);
        }
    }

    @Override
    public double getLinkMinimumTravelDisutility(Link link) {
        return link.getLength() / link.getFreespeed();
    }
}
