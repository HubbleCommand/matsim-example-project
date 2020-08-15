package org.sasha.writers;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.internal.MatsimWriter;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.io.MatsimXmlWriter;
import org.sasha.reserver.ReservationManager;
import org.sasha.reserver.ReservationSlot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationWriter extends MatsimXmlWriter implements MatsimWriter {
    public ReservationWriter(){

    }

    private void writeInit() {
        this.writeXmlHead();
        this.writeDoctype("reservations", "");
    }

    private void writeReservations(){
        /*
        * For each slot
        *   For each link
        *       Write number of reservations
        *
        * */
    }

    private void writeFinish() {
        try {
            writeEndTag("reservations");
            this.writer.flush();
            this.writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(String filename) {
        openFile(filename);
        this.writeInit();
        this.writeReservations();
        this.writeFinish();
    }
}
