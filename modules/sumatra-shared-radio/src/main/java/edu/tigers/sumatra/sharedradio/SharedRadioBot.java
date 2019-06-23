/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.sharedradio;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.bot.params.IBotParams;
import edu.tigers.sumatra.botmanager.bots.ASimBot;
import edu.tigers.sumatra.ids.BotID;


/**
 * This class is currently BROKEN!
 * BotSkills are being re-organized.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class SharedRadioBot extends ASimBot
{
	private final SharedRadioBaseStation baseStation;
	
	
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
		// setFeedbackDelay(0.17);
	}
	
	
	@Override
	public void sendMatchCommand()
	{
		super.sendMatchCommand();
		baseStation.sendMatchCommand(this);
	}
	
	
	@Override
	public double getDribblerSpeed()
	{
		return 0;
	}
	
	
	@Override
	public IBotParams getBotParams()
	{
		return new BotParams();
	}
	
}
