package edu.tigers.sumatra.paramoptimizer;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.paramoptimizer.redirect.RedirectDetector;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 16, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ParameterOptimizer extends AModule implements IWorldFrameObserver
{
	@SuppressWarnings("unused")
	private static final Logger	log					= Logger.getLogger(ParameterOptimizer.class.getName());
	
	/** */
	public static final String		MODULE_TYPE			= "ParameterOptimizer";
	/** */
	public static final String		MODULE_ID			= "ParameterOptimizer";
	
	@SuppressWarnings("unused")
	private RedirectDetector		redirectDetector	= new RedirectDetector();
	
	
	/**
	 * @param config
	 */
	public ParameterOptimizer(final SubnodeConfiguration config)
	{
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		// SimpleWorldFrame swf = wFrameWrapper.getSimpleWorldFrame();
		
		// Optional<RedirectDataSet> rds = redirectDetector.process(swf);
		// if (rds.isPresent())
		{
			// RedirectParamCalc.forBot(rds.get().getBot()).update(rds.get());
		}
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
	}
	
	
	@Override
	public void deinitModule()
	{
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addWorldFrameConsumer(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find WP module", e);
		}
	}
	
	
	@Override
	public void stopModule()
	{
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.removeWorldFrameConsumer(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find WP module", e);
		}
	}
}
