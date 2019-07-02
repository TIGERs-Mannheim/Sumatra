/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.bots;

import java.util.Map;
import java.util.Optional;

import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.bot.params.IBotParams;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.botskills.data.MatchCommand;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;


/**
 * Bot base class.
 */
public abstract class ABot implements IBot
{
	private static final double KICKER_LEVEL_MAX = 200;
	
	private final BotID botId;
	private final EBotType type;
	private final IBaseStation baseStation;
	private final Map<EFeature, EFeatureState> botFeatures;
	private final MatchCommand matchCtrl = new MatchCommand();
	
	private IBotParams botParams = new BotParams();
	private TrajectoryWithTime<IVector3> curTrajectory = null;
	private String controlledBy = "";
	private boolean hideFromRcm = false;
	
	
	public ABot(final EBotType type, final BotID id, final IBaseStation baseStation)
	{
		botId = id;
		this.type = type;
		botFeatures = getDefaultFeatureStates();
		this.baseStation = baseStation;
	}
	
	
	public abstract EBotParamLabel getBotParamLabel();
	
	
	private Map<EFeature, EFeatureState> getDefaultFeatureStates()
	{
		Map<EFeature, EFeatureState> result = EFeature.createFeatureList();
		result.put(EFeature.DRIBBLER, EFeatureState.WORKING);
		result.put(EFeature.CHIP_KICKER, EFeatureState.WORKING);
		result.put(EFeature.STRAIGHT_KICKER, EFeatureState.WORKING);
		result.put(EFeature.MOVE, EFeatureState.WORKING);
		result.put(EFeature.BARRIER, EFeatureState.WORKING);
		result.put(EFeature.CHARGE_CAPS, EFeatureState.WORKING);
		return result;
	}
	
	
	/**
	 * This is called when the match command should be sent
	 */
	public void sendMatchCommand()
	{
		getBaseStation().acceptMatchCommand(getBotId(), getMatchCtrl());
	}
	
	
	@Override
	public double getKickerLevelMax()
	{
		return KICKER_LEVEL_MAX;
	}
	
	
	@Override
	public boolean isAvailableToAi()
	{
		return !isBlocked();
	}
	
	
	@Override
	public final EBotType getType()
	{
		return type;
	}
	
	
	@Override
	public String toString()
	{
		return "[Bot: " + type + "|" + getBotId() + "]";
	}
	
	
	@Override
	public final Map<EFeature, EFeatureState> getBotFeatures()
	{
		return botFeatures;
	}
	
	
	@Override
	public final String getControlledBy()
	{
		return controlledBy;
	}
	
	
	/**
	 * @param controlledBy the controlledBy to set
	 */
	public final void setControlledBy(final String controlledBy)
	{
		this.controlledBy = controlledBy;
	}
	
	
	@Override
	public final ETeamColor getColor()
	{
		return getBotId().getTeamColor();
	}
	
	
	@Override
	public final boolean isBlocked()
	{
		return !controlledBy.isEmpty();
	}
	
	
	@Override
	public final boolean isHideFromRcm()
	{
		return hideFromRcm;
	}
	
	
	/**
	 * @param hideFromRcm the hideFromRcm to set
	 */
	public final void setHideFromRcm(final boolean hideFromRcm)
	{
		this.hideFromRcm = hideFromRcm;
	}
	
	
	@Override
	public final BotID getBotId()
	{
		return botId;
	}
	
	
	/**
	 * @return the matchCtrl
	 */
	public final MatchCommand getMatchCtrl()
	{
		return matchCtrl;
	}
	
	
	/**
	 * @return the baseStation
	 */
	public IBaseStation getBaseStation()
	{
		return baseStation;
	}
	
	
	@Override
	public Optional<BotState> getSensoryState(long timestamp)
	{
		return Optional.empty();
	}
	
	
	@Override
	public Optional<TrajectoryWithTime<IVector3>> getCurrentTrajectory()
	{
		return Optional.ofNullable(curTrajectory);
	}
	
	
	/**
	 * @param curTrajectory
	 */
	public void setCurrentTrajectory(final TrajectoryWithTime<IVector3> curTrajectory)
	{
		this.curTrajectory = curTrajectory;
	}
	
	
	@Override
	public final IBotParams getBotParams()
	{
		return botParams;
	}
	
	
	public void setBotParams(final IBotParams botParams)
	{
		this.botParams = botParams;
	}
}
