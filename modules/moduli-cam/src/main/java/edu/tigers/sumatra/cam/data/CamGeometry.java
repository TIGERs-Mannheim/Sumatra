/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam.data;

import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_GeometryModels;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;


/**
 * Geometry information of SSL vision
 */
@Data
@AllArgsConstructor
public class CamGeometry
{
	private final Map<Integer, CamCalibration> calibrations;
	private CamFieldSize field;
	private SSL_GeometryModels camBallModels;


	/**
	 * Update geometry.
	 *
	 * @param update
	 */
	public void update(final CamGeometry update)
	{
		calibrations.putAll(update.getCalibrations());
		field = update.getField();
		camBallModels = update.camBallModels;
	}
}
