/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.cam.ICamFrameObserver;
import edu.tigers.sumatra.cam.data.CamGeometry;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class GeometryUpdater extends AModule
{
	
	@SuppressWarnings("unused")
	private static final Logger		log							= Logger.getLogger(GeometryUpdater.class.getName());
	
	/** */
	public static final String			MODULE_TYPE					= "GeometryUpdater";
	/** */
	public static final String			MODULE_ID					= "geometryUpdater";
	
	@Configurable(comment = "Receive geometry from SSL vision")
	private static boolean				receiveGeometry			= true;
	
	@Configurable(comment = "Receive geometry from SSL vision, but only once!")
	private static boolean				receiveGeometryOnceOnly	= false;
	
	private boolean						geometryReceived			= false;
	private final ICamFrameObserver	camFrameObserver			= new CamFrameObserver();
	
	static
	{
		ConfigRegistration.registerClass("geom", GeometryUpdater.class);
	}
	
	
	/**
	 * @param config
	 */
	public GeometryUpdater(SubnodeConfiguration config)
	{
		// empty
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		// empty
	}
	
	
	@Override
	public void deinitModule()
	{
		// empty
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		geometryReceived = false;
		
		try
		{
			ACam cam = (ACam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
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
			ACam cam = (ACam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
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
					Geometry.setCamDetection(geometry);
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
