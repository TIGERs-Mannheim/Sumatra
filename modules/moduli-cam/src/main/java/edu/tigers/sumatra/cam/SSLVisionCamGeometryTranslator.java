/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.cam;

import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.cam.data.CamFieldSize;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.cam.proto.MessagesRobocupSslGeometry.SSL_GeometryData;

import java.util.HashMap;
import java.util.Map;


/**
 * Translate geometry data from protobuf message to our format
 */
public class SSLVisionCamGeometryTranslator
{
	public CamGeometry translate(final SSL_GeometryData geometryData)
	{
		Map<Integer, CamCalibration> calibrations = new HashMap<>();
		geometryData.getCalibList().forEach(calib -> calibrations.put(calib.getCameraId(), new CamCalibration(calib)));

		CamFieldSize fieldSize = new CamFieldSize(geometryData.getField());

		return new CamGeometry(calibrations, fieldSize, geometryData.getModels());
	}
}
