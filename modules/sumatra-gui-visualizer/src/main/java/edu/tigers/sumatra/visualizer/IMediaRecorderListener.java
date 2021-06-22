/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer;

public interface IMediaRecorderListener
{
	void setMediaParameters(final int w, final int h, EMediaOption mediaOption);

	void takeScreenshot();

	boolean startRecordingVideo();

	void stopRecordingVideo();
}
