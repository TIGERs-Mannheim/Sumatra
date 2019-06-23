/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.bots;

import java.util.Map;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;
import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.bots.communication.Statistics;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.CommandFactory;
import edu.tigers.sumatra.botmanager.commands.MatchCommand;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AVector3;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;


/**
 * Bot base class.
 * 
 * @author AndreR
 */
@Persistent(version = 2)
public abstract class ABot implements IBot, IConfigObserver
{
	/**  */
	public static final double											BAT_MIN			= 10.5;
	/**  */
	public static final double											BAT_MAX			= 12.6;
	
	private final BotID													botId;
	private final EBotType												type;
	private transient final IBaseStation							baseStation;
	
	private double															relBattery		= 1;
	private double															kickerLevel		= 0;
	
	private final Map<EFeature, EFeatureState>					botFeatures;
	
	private transient final Statistics								txStats			= new Statistics();
	private transient final Statistics								rxStats			= new Statistics();
	private transient final MatchCommand							matchCtrl		= new MatchCommand();
	private transient final MoveConstraints						moveConstraints;
	private transient Optional<TrajectoryWithTime<IVector3>>	curTrajectory	= Optional.empty();
	
	private transient double											kickerLevelMax	= 180;
	private transient String											controlledBy	= "";
	private transient boolean											hideFromAi		= false;
	private transient boolean											hideFromRcm		= false;
	private transient long												lastKickTime	= 0;
	/** [Hz] desired number of package to receive */
	private transient double											updateRate		= 100;
	private transient double											minUpdateRate	= 10;
	
	
	private static final String[]										BOT_NAMES		= { "Gandalf", "Alice", "Tigger",
			"Poller",
			"Q", "Eichbaum",
			"This Bot",
			"Black Betty",
			"Trinity", "Neo",
			"Bob",
			"Yoda" };
	
	
	/**
	 * @param type
	 * @param id
	 * @param baseStation
	 */
	public ABot(final EBotType type, final BotID id, final IBaseStation baseStation)
	{
		botId = id;
		this.type = type;
		botFeatures = getDefaultFeatureStates();
		this.baseStation = baseStation;
		moveConstraints = new MoveConstraints();
		ConfigRegistration.applySpezis(moveConstraints, "botmgr", "");
		ConfigRegistration.applySpezis(moveConstraints, "botmgr", type.name());
	}
	
	
	protected ABot(final ABot aBot, final EBotType type)
	{
		botId = aBot.botId;
		this.type = type;
		botFeatures = aBot.botFeatures;
		baseStation = aBot.baseStation;
		relBattery = aBot.relBattery;
		kickerLevel = aBot.kickerLevel;
		kickerLevelMax = aBot.kickerLevelMax;
		moveConstraints = aBot.moveConstraints;
	}
	
	
	protected ABot()
	{
		botId = null;
		type = null;
		botFeatures = null;
		baseStation = null;
		moveConstraints = null;
	}
	
	
	@Override
	public void afterApply(final IConfigClient configClient)
	{
		ConfigRegistration.applySpezis(moveConstraints, "botmgr", type.name());
	}
	
	
	/**
	 * @return battery level between 0 and 1
	 */
	@Override
	public double getBatteryRelative()
	{
		return relBattery;
	}
	
	
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
	
	
	/**
	 * @param cmd
	 */
	public void execute(final ACommand cmd)
	{
		txStats.packets++;
		txStats.payload += CommandFactory.getInstance().getLength(cmd, false);
	}
	
	
	/**
	 * 
	 */
	public void sendMatchCommand()
	{
	}
	
	
	/**
	 * 
	 */
	public abstract void start();
	
	
	/**
	 * 
	 */
	public abstract void stop();
	
	
	/**
	 * @return
	 */
	@Override
	public double getKickerLevel()
	{
		return kickerLevel;
	}
	
	
	/**
	 * The absolute maximum kicker level possible for the bot (not the currently set max cap!)
	 * 
	 * @return [V]
	 */
	@Override
	public double getKickerLevelMax()
	{
		return kickerLevelMax;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public abstract double getDribblerSpeed();
	
	
	/**
	 * @param id
	 * @param cmd
	 */
	public void onIncommingBotCommand(final BotID id, final ACommand cmd)
	{
		rxStats.packets++;
		rxStats.payload += CommandFactory.getInstance().getLength(cmd, false);
	}
	
	
	/**
	 * Each bot has its own hardware id that uniquely identifies a robot by hardware (mainboard)
	 * 
	 * @return
	 */
	@Override
	public abstract int getHardwareId();
	
	
	/**
	 * @return
	 */
	@Override
	public boolean isAvailableToAi()
	{
		return !isBlocked() && !isHideFromAi();
	}
	
	
	/**
	 * System.nanotime timestamp of last kick
	 * 
	 * @return
	 */
	@Override
	public final long getLastKickTime()
	{
		return lastKickTime;
	}
	
	
	/**
	 * @param lastKickTime the lastKickTime to set
	 */
	protected final void setLastKickTime(final long lastKickTime)
	{
		this.lastKickTime = lastKickTime;
	}
	
	
	/**
	 * @return
	 */
	public abstract boolean isBarrierInterrupted();
	
	
	/**
	 * @return
	 */
	public Statistics getRxStats()
	{
		return new Statistics(rxStats);
	}
	
	
	/**
	 * @return
	 */
	public Statistics getTxStats()
	{
		return new Statistics(txStats);
	}
	
	
	/**
	 * @return
	 */
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
	
	
	/**
	 * @return the botFeatures
	 */
	@Override
	public final Map<EFeature, EFeatureState> getBotFeatures()
	{
		return botFeatures;
	}
	
	
	/**
	 * @return the controlledBy
	 */
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
	
	
	/**
	 * @return the color
	 */
	@Override
	public final ETeamColor getColor()
	{
		return getBotId().getTeamColor();
	}
	
	
	/**
	 * @return the manualControl
	 */
	@Override
	public final boolean isBlocked()
	{
		return !controlledBy.isEmpty();
	}
	
	
	/**
	 * @return the excludeFromAi
	 */
	@Override
	public final boolean isHideFromAi()
	{
		return hideFromAi;
	}
	
	
	/**
	 * @param excludeFromAi the excludeFromAi to set
	 */
	public final void setHideFromAi(final boolean excludeFromAi)
	{
		hideFromAi = excludeFromAi;
	}
	
	
	/**
	 * @return the hideFromRcm
	 */
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
	
	
	/**
	 * @return the botId
	 */
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
	 * @return the updateRate
	 */
	public final double getUpdateRate()
	{
		return updateRate;
	}
	
	
	/**
	 * @param updateRate the updateRate to set
	 */
	public final void setUpdateRate(final double updateRate)
	{
		this.updateRate = updateRate;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public String getName()
	{
		return BOT_NAMES[getBotId().getNumber()];
	}
	
	
	/**
	 * @return the baseStation
	 */
	public final IBaseStation getBaseStation()
	{
		return baseStation;
	}
	
	
	/**
	 * Sets cheering flag
	 * 
	 * @param cheering
	 */
	public void setCheering(final boolean cheering)
	{
		getMatchCtrl().setCheering(cheering);
	}
	
	
	/**
	 * @param kickerLevel the kickerLevel to set
	 */
	protected final void setKickerLevel(final double kickerLevel)
	{
		this.kickerLevel = kickerLevel;
	}
	
	
	/**
	 * Get internal position from sensory data
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @return
	 */
	@Override
	public Optional<IVector3> getSensoryPos()
	{
		return Optional.empty();
	}
	
	
	/**
	 * Get internal velcoity from sensory data
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @return
	 */
	@Override
	public Optional<IVector3> getSensoryVel()
	{
		return Optional.empty();
	}
	
	
	/**
	 * @return
	 */
	@Override
	public Optional<TrajectoryWithTime<IVector3>> getCurrentTrajectory()
	{
		return curTrajectory;
	}
	
	
	/**
	 * @param curTrajectory
	 */
	public synchronized void setCurrentTrajectory(final Optional<TrajectoryWithTime<IVector3>> curTrajectory)
	{
		this.curTrajectory = curTrajectory;
	}
	
	
	/**
	 * @return the minUpdateRate
	 */
	public double getMinUpdateRate()
	{
		return minUpdateRate;
	}
	
	
	/**
	 * @param timestamp
	 * @return
	 */
	public IVector3 getGlobalTargetVelocity(final long timestamp)
	{
		return AVector3.ZERO_VECTOR;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public double getKickSpeed()
	{
		return getMatchCtrl().getKickSpeed();
	}
	
	
	/**
	 * @return
	 */
	@Override
	public String getDevice()
	{
		return getMatchCtrl().getDevice().name();
	}
	
	
	/**
	 * @param relBattery the relBattery to set
	 */
	public void setRelBattery(final double relBattery)
	{
		this.relBattery = relBattery;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public double getDefaultVelocity()
	{
		return getMoveConstraints().getVelMax();
	}
	
	
	/**
	 * @return
	 */
	@Override
	public double getDefaultAcceleration()
	{
		return getMoveConstraints().getAccMax();
	}
	
	
	/**
	 * @return the moveConstraints
	 */
	@Override
	public MoveConstraints getMoveConstraints()
	{
		return moveConstraints;
	}
}
