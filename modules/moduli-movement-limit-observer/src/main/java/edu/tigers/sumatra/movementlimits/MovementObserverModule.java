/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movementlimits;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.AWorldPredictor;
import lombok.extern.log4j.Log4j2;


/**
 * Module for observing bot movement and storing maximum values in a database.
 */
@Log4j2
public class MovementObserverModule extends AModule
{
	private MovementObserver observer;
	private KickSpeedObserver kickSpeedObserver = new KickSpeedObserver();


	@Override
	public void startModule()
	{
		kickSpeedObserver.start();
		try
		{
			BotParamsManager manager = SumatraModel.getInstance().getModule(BotParamsManager.class);
			observer = new MovementObserver(manager);
			AWorldPredictor predictor = SumatraModel.getInstance().getModule(AWorldPredictor.class);
			predictor.addObserver(observer);
			predictor.addObserver(kickSpeedObserver);
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
			predictor.removeObserver(kickSpeedObserver);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find module", e);
		}
		kickSpeedObserver.stop();
	}
}
