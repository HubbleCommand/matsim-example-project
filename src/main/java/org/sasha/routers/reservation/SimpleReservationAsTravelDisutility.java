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

    private int timesCalled = 0;

    public SimpleReservationAsTravelDisutility() {
        this.travelTime = new FreeSpeedTravelTime();
        this.ReservationDisutilityFactor = 1000;
        this.flowCapacityFactor = 1;
        this.timeDifferenceFactor = 60;
    }

    public SimpleReservationAsTravelDisutility(double reservationDisutilityFactor, double flowCapacityFactor, double timeDifferenceFactor) {
        this.ReservationDisutilityFactor = reservationDisutilityFactor;
        this.flowCapacityFactor = flowCapacityFactor;
        this.timeDifferenceFactor = timeDifferenceFactor;
        this.travelTime = new FreeSpeedTravelTime();
    }

    /*
        Very simple cost calculator
        If link has space left for reservations, default cost
        If there are too many reservations, default cost plus weighted
        We can do this as MATSim works on a mesoscopic level, and detailed vehicle interactions are not
        simulated. This means that vehicles will only affect each other when there is no free reservable
        capacity on the link and that they are physically blocking each other from progressing on the link.
     */
    public double calculateRelativeReservedCost(double linkCapacity, double reservedCapacity, Link link){
        if(reservedCapacity - linkCapacity <= 0){
            //Returning 0 here will result in NullPointerExceptions
            return getLinkMinimumTravelDisutility(link);
        } else {
            // For something more fine-tunable, use + instead of *
            //getLinkMinimumTravelDisutility(link) + ((reservedCapacity - linkCapacity + 1) * ReservationDisutilityFactor)

            //Or can also use
            //(getLinkMinimumTravelDisutility(link) * ReservationDisutilityFactor) + ((reservedCapacity - linkCapacity + 1) * ReservationDisutilityFactor)
            return getLinkMinimumTravelDisutility(link) * ((reservedCapacity - linkCapacity + 1) * ReservationDisutilityFactor);
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
        //TODO check if Link.getCapacity(Time) is already normalized to the flow factors!
        //might be available throuth link.getFlowCapacityPerSec(time)
        //will regardless need to take into account timeDifferenceFactor, as this normalizes time difference to time slot interval
        double linkCapacity =
                ( 1 / this.flowCapacityFactor) *                                // Take into account the flow capacity factor
                                                                                // However, need to inverse the factor,
                (link.getCapacity(time) / this.timeDifferenceFactor) *          // Take into account the difference in time units
                                                                                // link capacity in vehicles / hour, but we need vehicles / minute
                (ReservationManager.getInstance().getTimeInterval() / 60);      // Take into account the time interval of the slots

        timesCalled += 1;
        if(linkCapacity != 0){
            //Get the current number of reservations for the link
            double reservedCapacity = ReservationManager.getInstance().getReservations(time, link);

            if(timesCalled % 100000 == 0){
                LOG.warn("Link : " + link.getId() + " at time : " + time + " has reserved : " + reservedCapacity + "\n");
            }

            //Compute cost
            return this.calculateRelativeReservedCost(linkCapacity, reservedCapacity, link);
        } else {
            return this.getLinkMinimumTravelDisutility(link);
        }
    }

    //Taken from TimeAsTravelDisutility from dvrp contrib, which uses the core's FreeSpeedTravelTime
    @Override
    public double getLinkMinimumTravelDisutility(Link link) {
        return link.getLength() / link.getFreespeed();
    }
}
