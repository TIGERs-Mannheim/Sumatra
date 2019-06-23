/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 23, 2013
 * Author(s): TilmanS
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import java.util.Map;
import java.util.TimerTask;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import Jama.Matrix;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.KickerModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrameWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.grsim.GrSimConnection;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.grsim.GrSimNetworkCfg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.grsim.GrSimStatus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ABotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillGlobalPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillPositionPid;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.LimitedVelocityCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.TigerKickerKickV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSkillPositioningCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerSystemBotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IWorldFrameConsumer;
import edu.dhbw.mannheim.tigers.sumatra.util.GeneralPurposeTimer;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Bot used when playing in grSim simulator
 * Generates grSim protobuf commands
 * 
 * @author TilmanS
 */
@Persistent(version = 4)
public class GrSimBot extends SimBot implements IWorldFrameConsumer
{
	// Logger
	private static final Logger			log							= Logger.getLogger(GrSimBot.class.getName());
	
	private transient GrSimConnection	con;
	private transient GrSimNetworkCfg	grSimCfg;
	private transient GrSimStatus			status						= new GrSimStatus();
	
	private Vector2							lastDirection				= new Vector2();
	private float								lastAngularVelocity		= 0.0f;
	private float								lastCompensatedVelocity	= 0.0f;
	
	private transient Matrix				xEpsilon						= null;
	private transient Matrix				yEpsilon						= null;
	
	private int									maxCapacitorVoltage		= 0;
	private int									currentCapacitorVoltage	= 0;
	
	private float								velXBuffer					= 0.0f;
	private float								velYBuffer					= 0.0f;
	private float								velZBuffer					= 0.0f;
	
	
	// private boolean kickerDeadtimeActive = false;
	// private static final int KICKER_DEADTIME_MS = 1000;
	private static final int				KICKER_DRIBBLE_ARM_MS	= 3000;
	
	private transient SimpleWorldFrame	latestWorldFrame			= null;
	
	@Configurable(comment = "multiplied on position error for PositionController")
	private static float						positionErrorMultiplier	= 0.003f;
	
	@Configurable(comment = "Default maximum Capacitor charge for kicker")
	private static int						defaultKickerMaxCap		= 180;
	
	@Configurable
	private static boolean					useTrajectory				= true;
	
	private final transient KickerModel	kickModel					= KickerModel.forBot(EBotType.GRSIM);
	
	@Configurable(comment = "Dist [mm] - Distance between center of bot to dribbling bar")
	private static float						center2DribblerDist		= 90;
	
