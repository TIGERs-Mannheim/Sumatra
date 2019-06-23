/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 22, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.sharedradio;

import com.github.g3force.configurable.Configurable;
import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.botmanager.bots.ASimBot;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class SharedRadioBot extends ASimBot
{
	
	private final SharedRadioBaseStation	baseStation;
														
	@Configurable
	private static double						center2DribblerDist	= 75;
																					
																					
	@SuppressWarnings("unused")
	private SharedRadioBot()
	{
		super();
		baseStation = null;
	}
	
	
	/**
	 * @param botId
	 * @param baseStation
	 */
	public SharedRadioBot(final BotID botId, final SharedRadioBaseStation baseStation)
	{
		super(EBotType.SHARED_RADIO, botId, baseStation);
		this.baseStation = baseStation;
	}
	
	
	@Override
	public void sendMatchCommand()
	{
		super.sendMatchCommand();
		baseStation.sendMatchCommand(this);
	}
	
	
	@Override
	protected double getFeedbackDelay()
	{
		return 0.17;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public double getCenter2DribblerDist()
	{
		return center2DribblerDist;
	}
	
}
