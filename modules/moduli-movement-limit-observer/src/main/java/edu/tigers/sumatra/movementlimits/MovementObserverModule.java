/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movementlimits;

import org.apache.log4j.Logger;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.AWorldPredictor;


/**
 * Module for observing bot movement and storing maximum values in a database.
 *
 * @author Dominik Engelhardt
 */
public class MovementObserverModule extends AModule
{
	private static final Logger log = Logger.getLogger(MovementObserverModule.class);
	private MovementObserver observer;
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		// nothing to do here
	}
	
	
	@Override
	public void deinitModule()
	{
		// nothing to do here
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		try
		{
			BotParamsManager manager = SumatraModel.getInstance().getModule(BotParamsManager.class);
			observer = new MovementObserver(manager);
			AWorldPredictor predictor = SumatraModel.getInstance().getModule(AWorldPredictor.class);
			predictor.addObserver(observer);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find module", e);
		}
	}
	
	
	@Override
	public void stopModule()
	{
		try
		{
			AWorldPredictor predictor = SumatraModel.getInstance().getModule(AWorldPredictor.class);
			predictor.removeObserver(observer);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find module", e);
		}
	}
}
