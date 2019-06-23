/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 19, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.cam;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import edu.tigers.sumatra.MessagesRobocupSslGeometry.SSL_GeometryData;
import edu.tigers.sumatra.cam.data.CamCalibration;
import edu.tigers.sumatra.cam.data.CamFieldSize;
import edu.tigers.sumatra.cam.data.CamGeometry;


/**
 * Translate geometry data from protobuf message to our format
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SSLVisionCamGeometryTranslator
{
	/**
	 * @param geometryData
	 * @return
	 */
	public CamGeometry translate(final SSL_GeometryData geometryData)
	{
		Map<Integer, CamCalibration> calibrations = new HashMap<>();
		geometryData.getCalibList().stream()
				.filter(c -> c.getDistortion() > 0.1).collect(Collectors.toList())
				.forEach(calib -> calibrations.put(calib.getCameraId(), new CamCalibration(calib)));
		
		CamFieldSize fieldSize = new CamFieldSize(geometryData.getField());
		
		return new CamGeometry(calibrations, fieldSize);
	}
}
