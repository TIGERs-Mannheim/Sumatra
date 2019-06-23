/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.bots;

import java.util.Optional;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.bot.params.IBotParams;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.bots.communication.ReliableCmdManager;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchCtrl;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerSystemVersion;
import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.botparams.BotParamsManager.IBotParamsManagerObserver;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * New Bot 2015 with less/no knowledge about itself in Sumatra
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TigerBot extends ABot implements IBotParamsManagerObserver
{
	private static final Logger log = Logger.getLogger(TigerBot.class.getName());
	private final transient ReliableCmdManager reliableCmdManager = new ReliableCmdManager(this);
	private transient TigerSystemMatchFeedback latestFeedbackCmd;
	private transient BotParamsManager paramsManager;
	private IBotParams botParamsV2013 = new BotParams();
	private IBotParams botParamsV2016 = new BotParams();
	private transient TigerSystemVersion latestVersionCmd = new TigerSystemVersion();
	
	
	/**
	 * @param botId
	 * @param baseStation
	 * @param paramsManager
	 */
	public TigerBot(final BotID botId, final IBaseStation baseStation, final BotParamsManager paramsManager)
	{
		super(EBotType.TIGER_V3, botId, baseStation);
		latestFeedbackCmd = new TigerSystemMatchFeedback();
		log.debug("New TigerBot with ID " + botId);
		
		this.paramsManager = paramsManager;
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
		log.debug("Starting bot " + getBotId());
		if (paramsManager != null)
		{
			botParamsV2013 = paramsManager.get(EBotParamLabel.TIGER_V2013);
			botParamsV2016 = paramsManager.get(EBotParamLabel.TIGER_V2016);
			paramsManager.addObserver(this);
		}
	}
	
	
	@Override
	public void stop()
	{
		log.debug("Stopping bot " + getBotId());
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
	public void onIncomingBotCommand(final ACommand cmd)
	{
		super.onIncomingBotCommand(cmd);
		
		reliableCmdManager.incommingCommand(cmd);
		
		switch (cmd.getType())
		{
			case CMD_SYSTEM_MATCH_FEEDBACK:
				onNewFeedbackCmd((TigerSystemMatchFeedback) cmd);
				latestFeedbackCmd = (TigerSystemMatchFeedback) cmd;
				break;
			case CMD_SYSTEM_VERSION:
				latestVersionCmd = (TigerSystemVersion) cmd;
				break;
			default:
				break;
		}
	}
	
	
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
	public boolean isOK()
	{
		boolean energetic = getBotFeatures().get(EFeature.ENERGETIC) == EFeatureState.WORKING;
		boolean straight = getBotFeatures().get(EFeature.STRAIGHT_KICKER) == EFeatureState.WORKING;
		boolean chip = getBotFeatures().get(EFeature.CHIP_KICKER) == EFeatureState.WORKING;
		boolean charge = getBotFeatures().get(EFeature.CHARGE_CAPS) == EFeatureState.WORKING;
		
		return energetic && straight && chip && charge;
	}
	
	
	@Override
	public void sendMatchCommand()
	{
		super.sendMatchCommand();
		TigerSystemMatchCtrl matchCtrl = new TigerSystemMatchCtrl(getMatchCtrl());
		execute(matchCtrl);
	}
	
	
	@Override
	public Optional<BotState> getSensoryState(final long timestamp)
	{
		if (latestFeedbackCmd == null)
		{
			return Optional.empty();
		}
		State state = State.of(
				Pose.from(latestFeedbackCmd.getPosition().multiply(1000), latestFeedbackCmd.getOrientation()),
				Vector3.from2d(latestFeedbackCmd.getVelocity(), latestFeedbackCmd.getAngularVelocity()));
		return Optional.of(BotState.of(getBotId(), state));
	}
	
	
	@Override
	public boolean isBarrierInterrupted()
	{
		return latestFeedbackCmd.isBarrierInterrupted();
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
	
	
	@Override
	public String getVersionString()
	{
		return latestVersionCmd.getFullVersionString();
	}
}