	@Configurable
	private static float						delay							= 0.02f;
	
	
	// private class KickerDeadTimeTimerTask extends TimerTask
	// {
	// @Override
	// public void run()
	// {
	// kickerDeadtimeActive = false;
	// currentCapacitorVoltage = maxCapacitorVoltage;
	// }
	// }
	
	
	@SuppressWarnings("unused")
	private GrSimBot()
	{
		
	}
	
	
	/**
	 * @param botConfig
	 * @throws BotInitException
	 */
	public GrSimBot(final SubnodeConfiguration botConfig) throws BotInitException
	{
		super(botConfig);
		setKickerMaxCap(defaultKickerMaxCap);
	}
	
	
	/**
	 * @param id
	 */
	public GrSimBot(final BotID id)
	{
		super(EBotType.GRSIM, id, -1, id.getTeamColor() == ETeamColor.YELLOW ? 0 : 1);
		setKickerMaxCap(defaultKickerMaxCap);
	}
	
	
	@Override
	public void setDefaultKickerMaxCap()
	{
		setKickerMaxCap(defaultKickerMaxCap);
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
		switch (cmd.getType())
		{
			case CMD_KICKER_CHARGE_AUTO:
			{
				chargeKicker((TigerKickerChargeAuto) cmd);
				break;
			}
			default:
			{
				sendGrSimCommand(cmd);
			}
		}
		
	}
	
	
	@Override
	public void newSpline(final SplinePair3D spline)
	{
		notifyNewSplineData(spline);
	}
	
	
	private void sendGrSimCommand(final ACommand cmd)
	{
		con.setId(getBotId().getNumber());
		
		switch (cmd.getType())
		{
			case CMD_MOTOR_MOVE_V2:
			{
				final TigerMotorMoveV2 move = (TigerMotorMoveV2) cmd;
				
				move.setUnusedComponents(lastDirection, lastAngularVelocity, lastCompensatedVelocity);
				
				lastDirection = move.getXY();
				lastAngularVelocity = move.getW();
				lastCompensatedVelocity = move.getV();
				
				if ((xEpsilon != null) && (yEpsilon != null))
				{
					final Matrix origCmd = new Matrix(3, 1);
					origCmd.set(0, 0, move.getXY().x);
					origCmd.set(1, 0, move.getXY().y);
					origCmd.set(2, 0, move.getW());
					
					final Matrix sendCmd = TigerBot.getMotorToCmdMatrix()
							.minus(xEpsilon.times(move.getXY().x).plus(yEpsilon.times(move.getXY().y)))
							.times(TigerBot.getCmdToMotorMatrix().times(origCmd));
					
					move.setX((float) sendCmd.get(0, 0));
					move.setY((float) sendCmd.get(1, 0));
					move.setW((float) sendCmd.get(2, 0));
				}
				
				handleMove(move, con);
				
				break;
			}
			case CMD_SKILL_POSITIONING:
			{
				TigerSkillPositioningCommand posCmd = (TigerSkillPositioningCommand) cmd;
				handlePositionMove(posCmd.getDestination(), posCmd.getOrientation(), con);
			}
				break;
			case CMD_SYSTEM_BOT_SKILL:
				TigerSystemBotSkill botSkill = (TigerSystemBotSkill) cmd;
				ABotSkill skill = botSkill.getSkill();
				switch (skill.getType())
				{
					case GLOBAL_POSITION:
						BotSkillGlobalPosition posCmd = (BotSkillGlobalPosition) skill;
						handleTrajectoryMove(posCmd.getPos(), posCmd.getOrientation(), posCmd.getT(), con);
						break;
					case LOCAL_VELOCITY:
					{
						BotSkillLocalVelocity velCmd = (BotSkillLocalVelocity) skill;
						final TigerMotorMoveV2 move = new TigerMotorMoveV2();
						move.setX(velCmd.getY());
						move.setY(-velCmd.getX());
						move.setW(velCmd.getW());
						handleMove(move, con);
					}
						break;
					case MOTORS_OFF:
					{
						final TigerMotorMoveV2 move = new TigerMotorMoveV2();
						move.setX(0);
						move.setY(0);
						move.setW(0);
						handleMove(move, con);
					}
						break;
					case POSITION_PID:
						BotSkillPositionPid pidPos = (BotSkillPositionPid) skill;
						handlePositionMove(pidPos.getPos(), pidPos.getOrientation(), con);
						break;
					case PENALTY_SHOOT:
					case TUNE_PID:
					case GLOBAL_POS_VEL:
					case GLOBAL_VELOCITY:
					case ENC_TRAIN:
					default:
						log.warn("Unhandled bot skill: " + skill.getType());
						break;
				}
				break;
			case CMD_KICKER_KICKV2:
				handleKick((TigerKickerKickV2) cmd, con);
				break;
			case CMD_KICKER_KICKV3:
				handleKick((TigerKickerKickV3) cmd, con);
				break;
			case CMD_MOTOR_DRIBBLE:
				handleDribble((TigerDribble) cmd, con);
				break;
			case CMD_CTRL_RESET:
				final TigerMotorMoveV2 move = new TigerMotorMoveV2();
				move.setX(0);
				move.setY(0);
				move.setW(0);
				handleMove(move, con);
				break;
			case CMD_SYSTEM_LIMITED_VEL:
				LimitedVelocityCommand limVelCmd = (LimitedVelocityCommand) cmd;
				getPerformance().setVelMaxOverride(limVelCmd.getMaxVelocity());
				break;
			default:
				log.error("Unhandled Command!" + cmd.getType().toString());
		}
		
		con.send();
	}
	
	
	/**
	 * @param move
	 * @param con
	 */
	private void handleMove(final TigerMotorMoveV2 move, final GrSimConnection con)
	{
		con.setWheelSpeed(false);
		if (move != null)
		{
			velXBuffer = move.getY();
			velYBuffer = -move.getX();
			velZBuffer = move.getW();
		}
		con.setVelX(velXBuffer);
		con.setVelY(velYBuffer);
		con.setVelZ(velZBuffer);
	}
	
	
	private void handleTrajectoryMove(final IVector2 dest, final float targetAngle, final float transTime,
			final GrSimConnection con)
	{
		if (latestWorldFrame != null)
		{
			TrackedTigerBot tBot = latestWorldFrame.getBot(getBotID());
			if (tBot == null)
			{
				return;
			}
			
			IVector3 localVel = handleTrajectoryMove(dest, targetAngle, transTime,
					new Vector3(tBot.getPos(), tBot.getAngle()),
					new Vector3(tBot.getVel(), tBot.getaVel()), delay, true);
			velXBuffer = localVel.x();
			velYBuffer = localVel.y();
			velZBuffer = localVel.z();
		}
		if (Float.isFinite(velXBuffer) && Float.isFinite(velYBuffer) && Float.isFinite(velZBuffer))
		{
			con.setVelX(velXBuffer);
			con.setVelY(velYBuffer);
			con.setVelZ(velZBuffer);
		} else
		{
			log.error("vel not finite!!!");
		}
	}
	
	
	private void handlePositionMove(final IVector2 dest, final float orient, final GrSimConnection con)
	{
		con.setWheelSpeed(false);
		
		if (latestWorldFrame != null)
		{
			TrackedTigerBot bot = latestWorldFrame.getBot(getBotID());
			if (bot == null)
			{
				log.warn("No bot with id " + getBotID());
				return;
			}
			
			float errorW = orient - bot.getAngle();
			velZBuffer = AngleMath.normalizeAngle(errorW) * 4;
			
			IVector2 error = dest.subtractNew(bot.getPos()).multiply(positionErrorMultiplier);
			float futureAngle = bot.getAngle() + (velZBuffer * 0.04f);
			IVector2 localVel = AiMath.convertGlobalBotVector2Local(error, futureAngle);
			velXBuffer = localVel.y();
			velYBuffer = -localVel.x();
			
		} else
		{
			velXBuffer = 0;
			velYBuffer = 0;
			velZBuffer = 0;
		}
		con.setVelX(velXBuffer);
		con.setVelY(velYBuffer);
		con.setVelZ(velZBuffer);
	}
	
	
	/**
	 * @param kick
	 * @param con
	 */
	private void handleKick(final TigerKickerKickV3 kick, final GrSimConnection con)
	{
		switch (kick.getMode())
		{
			case ARM:
			case DISARM:
				con.setKickmode(1);
				con.setKickmode(1);
				break;
			case DRIBBLER:
				log.warn("Dribble-arm not implemented atm");
				break;
			case FORCE:
				con.setKickmode(0);
				break;
			case NONE:
				break;
			default:
				break;
		}
		
		con.setKickspeedX(kick.getKickSpeed());
		con.setKickerDisarm(kick.getMode() == EKickerMode.DISARM);
		
		switch (kick.getDevice())
		{
			case STRAIGHT:
				con.setKickspeedZ(0.0f);
				break;
			case CHIP:
				con.setKickspeedZ(kick.getKickSpeed());
				break;
		}
	}
	
	
	/**
	 * @param kick
	 * @param con
	 */
	private void handleKick(final TigerKickerKickV2 kick, final GrSimConnection con)
	{
		// log.trace("Kick: " + kick.getDevice() + " " + kick.getMode() + " " + kick.getFiringDuration());
		int kickmode = kick.getMode();
		if (kickmode == 3) // dribble-arm
		{
			GeneralPurposeTimer.getInstance().schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					Thread.currentThread().setName("KickerTimerTask");
					kick.setMode(EKickerMode.ARM);
					handleKick(kick, con);
				}
			}, KICKER_DRIBBLE_ARM_MS);
			// kickmode = 1;
			return;
		}
		if (kickmode == 0) // Force
		{
			con.setKickmode(0);
		} else if ((kickmode == 1) || (kickmode == 2)) // Arm || Disarm
		{
			con.setKickmode(1);
		}
		
		switch (kick.getDevice())
		{
			case TigerKickerKickV2.Device.STRAIGHT:
			{
				boolean disarm = false;
				if ((kickmode == 1) || (kickmode == 0)) // Force || Arm
				{
					// if (kickerDeadtimeActive)
					// {
					// con.setKickspeedX(0.0f);
					// con.setKickspeedZ(0.0f);
					// } else
					// {
					// kickerDeadtimeActive = true;
					float firingDuration = kick.getFiringDuration();
					if (firingDuration > 10000.0f)
					{
						firingDuration = 10000.0f;
					}
					// float kickspeed = ((maxCapacitorVoltage * 0.02f) * (firingDuration * 0.0001f))
					// + (kick.getLevel() * 0);
					float kickspeed = kickModel.getKickSpeed(firingDuration);
					con.setKickspeedX(kickspeed);
					con.setKickspeedZ(0.0f);
					
					// currentCapacitorVoltage = 0;
					// GeneralPurposeTimer.getInstance().schedule(new KickerDeadTimeTimerTask(), KICKER_DEADTIME_MS);
					// }
				} else if (kick.getMode() == 2)
				{
					disarm = true;
				}
				con.setKickerDisarm(disarm);
				break;
			}
			case TigerKickerKickV2.Device.CHIP:
			{
				boolean disarm = false;
				if ((kickmode == 1) || (kickmode == 0)) // Force || Arm
				{
					// if (kickerDeadtimeActive)
					// {
					// con.setKickspeedX(0.0f);
					// con.setKickspeedZ(0.0f);
					// } else
					// {
					// kickerDeadtimeActive = true;
					float firingDuration = kick.getFiringDuration();
					if (firingDuration > 10000.0f)
					{
						firingDuration = 10000.0f;
					}
					float kickspeed = (((maxCapacitorVoltage * 0.045f) * (firingDuration * 0.0001f)) / 2)
							+ (kick.getLevel() * 0); // TODO something smarter would be great...
					con.setKickspeedZ(kickspeed);
					con.setKickspeedX(kickspeed);
					
					// currentCapacitorVoltage = 0;
					// GeneralPurposeTimer.getInstance().schedule(new KickerDeadTimeTimerTask(), KICKER_DEADTIME_MS);
					// }
				} else if (kick.getMode() == 2)
				{
					disarm = true;
				}
				con.setKickerDisarm(disarm);
				break;
			}
			default:
				break;
		}
	}
	
	
	/**
	 * @param dribble
	 * @param con
	 */
	private void handleDribble(final TigerDribble dribble, final GrSimConnection con)
	{
		if (Math.abs(dribble.getSpeed()) > 0.0)
		{
			con.setSpinner(true);
			con.setSpinnerSpeed(dribble.getSpeed());
		} else
		{
			con.setSpinner(false);
		}
	}
	
	
	private void chargeKicker(final TigerKickerChargeAuto cmd)
	{
		maxCapacitorVoltage = cmd.getMax();
		currentCapacitorVoltage = maxCapacitorVoltage;
	}
	
	
	@Override
	public void start()
	{
		con.open();
		AWorldPredictor wp;
		try
		{
			wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find WP module", err);
		}
		super.start();
	}
	
	
	@Override
	public void stop()
	{
		super.stop();
		AWorldPredictor wp;
		try
		{
			wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.removeWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find WP module", err);
		}
		con.close();
	}
	
	
	@Override
	public float getBatteryLevel()
	{
		return 15.5f;
	}
	
	
	@Override
	public float getKickerLevel()
	{
		return currentCapacitorVoltage;
	}
	
	
	@Override
	public float getBatteryLevelMax()
	{
		return 16f;
	}
	
	
	@Override
	public float getBatteryLevelMin()
	{
		return 14f;
	}
	
	
	/**
	 * @param grSimCfg
	 */
	public void setGrSimCfg(final GrSimNetworkCfg grSimCfg)
	{
		this.grSimCfg = grSimCfg;
		con = new GrSimConnection(grSimCfg);
	}
	
	
	/**
	 * @return the grSimCfg
	 */
	public final GrSimNetworkCfg getGrSimCfg()
	{
		return grSimCfg;
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrame)
	{
		latestWorldFrame = wFrame.getSimpleWorldFrame();
	}
	
	
	@Override
	public void onStop()
	{
	}
	
	
	@Override
	public float getKickerLevelMax()
	{
		return 200;
	}
	
	
	@Override
	public boolean isAvailableToAi()
	{
		if (super.isAvailableToAi())
		{
			return getNetworkState() == ENetworkState.ONLINE;
		}
		return false;
	}
	
	
	/**
	 * @return the status
	 */
	public final GrSimStatus getStatus()
	{
		return status;
	}
	
	
	@Override
	public float getCenter2DribblerDist()
	{
		return center2DribblerDist;
	}
}
