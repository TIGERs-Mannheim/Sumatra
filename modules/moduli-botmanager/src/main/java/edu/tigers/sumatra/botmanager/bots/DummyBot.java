/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.bots;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.botmanager.basestation.DummyBaseStation;
import edu.tigers.sumatra.ids.BotID;


/**
 * Dummy stub class for bots to prevent using null
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DummyBot extends ASimBot
{
	private boolean avail2Ai = false;
	
	
	/**
	 */
	public DummyBot()
	{
		super(EBotType.UNKNOWN, BotID.get(), new DummyBaseStation());
	}
	
	
	/**
	 * @param botId
	 */
	public DummyBot(final BotID botId)
	{
		super(EBotType.UNKNOWN, botId, new DummyBaseStation());
	}
	
	
	/**
	 * @param aBot
	 */
	public DummyBot(final ABot aBot)
	{
		super(aBot, EBotType.UNKNOWN);
	}
	
	
	@Override
	public boolean isAvailableToAi()
	{
		return avail2Ai;
	}
	
	
	/**
	 * @param avail2Ai the avail2Ai to set
	 */
	public final void setAvail2Ai(final boolean avail2Ai)
	{
		this.avail2Ai = avail2Ai;
	}
	
	
	@Override
	public boolean isBarrierInterrupted()
	{
		return false;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public double getCenter2DribblerDist()
	{
		return 90;
	}
}
