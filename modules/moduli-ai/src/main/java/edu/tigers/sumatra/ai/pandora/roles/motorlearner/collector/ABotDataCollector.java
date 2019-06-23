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
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.BotManager;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.basestation.IBaseStationObserver;
import edu.tigers.sumatra.export.INumberListable;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @param <ResultType>
 */
public abstract class ABotDataCollector<ResultType extends INumberListable> extends ADataCollector<ResultType>
		implements IBaseStationObserver
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ABotDataCollector.class.getName());
	
	
	/**
	 * @param type
	 */
	protected ABotDataCollector(final EDataCollector type)
	{
		super(type);
	}
	
	
	@Override
	public void start()
	{
		super.start();
		try
		{
			BotManager bm = (BotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
			for (IBaseStation ibs : bm.getBaseStations())
			{
				ibs.addObserver(this);
			}
		} catch (ModuleNotFoundException err)
		{
			log.warn("Could not get BotManager");
		}
	}
	
	
	@Override
	public void stop()
	{
		super.stop();
		try
		{
			BotManager bm = (BotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
			for (IBaseStation ibs : bm.getBaseStations())
			{
				ibs.removeObserver(this);
			}
		} catch (ModuleNotFoundException err)
		{
			log.warn("Could not get BotManager");
		}
	}
	
}
