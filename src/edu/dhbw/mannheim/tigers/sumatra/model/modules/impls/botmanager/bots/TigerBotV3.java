/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 30, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.basestation.IBaseStation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ReliableCmdManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ABotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillGlobalPosVel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillGlobalPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillMotorsOff;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.LimitedVelocityCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.TigerKickerKickV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetPIDParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSkillPositioningCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemMatchCtrl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemMatchPosVel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerStatusFeedbackPid;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerSystemBotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerSystemPerformance;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * New Bot 2015 with less/no knowledge about itself in Sumatra
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class TigerBotV3 extends ABot
{
	private static final Logger								log						= Logger.getLogger(TigerBotV3.class
																											.getName());
	
	/**  */
	public static final float									BAT_MIN					= 10.5f;
	/**  */
	public static final float									BAT_MAX					= 12.6f;
	
	private float													battery					= 0;
	private float													kickerLevel				= 0;
	
	private transient final IBaseStation					baseStation;
	
	private transient final Statistics						txStats					= new Statistics();
	private transient final Statistics						rxStats					= new Statistics();
	
	private transient final ReliableCmdManager			reliableCmdManager	= new ReliableCmdManager(this);
	
	
	@Configurable(comment = "Desired frequency [Hz] of feedback commands from bot if idle")
	private static int											feedbackFreqIdle		= 5;
	
	@Configurable(comment = "Desired frequency [Hz] of feedback commands from bot if active")
	private static int											feedbackFreqActive	= 120;
	
	@Configurable(comment = "Bot names, ordered by id")
	private static String[]										botNames					= { "Gandalf", "Achilles", "Eichbaum",
																									"Tigger",
																									"Optimus Prime", "Odysseus", "Leopard 3",
																									"Q",
																									"Reality Checkpoint", "This Bot", "Yoda",
																									"Hercules" };
	
	@Configurable(comment = "Disable controller on reset")
	private static boolean										disableControllers	= true;
	
	@Configurable(comment = "Dist [mm] - Distance between center of bot to dribbling bar")
	private static float											center2DribblerDist	= 75;
	
	private transient TigerSystemMatchFeedback			latestFeedbackCmd		= new TigerSystemMatchFeedback();
	
	private transient MatchCmdMemory							matchCmdMemory			= new MatchCmdMemory();
	
	private transient final List<ITigerBotV3Observer>	observers				= new CopyOnWriteArrayList<ITigerBotV3Observer>();
	
	
	@SuppressWarnings("unused")
	private TigerBotV3()
	{
		baseStation = null;
	}
	
	
	/**
	 * @param botId
	 * @param baseStation
	 */
	public TigerBotV3(final BotID botId, final IBaseStation baseStation)
	{
		super(EBotType.TIGER_V3, botId, 0, -1);
		this.baseStation = baseStation;
		setName(botNames[botId.getNumber()]);
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
	public void setDefaultKickerMaxCap()
	{
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
		boolean handled = handleMatchCmd(cmd);
		if (handled)
		{
			matchCmdMemory.tUpdated = SumatraClock.nanoTime();
		} else
		{
			switch (cmd.getType())
			{
				case CMD_CTRL_SET_PID_PARAMS:
					TigerCtrlSetPIDParams pids = (TigerCtrlSetPIDParams) cmd;
					log.debug("Set PIDs for " + pids.getParamType() + " on " + getBotID() + " to " + pids.getParams());
					break;
				default:
			}
			
			// command not considered in match cmd, send it raw
			executeCommand(cmd);
		}
	}
	
	
	/**
	 * Execute a cmd directly, no match cmd handling here!
	 * 
	 * @param cmd
	 */
	private void executeCommand(final ACommand cmd)
	{
		reliableCmdManager.outgoingCommand(cmd);
		baseStation.enqueueCommand(getBotID(), cmd);
		txStats.packets++;
		txStats.payload += CommandFactory.getInstance().getLength(cmd, false);
	}
	
	
	private boolean handleMatchCmd(final ACommand cmd)
	{
		switch (cmd.getType())
		{
			case CMD_CTRL_RESET:
				matchCmdMemory.skill = new BotSkillMotorsOff();
				reliableCmdManager.clearAllPendingCmds();
				matchCmdMemory.dribbleSpeed = 0;
				if (matchCmdMemory.kickMode != EKickerMode.DISARM.getId())
				{
					matchCmdMemory.kickMode = EKickerMode.DISARM.getId();
					matchCmdMemory.kickSeq++;
				}
				matchCmdMemory.limitedVelocity = false;
				return true;
			case CMD_KICKER_KICKV2:
			{
				TigerKickerKickV2 kick = (TigerKickerKickV2) cmd;
				matchCmdMemory.kickSeq++;
				matchCmdMemory.kickDuration = kick.getFiringDuration();
				matchCmdMemory.kickDevice = kick.getDevice();
				matchCmdMemory.kickMode = kick.getMode();
				return true;
			}
			case CMD_KICKER_KICKV3:
			{
				TigerKickerKickV3 kick = (TigerKickerKickV3) cmd;
				matchCmdMemory.kickSeq++;
				matchCmdMemory.kickDuration = kick.getDuration();
				matchCmdMemory.kickDevice = kick.getDevice().getValue();
				matchCmdMemory.kickMode = kick.getMode().getId();
				return true;
			}
			case CMD_MOTOR_DRIBBLE:
				TigerDribble dribble = (TigerDribble) cmd;
				matchCmdMemory.dribbleSpeed = dribble.getSpeed();
				return true;
			case CMD_MOTOR_MOVE_V2:
				TigerMotorMoveV2 motor = (TigerMotorMoveV2) cmd;
				matchCmdMemory.skill = new BotSkillLocalVelocity(motor.getXY(), motor.getW());
				return true;
			case CMD_SKILL_POSITIONING:
				TigerSkillPositioningCommand pos = (TigerSkillPositioningCommand) cmd;
				if (matchCmdMemory.skill.getClass().equals(BotSkillGlobalPosition.class))
				{
					// BotSkillGlobalPosition posCmd = (BotSkillGlobalPosition) matchCmdMemory.skill;
					// if (!posCmd.getPos().equals(pos.getDestination(), 1))
					// {
					// log.info("New dest: " + pos.getDestination() + " old:" + posCmd.getPos());
					// }
				}
				matchCmdMemory.skill = new BotSkillGlobalPosition(pos.getDestination(), pos.getOrientation(), pos.getT());
				return true;
			case CMD_SYSTEM_MATCH_POS_VEL:
				TigerSystemMatchPosVel posVel = (TigerSystemMatchPosVel) cmd;
				matchCmdMemory.skill = new BotSkillGlobalPosVel(posVel.getDestination(),
						posVel.getOrientation(),
						posVel.getVelocity());
				return true;
			case CMD_KICKER_CHARGE_AUTO:
				TigerKickerChargeAuto charge = (TigerKickerChargeAuto) cmd;
				if (charge.getMax() > 0)
				{
					if (!matchCmdMemory.chargeAuto)
					{
						matchCmdMemory.chargeAuto = true;
						matchCmdMemory.kickSeq++;
					}
				} else if (matchCmdMemory.chargeAuto)
				{
					matchCmdMemory.chargeAuto = false;
					matchCmdMemory.kickSeq++;
				}
				return true;
			case CMD_SYSTEM_BOT_SKILL:
				TigerSystemBotSkill botSkillCmd = (TigerSystemBotSkill) cmd;
				matchCmdMemory.skill = botSkillCmd.getSkill();
				return true;
			case CMD_SYSTEM_LIMITED_VEL:
				LimitedVelocityCommand limVelCmd = (LimitedVelocityCommand) cmd;
				matchCmdMemory.limitedVelocity = limVelCmd.getMaxVelocity() > 0;
				return true;
			default:
				break;
		}
		return false;
	}
	
	
	/**
	 * Translate given cmds to a match cmd and send it.
	 * This will be called regularly by SkillExecutor
	 * 
	 * @param cmds
	 */
	public void executeMatchCmd(final List<ACommand> cmds)
	{
		for (ACommand cmd : cmds)
		{
			execute(cmd);
		}
		
		final int feedbackFreq;
		if ((SumatraClock.nanoTime() - matchCmdMemory.tUpdated) < 1e8)
		{
			feedbackFreq = feedbackFreqActive;
		} else
		{
			feedbackFreq = feedbackFreqIdle;
		}
		
		TigerSystemMatchCtrl match = new TigerSystemMatchCtrl();
		
		match.setSkill(matchCmdMemory.skill);
		
		match.setKickerAutocharge(matchCmdMemory.chargeAuto, matchCmdMemory.kickSeq);
		match.setKick(matchCmdMemory.kickDuration, matchCmdMemory.kickDevice, matchCmdMemory.kickMode,
				matchCmdMemory.kickSeq);
		match.setFeedbackFreq(feedbackFreq);
		match.setDribblerSpeed(matchCmdMemory.dribbleSpeed);
		match.setCheering(matchCmdMemory.cheering);
		match.setLimitedVelocity(matchCmdMemory.limitedVelocity);
		executeCommand(match);
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
		return battery;
	}
	
	
	@Override
	public float getBatteryLevelMax()
	{
		return BAT_MAX;
	}
	
	
	@Override
	public float getBatteryLevelMin()
	{
		return BAT_MIN;
	}
	
	
	@Override
	public float getKickerLevel()
	{
		return kickerLevel;
	}
	
	
	@Override
	public float getKickerLevelMax()
	{
		return 180f;
	}
	
	
	@Override
	public ENetworkState getNetworkState()
	{
		return ENetworkState.ONLINE;
	}
	
	
	@Override
	public void newSpline(final SplinePair3D spline)
	{
	}
	
	
	private void onNewFeedbackCmd(final TigerSystemMatchFeedback cmd)
	{
		for (EFeature f : EFeature.values())
		{
			getBotFeatures().put(f, cmd.isFeatureWorking(f) ? EFeatureState.WORKING : EFeatureState.KAPUT);
		}
		notifyNewFeedbackCmd(cmd);
	}
	
	
	@Override
	public void onIncommingBotCommand(final BotID id, final ACommand cmd)
	{
		if (!id.equals(getBotID()))
		{
			return;
		}
		
		reliableCmdManager.incommingCommand(cmd);
		
		switch (cmd.getType())
		{
			case CMD_SYSTEM_MATCH_FEEDBACK:
				latestFeedbackCmd = (TigerSystemMatchFeedback) cmd;
				battery = latestFeedbackCmd.getBatteryLevel();
				kickerLevel = latestFeedbackCmd.getKickerLevel();
				onNewFeedbackCmd(latestFeedbackCmd);
				break;
			case CMD_SYSTEM_CONSOLE_PRINT:
				notifyConsolePrint((TigerSystemConsolePrint) cmd);
				break;
			case CMD_SYSTEM_PERFORMANCE:
				setPerformance(new Performance((TigerSystemPerformance) cmd));
				break;
			case CMD_STATUS_FEEDBACK_PID:
				List<Number> nbrs = ((TigerStatusFeedbackPid) cmd).getNumberList();
				StringBuilder sb = new StringBuilder();
				for (Number n : nbrs)
				{
					sb.append(n);
					sb.append(' ');
				}
				sb.append('\n');
				String line = sb.toString();
				try
				{
					Files.write(Paths.get("data", "statusPid_" + getBotID().getNumberWithColorOffset() + ".dat"),
							line.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
				} catch (IOException err)
				{
					log.error("Could not write status pid file", err);
				}
				break;
			default:
				break;
		}
		
		rxStats.packets++;
		rxStats.payload += CommandFactory.getInstance().getLength(cmd, false);
	}
	
	
	/**
	 * @return
	 */
	@Override
	public Statistics getRxStats()
	{
		return new Statistics(rxStats);
	}
	
	
	/**
	 * @return
	 */
	@Override
	public Statistics getTxStats()
	{
		return new Statistics(txStats);
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
		return latestFeedbackCmd.getHardwareId();
	}
	
	
	/**
	 * Create a {@link TrackedTigerBot} from known feedback
	 * 
	 * @return
	 */
	public TrackedTigerBot createTrackedBot()
	{
		IVector2 pos = latestFeedbackCmd.getPosition().multiply(1000);
		IVector2 vel = latestFeedbackCmd.getVelocity();
		IVector2 acc = latestFeedbackCmd.getAcceleration();
		int height = 135;
		float angle = latestFeedbackCmd.getOrientation();
		float aVel = latestFeedbackCmd.getAngularVelocity();
		float aAcc = latestFeedbackCmd.getAngularAcceleration();
		return new TrackedTigerBot(getBotId(), pos, vel, acc, height, angle, aVel, aAcc, 1, this, getBotId()
				.getTeamColor());
	}
	
	
	@Override
	public boolean isAvailableToAi()
	{
		if (super.isAvailableToAi())
		{
			return latestFeedbackCmd.isFeatureWorking(EFeature.MOVE);
		}
		return false;
	}
	
	
	/**
	 * Sets cheering flag
	 * 
	 * @param cheering
	 */
	public void setCheering(final boolean cheering)
	{
		matchCmdMemory.cheering = cheering;
	}
	
	
	@Override
	public float getCenter2DribblerDist()
	{
		return center2DribblerDist;
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
	
	@Persistent(version = 1)
	private static class MatchCmdMemory
	{
		private transient ABotSkill	skill					= new BotSkillMotorsOff();
		private int							kickSeq				= 0;
		private float						kickDuration		= 0;
		private int							kickMode				= EKickerMode.DISARM.getId();
		private int							kickDevice			= EKickerDevice.STRAIGHT.getValue();
		private long						tUpdated				= 0;
		private float						dribbleSpeed		= 0;
		private boolean					chargeAuto			= false;
		private boolean					cheering				= false;
		private boolean					limitedVelocity	= false;
		
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("MatchCmdMemory [skill=");
			builder.append(skill.getType());
			builder.append(", kickSeq=");
			builder.append(kickSeq);
			builder.append(", kickDuration=");
			builder.append(kickDuration);
			builder.append(", kickMode=");
			builder.append(kickMode);
			builder.append(", kickDevice=");
			builder.append(kickDevice);
			builder.append(", tUpdated=");
			builder.append(tUpdated);
			builder.append(", dribbleSpeed=");
			builder.append(dribbleSpeed);
			builder.append(", chargeAuto=");
			builder.append(chargeAuto);
			builder.append(", cheering=");
			builder.append(cheering);
			builder.append("]");
			return builder.toString();
		}
	}
}
