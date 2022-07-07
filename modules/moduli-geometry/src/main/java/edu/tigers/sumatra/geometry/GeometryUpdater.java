/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.cam.ICamFrameObserver;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.model.SumatraModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Update global geometry data structure based on incoming ssl-vision frames.
 */
public class GeometryUpdater extends AModule
{
	private static final Logger log = LogManager.getLogger(GeometryUpdater.class.getName());

	@Configurable(comment = "Receive geometry from SSL vision", defValue = "true")
	private static boolean receiveGeometry = true;

	@Configurable(comment = "Receive geometry from SSL vision, but only once!", defValue = "false")
	private static boolean receiveGeometryOnceOnly = false;

	private boolean geometryReceived = false;
	private final ICamFrameObserver camFrameObserver = new CamFrameObserver();

	static
	{
		ConfigRegistration.registerClass("geom", GeometryUpdater.class);
	}


	@Override
	public void startModule()
	{
		geometryReceived = false;

		try
		{
			ACam cam = SumatraModel.getInstance().getModule(ACam.class);
			cam.addObserver(camFrameObserver);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find cam module", err);
		}
	}


	@Override
	public void stopModule()
	{
		try
		{
			ACam cam = SumatraModel.getInstance().getModule(ACam.class);
			cam.removeObserver(camFrameObserver);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find cam module", err);
		}
	}

	private class CamFrameObserver implements ICamFrameObserver
	{
		@Override
		public void onNewCameraGeometry(final CamGeometry geometry)
		{
			if (receiveGeometry)
			{
				if (!geometryReceived || !receiveGeometryOnceOnly)
				{
					Geometry.update(geometry);
				}

				if (!geometryReceived)
				{
					geometryReceived = true;
					log.info("Received geometry from vision");
				}
			}
		}
	}
}
