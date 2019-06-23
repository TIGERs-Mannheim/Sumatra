/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import java.util.Map;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;


/**
 * Dummy stub class for bots to prevent using null
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DummyBot extends ABot
{
	private float		currentCharge	= 189f;
	private boolean	avail2Ai			= false;
	
	
	/**
	 * @return the currentCharge
	 */
	public float getCurrentCharge()
	{
		return currentCharge;
	}
	
	
	/**
	 * @param currentCharge the currentCharge to set
	 */
	public void setCurrentCharge(final float currentCharge)
	{
		this.currentCharge = currentCharge;
	}
	
	
	/**
	 */
	public DummyBot()
	{
		super(EBotType.UNKNOWN, BotID.createBotId(), -1, -1);
	}
	
	
	/**
	 * @param botId
	 */
	public DummyBot(final BotID botId)
	{
		super(EBotType.UNKNOWN, botId, -1, -1);
	}
	
	
	@Override
	protected Map<EFeature, EFeatureState> getDefaultFeatureStates()
	{
		Map<EFeature, EFeatureState> result = EFeature.createFeatureList();
		result.put(EFeature.DRIBBLER, EFeatureState.WORKING);
		result.put(EFeature.CHIP_KICKER, EFeatureState.WORKING);
		result.put(EFeature.STRAIGHT_KICKER, EFeatureState.WORKING);
		result.put(EFeature.MOVE, EFeatureState.WORKING);
		result.put(EFeature.BARRIER, EFeatureState.WORKING);
		return result;
	}
	
	
	@Override
	public void execute(final ACommand cmd)
	{
	}
	
	
	@Override
	public void start()
	{
	}
	
	
	@Override
	public void stop()
	{
	}
	
	
	@Override
	public float getBatteryLevel()
	{
		return 2;
	}
	
	
	@Override
	public float getBatteryLevelMax()
	{
		return 3;
	}
	
	
	@Override
	public float getBatteryLevelMin()
	{
		return 1;
	}
	
	
	@Override
	public float getKickerLevel()
	{
		return currentCharge;
	}
	
	
	@Override
	public float getKickerLevelMax()
	{
		return 180f;
	}
	
	
	@Override
	public ENetworkState getNetworkState()
	{
		return ENetworkState.OFFLINE;
	}
	
	
	@Override
	public void newSpline(final SplinePair3D spline)
	{
	}
	
	
	@Override
	public void setDefaultKickerMaxCap()
	{
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
	public float getCenter2DribblerDist()
	{
		return 75;
	}
}
