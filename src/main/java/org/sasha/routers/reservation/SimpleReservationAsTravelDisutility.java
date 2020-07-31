package org.sasha.routers.reservation;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.vehicles.Vehicle;
import org.sasha.reserver.ReservationManager;

//Doc: https://www.matsim.org/apidocs/core/0.9.0/org/matsim/core/router/util/TravelDisutility.html

/**
 *
 */
public class SimpleReservationAsTravelDisutility implements TravelDisutility {
    private static final Logger LOG = Logger.getLogger(SimpleReservationAsTravelDisutility.class);
    private final TravelTime travelTime;
    private double ReservationDisutilityFactor;
    private final double timeDifferenceFactor;
    private final double flowCapacityFactor;

    public SimpleReservationAsTravelDisutility() {
        this.travelTime = new FreeSpeedTravelTime();
        this.ReservationDisutilityFactor = 100;
        this.flowCapacityFactor = 1;
        this.timeDifferenceFactor = 60;
    }

    public SimpleReservationAsTravelDisutility(double reservationDisutilityFactor, double flowCapacityFactor, double timeDifferenceFactor) {
        this.ReservationDisutilityFactor = reservationDisutilityFactor;
        this.flowCapacityFactor = flowCapacityFactor;
        this.timeDifferenceFactor = timeDifferenceFactor;
        this.travelTime = new FreeSpeedTravelTime();
    }

    //Very simple cost calculator
    //If link has space left for reservations, no cost
    //If there are too many reservations, have cost
    public double calculateRelativeReservedCost(double linkCapacity, double reservedCapacity, Link link){
        if(reservedCapacity - linkCapacity <= 0){
            //Returning 0 here will result in NullPointerExceptions
            return getLinkMinimumTravelDisutility(link);
        } else {
            return getLinkMinimumTravelDisutility(link) + ((reservedCapacity - linkCapacity) * ReservationDisutilityFactor);
        }
    }

    public double calculateSimpleReservedCost(double linkCapacity, double reservedCapacity, Link link){
        if(reservedCapacity - linkCapacity <= 0){
            return getLinkMinimumTravelDisutility(link);
        } else {
            return 30000.0;
        }
    }

    @Override
    public double getLinkTravelDisutility(final Link link, final double time, final Person person, final Vehicle vehicle) {
        //Get link capacity
        double linkCapacity =
                ( 1 / this.flowCapacityFactor) *                                // Take into account the flow capacity factor
                                                                                // However, need to inverse the factor,
                (link.getCapacity(time) / this.timeDifferenceFactor) *          // Take into account the difference in time units
                                                                                // link capacity in vehicles / hour, but we need vehicles / minute
                (ReservationManager.getInstance().getTimeInterval() / 60);      // Take into account the time interval of the slots

        if(linkCapacity != 0){
            LOG.warn("Link has capacity!");
            //Get the current number of reservations for the link
            //double reservedCapacity = this.reservationManager.getInstance().getReservations(time, link);
            double reservedCapacity = ReservationManager.getInstance().getReservations(time, link);
            //double reservedCapacity = 0;

            //Compute cost
            return this.calculateRelativeReservedCost(linkCapacity, reservedCapacity, link);
        } else {
            LOG.warn("Link has no capacity!");
            return this.getLinkMinimumTravelDisutility(link);
        }
    }

    //Taken from TimeAsTravelDisutility from dvrp contrib, which uses the core's FreeSpeedTravelTime
    @Override
    public double getLinkMinimumTravelDisutility(Link link) {
        return link.getLength() / link.getFreespeed();
    }
}
