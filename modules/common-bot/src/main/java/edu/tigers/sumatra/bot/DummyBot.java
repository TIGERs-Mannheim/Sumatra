/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.bot;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector3;


/**
 * Dummy stub class for bots to prevent using null
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class DummyBot implements IBot
{
	private boolean					avail2Ai				= false;
	private final BotID				botId;
	private final MoveConstraints	moveConstraints	= new MoveConstraints();
	
	
	/**
	 */
	public DummyBot()
	{
		this(BotID.get());
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
		this(aBot.getBotId());
		avail2Ai = aBot.isAvailableToAi();
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
		return 1;
	}
	
	
	@Override
	public double getKickerLevel()
	{
		return 0;
	}
	
	
	@Override
	public double getKickerLevelMax()
	{
		return 0;
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
	public long getLastKickTime()
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
	public double getKickSpeed()
	{
		return 0;
	}
	
	
	@Override
	public String getDevice()
	{
		return "";
	}
	
	
	@Override
	public double getDefaultVelocity()
	{
		return 0;
	}
	
	
	@Override
	public double getDefaultAcceleration()
	{
		return 0;
	}
	
	
	@Override
	public MoveConstraints getMoveConstraints()
	{
		return moveConstraints;
	}
}
