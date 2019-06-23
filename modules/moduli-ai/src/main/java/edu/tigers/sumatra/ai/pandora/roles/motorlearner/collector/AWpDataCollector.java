/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.motorlearner.collector;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.export.INumberListable;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <ResultType>
 */
public abstract class AWpDataCollector<ResultType extends INumberListable> extends ADataCollector<ResultType> implements
		IWorldFrameObserver
{
	@SuppressWarnings("unused")
	private static final Logger	log	= Logger.getLogger(AWpDataCollector.class.getName());
	
	
	/**
	 * @param type
	 */
	protected AWpDataCollector(final EDataCollector type)
	{
		super(type);
	}
	
	
	@Override
	public final void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		onNewWorldFrameWrapper(wFrameWrapper);
	}
	
	
	protected abstract void onNewWorldFrameWrapper(WorldFrameWrapper wfw);
	
	
	@Override
	public void start()
	{
		super.start();
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.warn("Could not get WP");
		}
	}
	
	
	@Override
	public void stop()
	{
		super.stop();
		try
		{
			AWorldPredictor wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.removeWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.warn("Could not get WP");
		}
	}
	
}
