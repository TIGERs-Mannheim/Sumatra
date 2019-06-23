/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.AroundBallDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.DoNothingDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.EMovingSpeed;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.EPathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.IKickPathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.KickBallDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.KickBallSplineDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.PushBallDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.TurnWithBallDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.ChipParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.TigerDevices;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Perform straight (and chip) kicks with static and moving balls
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 *         MarkG <Mark.Geiger@dlr.de>
 */
public class KickSkill extends AMoveSkill
{
	private static final Logger	log					= Logger.getLogger(KickSkill.class
																			.getName());
	
	// ###### parameters ##########
	// ###### delay #######
	@Configurable(comment = "Delay [s] to wait before sending kick command, starting when kicking is possible")
	private static float				workingKickDelay	= 0.0f;
	@Configurable(comment = "Delay [s] to wait before sending kick command, starting when kicking is possible")
	private static float				brokenKickDelay	= 0.7f;
	private float						kickDelay			= workingKickDelay;
	
	
	@Configurable(comment = "If ball is moving faster than this and not in our direction, bot will wait.")
	private static float				waitBallSpeedTol	= 2;
	
	@Configurable(comment = "Desired ball velocity at kicker of receiving bot")
	private static float				defaultPassEndVel	= 1.5f;
	
	@Configurable(comment = "Added to kick speed due to ball not rolling after kick")
	private static float				kickSpeedOffset	= 1.0f;
	
	@Configurable(comment = "Which type of command should be send to the bots? POS or VEL")
	private static ECommandType	cmdMode				= ECommandType.POS;
	
	@Configurable(comment = "Dribble speed if receiving ball")
	private static int				dribbleOnReceive	= 10000;
	
	
	// ######### from construction #########
	private DynamicPosition			receiver;
	private EKickMode					kickMode;
	
	// ########## state variables ############
	
	protected long						kickTimeStart		= 0;
	private int							duration				= -1;
	private EMoveMode					moveMode				= EMoveMode.NORMAL;
	private EKickerDevice			device				= EKickerDevice.STRAIGHT;
	private IKickPathDriver			currentDriver		= new DoNothingDriver();
	private float						passEndVel			= defaultPassEndVel;
	
	// ########## Pushing ####################
	private long						timeLastMoved		= System.nanoTime();
	private IVector2					lastPos				= null;
	
	/**
	 */
	public enum EKickMode
	{
		/**  */
		PASS,
		/**  */
		MAX,
		/**  */
		POINT,
		/**  */
		FIXED_DURATION,
	}
	
