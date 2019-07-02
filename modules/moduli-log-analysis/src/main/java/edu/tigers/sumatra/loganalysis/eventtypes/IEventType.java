/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes;

import edu.tigers.sumatra.labeler.LogLabels;

public interface IEventType {


    /**
     * Add this event type to the protobuf message log labels
     * @param labelsBuilder
     */
    void addEventTypeTo(LogLabels.Labels.Builder labelsBuilder);

}
