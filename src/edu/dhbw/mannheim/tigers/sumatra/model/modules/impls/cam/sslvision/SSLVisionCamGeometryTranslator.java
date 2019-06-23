/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s):
 * Lukas
 * Clemens
 * Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.sslvision;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslGeometry.SSL_GeometryCameraCalibration;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslGeometry.SSL_GeometryData;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamCalibration;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamFieldGeometry;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamGeometryFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamProps;


/**
 * Provides a static conversion-method for the {@link SSL_GeometryData} to wrap the incoming SSL-Vision formats with
 * our own, internal representations
 * 
 * @see edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.SSLVisionCam
 * @author Lukas, Clemens, Gero
 * 
 */
public class SSLVisionCamGeometryTranslator
{
	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param geometryFrame
	 * @param teamProps
	 * @return
	 */
	public CamGeometryFrame translate(SSL_GeometryData geometryFrame, TeamProps teamProps)
	{
		final boolean haveToTurn = teamProps.getPlayLeftToRight();
		
		// --- check if detectionFrame != null ---
		if (geometryFrame == null)
		{
			return null;
		}
		
		// --- new field geometry for new data ---
		final CamFieldGeometry fieldGeometry = new CamFieldGeometry(geometryFrame.getField());
		
		
		// --- new calibration-list for new data ---
		final List<CamCalibration> calibration = new ArrayList<CamCalibration>();
		for (final SSL_GeometryCameraCalibration cc : geometryFrame.getCalibList())
		{
			if (haveToTurn)
			{
				final CamCalibration newCC = new CamCalibration(cc.getCameraId(), cc.getFocalLength(),
				// TODO Gero: Have to turn?? (Gero)
						cc.getPrincipalPointX(), cc.getPrincipalPointY(), cc.getDistortion(),
						
						// TODO Gero: Have to turn?? (Gero)
						cc.getQ0(), cc.getQ1(), cc.getQ2(), cc.getQ3(),
						
						// TODO Gero: Have to turn?? (Gero)
						cc.getTx(), cc.getTy(), cc.getTz(),
						
						// Turn
						-cc.getDerivedCameraWorldTx(), -cc.getDerivedCameraWorldTy(), cc.getDerivedCameraWorldTz());
				calibration.add(newCC);
			} else
			{
				calibration.add(new CamCalibration(cc));
			}
		}
		
		return new CamGeometryFrame(fieldGeometry, calibration);
	}
	
}