	/**
	 */
	public enum EMoveMode
	{
		/**  */
		NORMAL,
		/**  */
		CHILL
	}
	
	
	/**
	 * If receiver is a vector, kick in this direction with max force,
	 * if receiver is a botId, pass to this bot with appropriate force
	 * 
	 * @param receiver
	 * @param kickMode
	 */
	public KickSkill(final DynamicPosition receiver, final EKickMode kickMode)
	{
		this(ESkillName.KICK, receiver, kickMode, EMoveMode.NORMAL);
	}
	
	
	/**
	 * If receiver is a vector, kick in this direction with max force,
	 * if receiver is a botId, pass to this bot with appropriate force
	 * 
	 * @param receiver
	 * @param kickMode
	 * @param moveMode
	 */
	public KickSkill(final DynamicPosition receiver, final EKickMode kickMode, final EMoveMode moveMode)
	{
		this(ESkillName.KICK, receiver, kickMode, moveMode);
	}
	
	
	/**
	 * Kick with fixed duration
	 * 
	 * @param receiver
	 * @param moveMode
	 * @param duration
	 */
	public KickSkill(final DynamicPosition receiver, final EMoveMode moveMode, final int duration)
	{
		this(receiver, EKickMode.FIXED_DURATION, moveMode);
		this.duration = duration;
	}
	
	
	/**
	 * Kick with fixed duration
	 * 
	 * @param receiver
	 * @param kickMode
	 * @param moveMode
	 * @param duration
	 */
	public KickSkill(final DynamicPosition receiver, final EKickMode kickMode, final EMoveMode moveMode,
			final int duration)
	{
		this(ESkillName.KICK, receiver, kickMode, moveMode);
		this.duration = duration;
	}
	
	
	/**
	 * If receiver is a vector, kick in this direction with max force,
	 * if receiver is a botId, pass to this bot with appropriate force
	 * 
	 * @param skillName
	 * @param receiver
	 */
	protected KickSkill(final ESkillName skillName, final DynamicPosition receiver, final EKickMode kickMode,
			final EMoveMode moveMode)
	{
		super(skillName);
		this.moveMode = moveMode;
		this.receiver = receiver;
		this.kickMode = kickMode;
		setCommandType(cmdMode);
		getMoveCon().setDriveFast(true);
	}
	
	
	private void updatePushingCheck()
	{
		if (lastPos == null)
		{
			lastPos = getPos();
		}
		float dist2Ball = GeoMath.distancePP(getPos(), getWorldFrame().getBall().getPos());
		if (dist2Ball > 400)
		{
			// we may just wait to receive the ball. pushing only makes sense if ball is near by
			return;
		}
		if (GeoMath.distancePP(lastPos, getPos()) > 50)
		{
			timeLastMoved = System.nanoTime();
			lastPos = getPos();
		}
		if (((System.nanoTime() - timeLastMoved) > 1e9) && (currentDriver.getType() != EPathDriver.PUSH_BALL))
		{
			currentDriver = new PushBallDriver(receiver);
		}
	}
	
	
	private void updateReceiverTarget()
	{
		receiver.update(getWorldFrame());
	}
	
	
	@Override
	protected final void update(final List<ACommand> cmds)
	{
		updateReceiverTarget();
		updateDirection();
		updatePushingCheck();
		updateDriver();
		
		generateKickCmd(cmds);
	}
	
	
	private void updateDriver()
	{
		final IVector2 ballPos = getWorldFrame().getBall().getPos();
		final IVector2 ballVel = getWorldFrame().getBall().getVel();
		final TrackedTigerBot bot = getTBot();
		
		if (((ballVel.getLength2() > waitBallSpeedTol)
				&& (Math.abs(AngleMath.difference(ballVel.getAngle(), bot.getPos().subtractNew(ballPos).getAngle())) > AngleMath.PI_HALF))
				|| !AIConfig.getGeometry().getField().isPointInShape(getWorldFrame().getBall().getPos()))
		{
			// ball is fast and is not moving in our direction
			if (currentDriver.getType() != EPathDriver.DO_NOTHING)
			{
				currentDriver = new DoNothingDriver();
			}
		} else if (currentDriver.isDone() || (currentDriver.getType() == EPathDriver.DO_NOTHING))
		{
			float dist2Ball = GeoMath.distancePP(ballPos, getPos());
			boolean useSpline = false;
			if (dist2Ball < 150)
			{
				currentDriver = new TurnWithBallDriver(receiver);
			} else if (useSpline)
			{
				currentDriver = new KickBallSplineDriver(receiver);
			} else
			{
				switch (currentDriver.getType())
				{
					case AROUND_BALL:
					case CATCH_BALL:
					case TURN_WITH_BALL:
						// currentDriver = new KickBallV2Driver(receiver);
						currentDriver = new KickBallDriver(receiver);
						// if (moveMode == EMoveMode.CHILL)
						// {
						// currentDriver = new KickBallChillTrajDriver(receiver);
						// } else
						// {
						// currentDriver = new KickBallTrajDriver(receiver);
						// }
						break;
					case DO_NOTHING:
					case PUSH_BALL:
					case KICK_TRAJ:
					case KICK_CHILL_TRAJ:
					case KICK_BALL:
					case KICK_BALL_V2:
						// currentDriver = new KickBallTrajDriver(receiver);
						// currentDriver = new CatchBallDriver(receiver);
						currentDriver = new AroundBallDriver(receiver);
						break;
					default:
						throw new RuntimeException("Using wrong PathDriver in KickSkill");
				}
			}
		}
		setPathDriver(currentDriver);
	}
	
	
	@Override
	public void doCalcEntryActions(final List<ACommand> cmds)
	{
		if (getBot().getBotFeatures().get(EFeature.BARRIER) == EFeatureState.KAPUT)
		{
			kickDelay = brokenKickDelay;
		} else
		{
			kickDelay = workingKickDelay;
		}
		
		setCommandType(cmdMode);
		if (moveMode == EMoveMode.CHILL)
		{
			getMoveCon().setSpeed(EMovingSpeed.SLOW, 1.5f);
		} else
		{
			getMoveCon().setDriveFast(true);
		}
	}
	
	
	@Override
	protected void doCalcExitActions(final List<ACommand> cmds)
	{
		getDevices().allOff(cmds);
	}
	
	
	private void generateKickCmd(final List<ACommand> cmds)
	{
		float kickLength = receiver.subtractNew(getWorldFrame().getBall().getPos()).getLength2();
		EKickerDevice usedDevice = device;
		EKickerMode mode = EKickerMode.ARM;
		float kickSpeed = -1;
		int dribble = 0;
		
		if ((getBot().getBotFeatures().get(EFeature.BARRIER) == EFeatureState.KAPUT))
		{
			mode = EKickerMode.FORCE;
		}
		
		if ((device == EKickerDevice.STRAIGHT)
				&& (getBot().getBotFeatures().get(EFeature.STRAIGHT_KICKER) != EFeatureState.WORKING))
		{
			// kicker is broken, lets chip with 1/2 length :)
			kickLength /= 2;
			usedDevice = EKickerDevice.CHIP;
		} else if ((device == EKickerDevice.CHIP)
				&& (getBot().getBotFeatures().get(EFeature.CHIP_KICKER) != EFeatureState.WORKING))
		{
			usedDevice = EKickerDevice.STRAIGHT;
		} else if ((getBot().getBotFeatures().get(EFeature.STRAIGHT_KICKER) != EFeatureState.WORKING)
				&& (getBot().getBotFeatures().get(EFeature.CHIP_KICKER) != EFeatureState.WORKING))
		{
			log.warn("Kicker and Chipper of bot " + getBot().getBotID() + " is configured broken!");
			complete();
		}
		
		switch (usedDevice)
		{
			case CHIP:
			{
				ChipParams chipValues = calcChipParams(kickLength);
				duration = chipValues.getDuration();
				dribble = chipValues.getDribbleSpeed();
				if ((moveMode == EMoveMode.CHILL) && (chipValues.getDribbleSpeed() > 0))
				{
					mode = EKickerMode.DRIBBLER;
				}
				break;
			}
			case STRAIGHT:
			{
				kickSpeed = calcStraightKickSpeed(kickLength);
				currentDriver.setShootSpeed(kickSpeed);
				break;
			}
		}
		
		if (currentDriver.isReceiving() && !getTBot().hasBallContact())
		{
			dribble = dribbleOnReceive;
		}
		
		if (readyForKick())
		{
			// this is only used if barrier is broken!
			if (kickTimeStart == 0)
			{
				// start time for kick
				kickTimeStart = SumatraClock.nanoTime();
			}
			
			boolean delayReached = (TimeUnit.NANOSECONDS.toMillis(SumatraClock.nanoTime() - kickTimeStart) >= (kickDelay * 1000));
			if (delayReached)
			{
				if (kickSpeed > 0)
				{
					getDevices().kickGeneralSpeed(cmds, mode, usedDevice, kickSpeed, dribble);
				} else
				{
					getDevices().kickGeneralDuration(cmds, mode, usedDevice, duration, dribble);
				}
			}
		} else
		{
			getDevices().disarm(cmds);
			kickTimeStart = 0;
		}
		getDevices().dribble(cmds, dribble);
	}
	
	
	private boolean readyForKick()
	{
		// TODO
		IVector2 dir = receiver.subtractNew(getWorldFrame().getBall().getPos());
		float angle = Math.abs(AngleMath.getShortestRotation(dir.getAngle(), getTBot().getAngle()));
		boolean angleOk = angle < 0.2f;
		return angleOk && currentDriver.armKicker()
				&& (currentDriver.getType() != EPathDriver.PUSH_BALL);
	}
	
	
	private void updateDirection()
	{
		receiver.update(getWorldFrame());
	}
	
	
	protected float calcStraightKickSpeed(final float length)
	{
		switch (kickMode)
		{
			case MAX:
				return 8;
			case PASS:
				return AIConfig.getBallModel().getVelForDist(length, passEndVel) + kickSpeedOffset;
			case POINT:
				return AIConfig.getBallModel().getVelForDist(length, 0) + kickSpeedOffset;
			case FIXED_DURATION:
				return getDevices().calcKickSpeed(duration);
			default:
				throw new IllegalStateException();
		}
	}
	
	
	protected ChipParams calcChipParams(final float kickLength)
	{
		return TigerDevices.calcChipFastParams(kickLength);
	}
	
	
	/**
	 * @return the kickDelay
	 */
	public final float getKickDelay()
	{
		return kickDelay;
	}
	
	
	/**
	 * @param kickDelay the kickDelay to set
	 */
	public final void setKickDelay(final float kickDelay)
	{
		this.kickDelay = kickDelay;
	}
	
	
	/**
	 * @return the device
	 */
	public final EKickerDevice getDevice()
	{
		return device;
	}
	
	
	/**
	 * @param device the device to set
	 */
	public final void setDevice(final EKickerDevice device)
	{
		this.device = device;
	}
	
	
	/**
	 * Update the receiver target
	 * 
	 * @param recv
	 */
	public final void setReceiver(final DynamicPosition recv)
	{
		receiver = recv;
		updateDirection();
	}
	
	
	/**
	 * @return the kickMode
	 */
	public final EKickMode getKickMode()
	{
		return kickMode;
	}
	
	
	/**
	 * @param kickMode the kickMode to set
	 */
	public final void setKickMode(final EKickMode kickMode)
	{
		this.kickMode = kickMode;
	}
	
	
	/**
	 * @return the receiver
	 */
	public final DynamicPosition getReceiver()
	{
		return receiver;
	}
	
	
	/**
	 * @return the passEndVel
	 */
	public final float getPassEndVel()
	{
		return passEndVel;
	}
	
	
	/**
	 * @param passEndVel the passEndVel to set
	 */
	public final void setPassEndVel(final float passEndVel)
	{
		this.passEndVel = passEndVel;
	}
	
	
	/**
	 * @param duration the duration to set
	 */
	public final void setDuration(final int duration)
	{
		kickMode = EKickMode.FIXED_DURATION;
		this.duration = duration;
	}
	
	
	/**
	 * @return the duration
	 */
	public final int getDuration()
	{
		return duration;
	}
}
