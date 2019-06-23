/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.bot.params.IBotParams;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector3;


/**
 * Dummy stub class for bots to prevent using null
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class DummyBot implements IBot
{
	private final BotID				botId;
	private boolean					avail2Ai				= false;
	
	private static final double	KICKER_LEVEL_MAX	= 180;
	private double						relBattery			= 1;
	private double						kickerLevel			= 0;
	
	
	/**
	 * Dummy dummyBot (invalid bot id)
	 */
	public DummyBot()
	{
		this(BotID.noBot());
	}
	
	
	/**
	 * @param botId
	 */
	public DummyBot(final BotID botId)
	{
		this.botId = botId;
	}
	
	
	/**
	 * @param aBot
	 */
	public DummyBot(final IBot aBot)
	{
		botId = aBot.getBotId();
		avail2Ai = aBot.isAvailableToAi();
		relBattery = aBot.getBatteryRelative();
		kickerLevel = aBot.getKickerLevel();
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
	public double getBatteryRelative()
	{
		return relBattery;
	}
	
	
	@Override
	public double getKickerLevel()
	{
		return kickerLevel;
	}
	
	
	@Override
	public double getKickerLevelMax()
	{
		return KICKER_LEVEL_MAX;
	}
	
	
	@Override
	public double getDribblerSpeed()
	{
		return 0;
	}
	
	
	@Override
	public int getHardwareId()
	{
		return 0;
	}
	
	
	@Override
	public EBotType getType()
	{
		return EBotType.UNKNOWN;
	}
	
	
	@Override
	public Map<EFeature, EFeatureState> getBotFeatures()
	{
		return new HashMap<>();
	}
	
	
	@Override
	public String getControlledBy()
	{
		return "";
	}
	
	
	@Override
	public ETeamColor getColor()
	{
		return botId.getTeamColor();
	}
	
	
	@Override
	public boolean isBlocked()
	{
		return false;
	}
	
	
	@Override
	public boolean isHideFromAi()
	{
		return !avail2Ai;
	}
	
	
	@Override
	public boolean isHideFromRcm()
	{
		return true;
	}
	
	
	@Override
	public BotID getBotId()
	{
		return botId;
	}
	
	
	@Override
	public double getCenter2DribblerDist()
	{
		return 75;
	}
	
	
	@Override
	public String getName()
	{
		return "Dummy";
	}
	
	
	@Override
	public Optional<IVector3> getSensoryPos()
	{
		return Optional.empty();
	}
	
	
	@Override
	public Optional<IVector3> getSensoryVel()
	{
		return Optional.empty();
	}
	
	
	@Override
	public boolean isBarrierInterrupted()
	{
		return false;
	}
	
	
	@Override
	public ERobotMode getRobotMode()
	{
		return ERobotMode.READY;
	}
	
	
	@Override
	public IBotParams getBotParams()
	{
		return new BotParams();
	}
}
