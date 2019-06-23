/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.bots;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.bot.params.IBotParams;
import edu.tigers.sumatra.botmanager.basestation.DummyBaseStation;
import edu.tigers.sumatra.ids.BotID;


/**
 * Dummy stub class for bots to prevent using null
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DummyBot extends ABot
{
	private boolean avail2Ai = false;
	
	
	/**
	 * Default constructor.
	 */
	public DummyBot()
	{
		super(EBotType.UNKNOWN, BotID.noBot(), new DummyBaseStation());
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
	
	
	@Override
	public void start()
	{
		// nothing
	}
	
	
	@Override
	public void stop()
	{
		// nothing
	}
	
	
	@Override
	public double getDribblerSpeed()
	{
		return 0;
	}
	
	
	@Override
	public int getHardwareId()
	{
		return getBotId().getNumberWithColorOffsetBS();
	}
	
	
	@Override
	public double getKickerLevel()
	{
		return 0;
	}
	
	
	@Override
	public double getBatteryRelative()
	{
		return 0;
	}
	
	
	@Override
	public ERobotMode getRobotMode()
	{
		return ERobotMode.READY;
	}

	@Override
	public boolean isOK() {
		return true;
	}


	@Override
	public IBotParams getBotParams()
	{
		return new BotParams();
	}
}
