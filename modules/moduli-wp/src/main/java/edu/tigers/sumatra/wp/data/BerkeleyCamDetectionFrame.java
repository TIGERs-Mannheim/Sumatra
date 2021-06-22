/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp.data;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;


/**
 * Entity for vision data
 */
@Entity
@Data
@RequiredArgsConstructor
public class BerkeleyCamDetectionFrame
{
	@PrimaryKey
	private final long timestamp;

	private final Map<Integer, ExtendedCamDetectionFrame> camFrames;


	@SuppressWarnings("unused")
	private BerkeleyCamDetectionFrame()
	{
		timestamp = 0;
		camFrames = null;
	}
}
