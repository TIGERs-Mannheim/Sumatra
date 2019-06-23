/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.paramoptimizer;

import org.apache.log4j.Logger;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.paramoptimizer.redirect.RedirectDetector;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ParameterOptimizer extends AModule implements IWorldFrameObserver
{
	@SuppressWarnings("unused")
	private static final Logger	log					= Logger.getLogger(ParameterOptimizer.class.getName());
	
	@SuppressWarnings("unused")
	private RedirectDetector		redirectDetector	= new RedirectDetector();
	
	
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
		try
		{
			AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
			wp.addObserver(this);
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
			AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
			wp.removeObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find WP module", e);
		}
	}
}
