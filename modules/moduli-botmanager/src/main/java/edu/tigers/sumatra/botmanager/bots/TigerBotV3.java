/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.bots;

import java.util.Optional;

import org.apache.log4j.Logger;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.bot.params.IBotParams;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.bots.communication.ReliableCmdManager;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchCtrl;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.botparams.BotParamsManager.IBotParamsManagerObserver;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.filter.DataSync;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * New Bot 2015 with less/no knowledge about itself in Sumatra
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 2)
public class TigerBotV3 extends ABot implements IBotParamsManagerObserver
{
	private static final Logger						log						= Logger.getLogger(TigerBotV3.class
			.getName());
	
	private final transient ReliableCmdManager	reliableCmdManager	= new ReliableCmdManager(this);
	private transient TigerSystemMatchFeedback	latestFeedbackCmd		= null;
	private transient DataSync<IVector3>			sensoryPosBuffer		= new DataSync<>(30);
	private transient DataSync<IVector3>			sensoryVelBuffer		= new DataSync<>(30);
	private transient BotParamsManager				paramsManager;
	
	private IBotParams									botParamsV2013			= new BotParams();
	private IBotParams									botParamsV2016			= new BotParams();
	
	
	@SuppressWarnings("unused")
	private TigerBotV3()
	{
	}
	
	
	/**
	 * @param botId
	 * @param baseStation
	 * @param paramsManager
	 */
	public TigerBotV3(final BotID botId, final IBaseStation baseStation, final BotParamsManager paramsManager)
	{
		this(EBotType.TIGER_V3, botId, baseStation);
		
		this.paramsManager = paramsManager;
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
		if (paramsManager != null)
		{
			botParamsV2013 = paramsManager.getBotParams(EBotParamLabel.TIGER_V2013);
			botParamsV2016 = paramsManager.getBotParams(EBotParamLabel.TIGER_V2016);
			paramsManager.addObserver(this);
		}
	}
	
	
	@Override
	public void stop()
	{
		if (paramsManager != null)
		{
			paramsManager.removeObserver(this);
		}
	}
	
	
	private void onNewFeedbackCmd(final TigerSystemMatchFeedback cmd)
	{
		for (EFeature f : EFeature.values())
		{
			getBotFeatures().put(f, cmd.isFeatureWorking(f) ? EFeatureState.WORKING : EFeatureState.KAPUT);
		}
	}
	
	
	@Override
	public void onIncommingBotCommand(final ACommand cmd)
	{
		super.onIncommingBotCommand(cmd);
		
		reliableCmdManager.incommingCommand(cmd);
		
		if (cmd.getType() == ECommand.CMD_SYSTEM_MATCH_FEEDBACK)
		{
			onNewFeedbackCmd((TigerSystemMatchFeedback) cmd);
			latestFeedbackCmd = (TigerSystemMatchFeedback) cmd;
			long timestamp = System.nanoTime();
			sensoryPosBuffer.add(timestamp,
					Vector3.from2d(latestFeedbackCmd.getPosition(), latestFeedbackCmd.getOrientation()));
			sensoryVelBuffer.add(timestamp,
					Vector3.from2d(latestFeedbackCmd.getVelocity(), latestFeedbackCmd.getAngularVelocity()));
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
		return super.isAvailableToAi() &&
				((latestFeedbackCmd == null) || latestFeedbackCmd.isFeatureWorking(EFeature.MOVE));
		
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
	
	
	/**
	 * @return battery level between 0 and 1
	 */
	@Override
	public double getBatteryRelative()
	{
		if (latestFeedbackCmd != null)
		{
			return latestFeedbackCmd.getBatteryPercentage();
		}
		
		return 0;
	}
	
	
	@Override
	public double getKickerLevel()
	{
		if (latestFeedbackCmd != null)
		{
			return latestFeedbackCmd.getKickerLevel();
		}
		
		return 0;
	}
	
	
	@Override
	public ERobotMode getRobotMode()
	{
		if (latestFeedbackCmd != null)
		{
			return latestFeedbackCmd.getRobotMode();
		}
		return ERobotMode.IDLE;
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
			return Optional.of(Vector3.from2d(posXy.get().getXYVector().multiplyNew(1000), posW.get().z()));
		}
		return Optional.empty();
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
			return Optional.of(Vector3.from2d(velXy.get().getXYVector(), velW.get().z()));
		}
		return Optional.empty();
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
		return getBotParams().getDimensions().getCenter2DribblerDist();
	}
	
	
	@Override
	public IBotParams getBotParams()
	{
		if (getBotFeatures().getOrDefault(EFeature.V2016, EFeatureState.UNKNOWN) == EFeatureState.WORKING)
		{
			return botParamsV2016;
		}
		
		return botParamsV2013;
	}
	
	
	@Override
	public void onBotParamsUpdated(final EBotParamLabel label, final IBotParams params)
	{
		switch (label)
		{
			case TIGER_V2013:
				botParamsV2013 = params;
				break;
			case TIGER_V2016:
				botParamsV2016 = params;
				break;
			default:
				break;
		}
	}
}
