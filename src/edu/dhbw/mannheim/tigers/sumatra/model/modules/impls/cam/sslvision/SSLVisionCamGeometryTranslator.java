/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 19, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision;

import java.util.HashMap;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslGeometry.SSL_GeometryData;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamCalibration;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamFieldSize;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamGeometry;


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
		for (int i = 0; i < geometryData.getCalibCount(); i++)
		{
			CamCalibration calibration = new CamCalibration(geometryData.getCalib(i));
			calibrations.put(calibration.getCameraId(), calibration);
		}
		CamFieldSize fieldSize = new CamFieldSize(geometryData.getField());
		return new CamGeometry(calibrations, fieldSize);
	}
}
