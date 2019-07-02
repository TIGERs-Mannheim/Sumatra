/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes;

/**
 * This class describes a abstract Event Type, that can be detected from a log file
 */
public interface IEventTypeDetection<T extends IEventType>
{

    /**
     * This abstract methods provides the next frame for the detection of the event type
     * @param frame the given frame for detection
     */
     void nextFrameForDetection(TypeDetectionFrame frame);

    /**
     * this method sets the detection to init state
     */
    void resetDetection();

    /**
     * Getter method for the detected event type
     * @return detected event type
     */
     T getDetectedEventType();
}
