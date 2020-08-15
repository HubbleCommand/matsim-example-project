package org.sasha.routers.reservation;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.vehicles.Vehicle;
import org.sasha.reserverV2.ReservationManagerV2;

//Doc: https://www.matsim.org/apidocs/core/0.9.0/org/matsim/core/router/util/TravelDisutility.html

/**
 *
 */
public class SimpleReservationAsTravelDisutility implements TravelDisutility {
    private static final Logger LOG = Logger.getLogger(SimpleReservationAsTravelDisutility.class);
    private final TravelTime travelTime;
    private final double ReservationDisutilityFactor;
    private final double FullReservationDisutilityFactor;
    private final double timeDifferenceFactor;
    private final double flowCapacityFactor;

    private int timesCalled = 0;

    public SimpleReservationAsTravelDisutility() {
        this.travelTime = new FreeSpeedTravelTime();
        this.FullReservationDisutilityFactor = 100;
        this.ReservationDisutilityFactor = 100;
        this.flowCapacityFactor = 1;
        this.timeDifferenceFactor = 60;
    }

    public SimpleReservationAsTravelDisutility(double reservationDisutilityFactor, double fullReservationDisutilityFactor, double flowCapacityFactor, double timeDifferenceFactor) {
        this.ReservationDisutilityFactor = reservationDisutilityFactor;
        this.FullReservationDisutilityFactor = fullReservationDisutilityFactor;
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
        //STRICTLY smaller than zero, if zero then costs NEED TO BE INCURRED
        if(reservedCapacity - linkCapacity < 0){
            //Returning 0 here will result in NullPointerExceptions
            //return getLinkMinimumTravelDisutility(link);

            //This is what I want to use, but routes don't get calculated. Could it be returning Zero?
            //return (getLinkMinimumTravelDisutility(link) + ((reservedCapacity - linkCapacity + 1) * ReservationDisutilityFactor));

            //It was returning a negative cost!! Duh!
            return (getLinkMinimumTravelDisutility(link) + ((reservedCapacity / linkCapacity) * ReservationDisutilityFactor));
        } else {
            // For something more fine-tunable, use + instead of *
            //getLinkMinimumTravelDisutility(link) + ((reservedCapacity - linkCapacity + 1) * ReservationDisutilityFactor)

            //Or can also use
            //(getLinkMinimumTravelDisutility(link) * ReservationDisutilityFactor) + ((reservedCapacity - linkCapacity + 1) * ReservationDisutilityFactor)
            return (getLinkMinimumTravelDisutility(link) * ((reservedCapacity - linkCapacity + 1) * FullReservationDisutilityFactor));
        }
    }

    //WARN THIS IS NOT WORKING. FOR SOME REASON WON'T CALCULATE ANY ROUTES!!!!!
    public double calculateAdvancedReservedCost(double linkCapacity, double reservedCapacity, Link link){
        double linkTravelDisutility = getLinkMinimumTravelDisutility(link) + ((reservedCapacity / linkCapacity) * ReservationDisutilityFactor);

        if(linkTravelDisutility < 0){
            return 0;
        } else {
            return linkTravelDisutility;
        }

        //return getLinkMinimumTravelDisutility(link) * ((reservedCapacity - linkCapacity + 1) * ReservationDisutilityFactor);
        /*if(reservedCapacity - linkCapacity < 0){
            //Returning 0 here will result in NullPointerExceptions
            return getLinkMinimumTravelDisutility(link) + ((reservedCapacity - linkCapacity + 1) * ReservationDisutilityFactor);
        } else {
            // For something more fine-tunable, use + instead of *
            //getLinkMinimumTravelDisutility(link) + ((reservedCapacity - linkCapacity + 1) * ReservationDisutilityFactor)

            //Or can also use
            //(getLinkMinimumTravelDisutility(link) * ReservationDisutilityFactor) + ((reservedCapacity - linkCapacity + 1) * ReservationDisutilityFactor)
            return getLinkMinimumTravelDisutility(link) + ((reservedCapacity - linkCapacity + 1) * FullReservationDisutilityFactor);
        }*/
    }

    @Override
    public double getLinkTravelDisutility(final Link link, final double time, final Person person, final Vehicle vehicle) {
        //Get link capacity
        //TODO check if Link.getCapacity(Time) is already normalized to the flow factors!
        //might be available through link.getFlowCapacityPerSec(time)
        //will regardless need to take into account timeDifferenceFactor, as this normalizes time difference to time slot interval
        double linkCapacity =

                //TODO update the capacity factor!!!!!!!!
                //Fuck I think this is wrong as well...
                this.flowCapacityFactor *
                //Do I even need to do this? I don't think so, it was fine when it was 1!
                //( 1 / this.flowCapacityFactor) *                                // Take into account the flow capacity factor
                                                                                // However, need to inverse the factor,
                (link.getCapacity(time) / 60) *          // Take into account the difference in time units
                                                                                // link capacity in vehicles / hour, but we need vehicles / minute
                (ReservationManagerV2.getInstance().getTimeInterval() / 60);    // Take into account the time interval of the slots

        //linkCapacity = Math.round(linkCapacity * 100.0) / 100.0;
        linkCapacity = Math.round(linkCapacity);

        timesCalled += 1;
        if(linkCapacity != 0){
            //Get the current number of reservations for the link
            double reservedCapacity = ReservationManagerV2.getInstance().getReservations(time, link);

            //One direction on the Pont du Mont Blanc
            if(timesCalled % 100000 == 0 /*&& link.getId().equals("83006")*/ && reservedCapacity > 0){
                //LOG.warn("Link : " + link.getId() + " at time : " + time + " has reserved : " + reservedCapacity + " out of normalized cap. : " + linkCapacity + " out of defined xml cap. :" + link.getCapacity(time) + "\n");
            }

            //Compute cost
            return this.calculateAdvancedReservedCost(linkCapacity, reservedCapacity, link);
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
