/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movementlimits;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.AWorldPredictor;


/**
 * Module for observing bot movement and storing maximum values in a database.
 */
public class MovementObserverModule extends AModule
{
	private static final Logger log = LogManager.getLogger(MovementObserverModule.class);
	private MovementObserver observer;


	@Override
	public void startModule()
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
