/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam.data;

import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_GeometryModels;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Map;


/**
 * Geometry information of SSL vision
 */
@Value
@Builder(toBuilder = true)
public class CamGeometry
{
	@Singular
	Map<Integer, CamCalibration> cameraCalibrations;
	CamFieldSize fieldSize;
	@Builder.Default
	SSL_GeometryModels ballModels = SSL_GeometryModels.newBuilder().build();


	/**
	 * Merge geometry with an update and return result.
	 *
	 * @param update
	 * @return merge result
	 */
	public CamGeometry merge(CamGeometry update)
	{
		return toBuilder()
				.cameraCalibrations(update.getCameraCalibrations())
				.fieldSize(update.getFieldSize())
				.ballModels(update.getBallModels())
				.build();
	}
}
