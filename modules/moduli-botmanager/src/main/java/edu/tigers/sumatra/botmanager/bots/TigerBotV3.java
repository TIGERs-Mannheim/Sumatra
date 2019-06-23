/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 30, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.bots;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.bots.communication.ReliableCmdManager;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchCtrl;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.filter.DataSync;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector3;


/**
 * New Bot 2015 with less/no knowledge about itself in Sumatra
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 2)
public class TigerBotV3 extends ABot
{
	private static final Logger								log						= Logger.getLogger(TigerBotV3.class
			.getName());
	
	private transient final ReliableCmdManager			reliableCmdManager	= new ReliableCmdManager(this);
	private transient TigerSystemMatchFeedback			latestFeedbackCmd		= null;
	private transient DataSync<IVector3>					sensoryPosBuffer		= new DataSync<>(30);
	private transient DataSync<IVector3>					sensoryVelBuffer		= new DataSync<>(30);
	private transient final List<ITigerBotV3Observer>	observers				= new CopyOnWriteArrayList<ITigerBotV3Observer>();
	
	@Configurable(spezis = { "v2013", "v2016" }, defValueSpezis = { "80", "75" })
	private double													center2DribblerDist	= 0;
	@Configurable(spezis = { "v2013", "v2016" }, defValueSpezis = { "3.0", "3.0" })
	private double													defAcc					= 0;
	@Configurable(spezis = { "v2013", "v2016" }, defValueSpezis = { "3.0", "4.0" })
	private double													defVel					= 0;
	
	
	static
	{
		ConfigRegistration.registerClass("botmgr", TigerBotV3.class);
	}
	
	
	@SuppressWarnings("unused")
	private TigerBotV3()
	{
	}
	
	
	/**
	 * @param botId
	 * @param baseStation
	 */
	public TigerBotV3(final BotID botId, final IBaseStation baseStation)
	{
		this(EBotType.TIGER_V3, botId, baseStation);
	}
	
	
	/**
	 * @param botId
	 * @param baseStation
	 */
	protected TigerBotV3(final EBotType type, final BotID botId, final IBaseStation baseStation)
	{
		super(type, botId, baseStation);
		latestFeedbackCmd = new TigerSystemMatchFeedback();
		log.debug("New TigerBot V3 with ID" + botId);
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final ITigerBotV3Observer observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ITigerBotV3Observer observer)
	{
		observers.remove(observer);
	}
	
	
	private void notifyNewFeedbackCmd(final TigerSystemMatchFeedback cmd)
	{
		for (ITigerBotV3Observer observer : observers)
		{
			observer.onNewFeedbackCmd(cmd);
		}
	}
	
	
	/**
	 * @param cmd
	 */
	private void notifyConsolePrint(final TigerSystemConsolePrint cmd)
	{
		for (ITigerBotV3Observer observer : observers)
		{
			observer.onConsolePrint(cmd);
		}
	}
	
	
	@Override
	public void execute(final ACommand cmd)
	{
		super.execute(cmd);
		reliableCmdManager.outgoingCommand(cmd);
		getBaseStation().enqueueCommand(getBotId(), cmd);
	}
	
	
	@Override
	public void start()
	{
	}
	
	
	@Override
	public void stop()
	{
	}
	
	
	private void onNewFeedbackCmd(final TigerSystemMatchFeedback cmd)
	{
		setKickerLevel(cmd.getKickerLevel());
		if (latestFeedbackCmd.getKickCounter() != cmd.getKickCounter())
		{
			setLastKickTime(System.nanoTime());
		}
		for (EFeature f : EFeature.values())
		{
			getBotFeatures().put(f, cmd.isFeatureWorking(f) ? EFeatureState.WORKING : EFeatureState.KAPUT);
		}
		if (center2DribblerDist == 0)
		{
			String spezi = "v2013";
			if (cmd.isFeatureWorking(EFeature.V2016))
			{
				spezi = "v2016";
			}
			ConfigRegistration.applySpezis(this, "botmgr", spezi);
			ConfigRegistration.applySpezis(getMoveConstraints(), "botmgr", spezi);
			assert center2DribblerDist != 0;
		}
		setRelBattery(latestFeedbackCmd.getBatteryPercentage());
		notifyNewFeedbackCmd(cmd);
	}
	
	
	@Override
	public void onIncommingBotCommand(final BotID id, final ACommand cmd)
	{
		if (!id.equals(getBotId()))
		{
			return;
		}
		super.onIncommingBotCommand(id, cmd);
		
		reliableCmdManager.incommingCommand(cmd);
		
		switch (cmd.getType())
		{
			case CMD_SYSTEM_MATCH_FEEDBACK:
				onNewFeedbackCmd((TigerSystemMatchFeedback) cmd);
				latestFeedbackCmd = (TigerSystemMatchFeedback) cmd;
				long timestamp = System.nanoTime();
				sensoryPosBuffer.add(timestamp,
						new Vector3(latestFeedbackCmd.getPosition(), latestFeedbackCmd.getOrientation()));
				sensoryVelBuffer.add(timestamp,
						new Vector3(latestFeedbackCmd.getVelocity(), latestFeedbackCmd.getAngularVelocity()));
				break;
			case CMD_SYSTEM_CONSOLE_PRINT:
				notifyConsolePrint((TigerSystemConsolePrint) cmd);
				break;
			default:
				break;
		}
	}
	
	
	/**
	 * @return the latestFeedbackCmd
	 */
	public TigerSystemMatchFeedback getLatestFeedbackCmd()
	{
		return latestFeedbackCmd;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public int getHardwareId()
	{
		if (latestFeedbackCmd != null)
		{
			return latestFeedbackCmd.getHardwareId();
		}
		return 255;
	}
	
	
	@Override
	public boolean isAvailableToAi()
	{
		if (super.isAvailableToAi())
		{
			if (latestFeedbackCmd == null)
			{
				return true;
			}
			return latestFeedbackCmd.isFeatureWorking(EFeature.MOVE);
		}
		return false;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public interface ITigerBotV3Observer
	{
		/**
		 * @param cmd
		 */
		void onNewFeedbackCmd(TigerSystemMatchFeedback cmd);
		
		
		/**
		 * @param cmd
		 */
		void onConsolePrint(TigerSystemConsolePrint cmd);
	}
	
	
	@Override
	public double getDribblerSpeed()
	{
		if (latestFeedbackCmd != null)
		{
			return latestFeedbackCmd.getDribblerSpeed();
		}
		return 0;
	}
	
	
	@Override
	public void sendMatchCommand()
	{
		super.sendMatchCommand();
		TigerSystemMatchCtrl matchCtrl = new TigerSystemMatchCtrl(getMatchCtrl());
		execute(matchCtrl);
	}
	
	
	@Override
	public Optional<IVector3> getSensoryPos()
	{
		long timestamp = System.nanoTime();
		long offsetXy = (long) (0.0 * 1e9);
		Optional<IVector3> posXy = sensoryPosBuffer.get(timestamp - offsetXy);
		long offsetW = (long) (0.06 * 1e9);
		Optional<IVector3> posW = sensoryPosBuffer.get(timestamp - offsetW);
		
		if (posXy.isPresent() && posW.isPresent())
		{
			return Optional.of(new Vector3(posXy.get().getXYVector().multiplyNew(1000), posW.get().z()));
		}
		return Optional.empty();
		// return Optional.of(new Vector3(
		// getLatestFeedbackCmd().getPosition().multiplyNew(1000),
		// getLatestFeedbackCmd().getOrientation()));
	}
	
	
	@Override
	public Optional<IVector3> getSensoryVel()
	{
		long timestamp = System.nanoTime();
		long offsetXy = (long) (0.0 * 1e9);
		Optional<IVector3> velXy = sensoryVelBuffer.get(timestamp - offsetXy);
		long offsetW = (long) (0.06 * 1e9);
		Optional<IVector3> velW = sensoryVelBuffer.get(timestamp - offsetW);
		
		if (velXy.isPresent() && velW.isPresent())
		{
			return Optional.of(new Vector3(velXy.get().getXYVector(), velW.get().z()));
		}
		return Optional.empty();
		// return Optional.of(new Vector3(
		// getLatestFeedbackCmd().getVelocity(),
		// getLatestFeedbackCmd().getAngularVelocity()));
	}
	
	
	@Override
	public boolean isBarrierInterrupted()
	{
		return getLatestFeedbackCmd().isBarrierInterrupted();
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
