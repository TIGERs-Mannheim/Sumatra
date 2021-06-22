/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes;

import edu.tigers.sumatra.gamelog.proto.LogLabels;

public interface IEventType {


    /**
	 * Add this event type to the protobuf message log labels
	 *
	 * @param labelsBuilder
	 * @param frameId
	 */
	void addEventTypeTo(LogLabels.Labels.Builder labelsBuilder, final int frameId);

}
