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

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamCalibration;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamFieldGeometry;
import edu.dhbw.mannheim.tigers.sumatra.model.data.CamGeometryFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslGeometry.SSL_GeometryCameraCalibration;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslGeometry.SSL_GeometryData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.SSLVisionCam;


/**
 * Provides a static conversion-method for the {@link SSL_GeometryData} to wrap the incoming SSL-Vision formats with
 * our own, internal representations
 * 
 * @see SSLVisionCam
 * @author Lukas, Clemens, Gero
 * 
 */
public class SSLVisionCamGeometryTranslator
{
	
	// ---------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final boolean	haveToTurn	= SumatraModel.getInstance().getGlobalConfiguration()
																	.getString("ourGameDirection").equalsIgnoreCase("leftToRight");
	
	
	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public static CamGeometryFrame translate(SSL_GeometryData geometryFrame)
	{
		// --- check if detectionFrame != null ---
		if (geometryFrame == null)
		{
			return null;
		}
		
		// --- new field geometry for new data ---
		CamFieldGeometry fieldGeometry = new CamFieldGeometry(geometryFrame.getField());
		

		// --- new calibration-list for new data ---
		List<CamCalibration> calibration = new ArrayList<CamCalibration>();
		for (SSL_GeometryCameraCalibration cc : geometryFrame.getCalibList())
		{
			if (haveToTurn)
			{
				CamCalibration newCC = new CamCalibration(cc.getCameraId(), cc.getFocalLength(),
						// TODO Gero: Have to turn?? (Gero)
						cc.getPrincipalPointX(), cc.getPrincipalPointY(),
						cc.getDistortion(),

						// TODO Gero: Have to turn?? (Gero)
						cc.getQ0(), cc.getQ1(), cc.getQ2(), cc.getQ3(),

						// TODO Gero: Have to turn?? (Gero)
						cc.getTx(), cc.getTy(), cc.getTz(),
						
						// Turn
						-cc.getDerivedCameraWorldTx(), -cc.getDerivedCameraWorldTy(),
						cc.getDerivedCameraWorldTz());
				calibration.add(newCC);
			} else
			{
				calibration.add(new CamCalibration(cc));
			}
		}
		
		return new CamGeometryFrame(fieldGeometry, calibration);
	}
	
}
