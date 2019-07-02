/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.bots;

import java.util.Optional;

import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.botmanager.basestation.IBaseStation;
import edu.tigers.sumatra.botmanager.basestation.TigersBaseStation;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerSystemVersion;
import edu.tigers.sumatra.botmanager.communication.ReliableCmdManager;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * A TIGERs Bot implementation
 */
public class TigerBot extends ABot
{
	private final ReliableCmdManager reliableCmdManager = new ReliableCmdManager(this);
	
	private TigerSystemMatchFeedback latestFeedbackCmd = new TigerSystemMatchFeedback();
	private String version = "not available";
	
	
	public TigerBot(final BotID botId, final IBaseStation baseStation)
	{
		super(EBotType.TIGERS, botId, baseStation);
	}
	
	
	@Override
	public TigersBaseStation getBaseStation()
	{
		return (TigersBaseStation) super.getBaseStation();
	}
	
	
	public void execute(final ACommand cmd)
	{
		reliableCmdManager.outgoingCommand(cmd);
		getBaseStation().enqueueCommand(getBotId(), cmd);
	}
	
	
	private void onNewFeedbackCmd(final TigerSystemMatchFeedback cmd)
	{
		for (EFeature f : EFeature.values())
		{
			getBotFeatures().put(f, cmd.isFeatureWorking(f) ? EFeatureState.WORKING : EFeatureState.KAPUT);
		}
	}
	
	
	public void onIncomingBotCommand(final ACommand cmd)
	{
		reliableCmdManager.incomingCommand(cmd);
		
		switch (cmd.getType())
		{
			case CMD_SYSTEM_MATCH_FEEDBACK:
				onNewFeedbackCmd((TigerSystemMatchFeedback) cmd);
				latestFeedbackCmd = (TigerSystemMatchFeedback) cmd;
				break;
			case CMD_SYSTEM_VERSION:
				version = ((TigerSystemVersion) cmd).getFullVersionString();
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
	
	
	@Override
	public double getCenter2DribblerDist()
	{
		return getBotParams().getDimensions().getCenter2DribblerDist();
	}
	
	
	@Override
	public String getVersionString()
	{
		return version;
	}
	
	
	@Override
	public double getDribblerTemp()
	{
		return latestFeedbackCmd.getDribblerTemp();
	}
	
	
	@Override
	public EBotParamLabel getBotParamLabel()
	{
		if (getBotFeatures().getOrDefault(EFeature.V2016, EFeatureState.UNKNOWN) == EFeatureState.WORKING)
		{
			return EBotParamLabel.TIGER_V2016;
		}
		return EBotParamLabel.TIGER_V2013;
	}
}
