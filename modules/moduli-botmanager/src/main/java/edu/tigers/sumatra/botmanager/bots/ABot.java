/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.bots;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.bots.communication.Statistics;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.CommandFactory;
import edu.tigers.sumatra.botmanager.commands.MatchCommand;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;


/**
 * Bot base class.
 * 
 * @author AndreR
 */
public abstract class ABot implements IBot, IConfigObserver
{
	private static final String[]										BOT_NAMES		= { "Gandalf", "Alice", "Tigger",
			"Poller",
			"Q", "Eichbaum",
			"This Bot",
			"Black Betty",
			"Trinity", "Neo",
			"Bob",
			"Yoda" };
	
	private final BotID													botId;
	private final EBotType												type;
	private final ERobotMode											robotMode;
	private final transient IBaseStation							baseStation;
	private final Map<EFeature, EFeatureState>					botFeatures;
	private final transient Statistics								txStats			= new Statistics();
	private final transient Statistics								rxStats			= new Statistics();
	private final transient MatchCommand							matchCtrl		= new MatchCommand();
	private final transient List<IABotObserver>					observers		= new CopyOnWriteArrayList<>();
	private transient Optional<TrajectoryWithTime<IVector3>>	curTrajectory	= Optional.empty();
	private transient double											kickerLevelMax	= 200;
	private transient String											controlledBy	= "";
	private transient boolean											hideFromAi		= false;
	private transient boolean											hideFromRcm		= false;
	
	/** [Hz] desired number of package to receive */
	private transient double											updateRate		= 100;
	
	
	/**
	 * @param type
	 * @param id
	 * @param baseStation
	 */
	public ABot(final EBotType type, final BotID id, final IBaseStation baseStation)
	{
		botId = id;
		this.type = type;
		robotMode = ERobotMode.IDLE;
		botFeatures = getDefaultFeatureStates();
		this.baseStation = baseStation;
	}
	
	
	protected ABot(final ABot aBot, final EBotType type)
	{
		botId = aBot.botId;
		this.type = type;
		robotMode = aBot.robotMode;
		botFeatures = aBot.botFeatures;
		baseStation = aBot.baseStation;
		kickerLevelMax = aBot.kickerLevelMax;
	}
	
	
	protected ABot()
	{
		botId = null;
		type = null;
		robotMode = null;
		botFeatures = null;
		baseStation = null;
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IABotObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IABotObserver observer)
	{
		observers.remove(observer);
	}
	
	
	protected void notifyIncommingBotCommand(final ACommand cmd)
	{
		for (IABotObserver observer : observers)
		{
			observer.onIncommingBotCommand(cmd);
		}
	}
	
	
	protected Map<EFeature, EFeatureState> getDefaultFeatureStates()
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
	 * @param cmd
	 */
	public void execute(final ACommand cmd)
	{
		txStats.packets++;
		txStats.payload += CommandFactory.getInstance().getLength(cmd, false);
	}
	
	
	/**
	 * This is called when the match command should be sent
	 */
	public void sendMatchCommand()
	{
	}
	
	
	/**
	 * Start bot
	 */
	public abstract void start();
	
	
	/**
	 * Stop bot
	 */
	public abstract void stop();
	
	
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
	 * @param cmd
	 */
	public void onIncommingBotCommand(final ACommand cmd)
	{
		rxStats.packets++;
		rxStats.payload += CommandFactory.getInstance().getLength(cmd, false);
		
		notifyIncommingBotCommand(cmd);
	}
	
	
	/**
	 * @return
	 */
	@Override
	public boolean isAvailableToAi()
	{
		return !isBlocked() && !isHideFromAi();
	}
	
	
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
	
	
	/**
	 * @return
	 */
	@Override
	public ERobotMode getRobotMode()
	{
		return robotMode;
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
	 * Get internal velocity from sensory data
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
	public synchronized Optional<TrajectoryWithTime<IVector3>> getCurrentTrajectory()
	{
		return curTrajectory;
	}
	
	
	/**
	 * @param curTrajectory
	 */
	public synchronized void setCurrentTrajectory(final TrajectoryWithTime<IVector3> curTrajectory)
	{
		this.curTrajectory = Optional.ofNullable(curTrajectory);
	}
	
	
	/** Common Bot Observer */
	@FunctionalInterface
	public interface IABotObserver
	{
		/**
		 * Called when a new command from the robot arrives.
		 * 
		 * @param cmd
		 */
		void onIncommingBotCommand(ACommand cmd);
	}
}
