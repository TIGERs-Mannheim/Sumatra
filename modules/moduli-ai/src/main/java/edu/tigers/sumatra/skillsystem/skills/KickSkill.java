/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.driver.DoNothingDriver;
import edu.tigers.sumatra.skillsystem.driver.IKickPathDriver;
import edu.tigers.sumatra.skillsystem.driver.KickBallTrajDriver;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * Perform straight (and chip) kicks with static and moving balls
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 *         MarkG <Mark.Geiger@dlr.de>
 */
public class KickSkill extends AMoveSkill
{
	@SuppressWarnings("unused")
	private static final Logger	log					= Logger.getLogger(KickSkill.class.getName());
	
	// ###### parameters ##########
	// ###### delay #######
	@Configurable(comment = "Delay [s] to wait before sending kick command, starting when kicking is possible")
	private static double			workingKickDelay	= 0.0;
	@Configurable(comment = "Delay [s] to wait before sending kick command, starting when kicking is possible")
	private static double			brokenKickDelay	= 0.7;
	private double						kickDelay			= workingKickDelay;
	
	
	@Configurable(comment = "If ball is moving faster than this and not in our direction, bot will wait.")
	private static double			waitBallSpeedTol	= 2;
	
	
	@Configurable(comment = "Dribble speed if receiving ball")
	private static int				defDribbleSpeed	= 10000;
	
	// ######### from construction #########
	private final DynamicPosition	receiver;
	private EKickMode					kickMode;
	private EMoveMode					moveMode;
	
	// ########## state variables ############
	
	protected long						kickTimeStart		= 0;
	private EKickerDevice			device				= EKickerDevice.STRAIGHT;
	private IKickPathDriver			currentDriver		= new DoNothingDriver();
	private double						passEndVel			= OffensiveConstants.getDefaultPassEndVel();
	private double						kickSpeed			= 8;
	private int							dribbleSpeed		= defDribbleSpeed;
	private boolean					ready4Kick			= true;
	
	/**
	 */
	public enum EKickMode
	{
		/** Calculate kickSpeed for passing to receiver */
		PASS,
		/** Full speed */
		MAX,
		/** kick such that ball will come to a stop at receiver */
		POINT,
		/** use given kickSpeed */
		FIXED_SPEED,
	}
	
	/**
	 */
	public enum EMoveMode
	{
		/** normal operation */
		NORMAL,
		/** drive slow, do not touch ball before kick */
		CHILL,
		/** no catching, just go straight to the ball */
		AGGRESSIVE,
		/** aiming not important, just get the ball off from me */
		PANIC,
		/** move slow, but allow to (double) touch ball */
		CHILL_TOUCH,
	}
	
	
	/**
	 * Kick to receiver with max force
	 * 
	 * @param receiver
	 */
	public KickSkill(final DynamicPosition receiver)
	{
		this(ESkill.KICK, receiver, EKickMode.MAX, EKickerDevice.STRAIGHT, EMoveMode.NORMAL, 0);
	}
	
	
	/**
	 * This constructor is used by GUI instanceable
	 * 
	 * @param receiver
	 * @param kickMode
	 * @param device
	 * @param moveMode
	 * @param kickSpeed
	 */
	public KickSkill(
			final DynamicPosition receiver,
			final EKickMode kickMode,
			final EKickerDevice device,
			final EMoveMode moveMode,
			final double kickSpeed)
	{
		this(ESkill.KICK, receiver, kickMode, device, moveMode, kickSpeed);
	}
	
	
	/**
	 * General constructor
	 * 
	 * @param skillName
	 * @param receiver
	 */
	protected KickSkill(final ESkill skillName,
			final DynamicPosition receiver,
			final EKickMode kickMode,
			final EKickerDevice device,
			final EMoveMode moveMode,
			final double kickSpeed)
	{
		super(skillName);
		this.receiver = receiver;
		this.kickMode = kickMode;
		this.device = device;
		this.kickSpeed = kickSpeed;
		this.moveMode = moveMode;
		currentDriver = new KickBallTrajDriver(receiver);
		currentDriver.setMoveMode(moveMode);
		setPathDriver(currentDriver);
	}
	
	
	private void generateKickCmd()
	{
		double kickLength = receiver.subtractNew(getWorldFrame().getBall().getPos()).getLength2();
		EKickerDevice usedDevice = device;
		EKickerMode mode = EKickerMode.ARM_AIM;
		int dribble = 0;
		
		if (moveMode == EMoveMode.CHILL)
		{
			mode = EKickerMode.ARM;
		}
		
		if (getBot().getBotFeatures().get(EFeature.BARRIER) == EFeatureState.KAPUT)
		{
			mode = EKickerMode.FORCE;
			kickDelay = brokenKickDelay;
		} else
		{
			kickDelay = workingKickDelay;
		}
		
		if ((device == EKickerDevice.STRAIGHT)
				&& (getBot().getBotFeatures().get(EFeature.STRAIGHT_KICKER) != EFeatureState.WORKING))
		{
			// kicker is broken, lets pass with chip
			usedDevice = EKickerDevice.CHIP;
			kickMode = EKickMode.PASS;
		} else if ((device == EKickerDevice.CHIP)
				&& (getBot().getBotFeatures().get(EFeature.CHIP_KICKER) != EFeatureState.WORKING))
		{
			// good luck
			usedDevice = EKickerDevice.STRAIGHT;
		}
		
		switch (usedDevice)
		{
			case CHIP:
			{
				ChipParams chipValues = calcChipParams(kickLength);
				kickSpeed = chipValues.getKickSpeed();
				dribble = chipValues.getDribbleSpeed();
				break;
			}
			case STRAIGHT:
			{
				kickSpeed = calcStraightKickSpeed(kickLength);
				currentDriver.setShootSpeed(kickSpeed);
				break;
			}
		}
		
		if (currentDriver.isEnableDribbler())
		{
			dribble = dribbleSpeed;
		}
		
		if (currentDriver.armKicker())
		{
			// this is only used if barrier is broken!
			if (kickTimeStart == 0)
			{
				// start time for kick
				kickTimeStart = getWorldFrame().getTimestamp();
			}
			
			boolean delayReached = (TimeUnit.NANOSECONDS
					.toMillis(getWorldFrame().getTimestamp() - kickTimeStart) >= (kickDelay * 1000));
			if (delayReached)
			{
				getBot().getMatchCtrl().setKick(kickSpeed, usedDevice, mode);
			}
		} else
		{
			getBot().getMatchCtrl().setKick(0, usedDevice, EKickerMode.DISARM);
			kickTimeStart = 0;
		}
		getBot().getMatchCtrl().setDribblerSpeed(dribble);
	}
	
	
	protected double calcStraightKickSpeed(final double length)
	{
		double kickSpeedBase;
		switch (kickMode)
		{
			case MAX:
				kickSpeedBase = 8;
				break;
			case PASS:
				kickSpeedBase = Geometry.getBallModel().getVelForDist(length, passEndVel)
						+ OffensiveConstants.getKickSpeedOffset();
				break;
			case POINT:
				kickSpeedBase = Geometry.getBallModel().getVelForDist(length, 0) + OffensiveConstants.getKickSpeedOffset();
				break;
			case FIXED_SPEED:
				return kickSpeed;
			default:
				throw new IllegalStateException();
		}
		
		IVector2 targetVel = receiver.subtractNew(getTBot().getBotKickerPos()).scaleTo(kickSpeedBase);
		IVector2 ballVel = getWorldFrame().getBall().getVel();
		
		IVector2 vel = targetVel.subtractNew(ballVel);
		return vel.getLength();
	}
	
	
	protected ChipParams calcChipParams(final double kickLength)
	{
		double acc = 9.81;
		double kickSpeed;
		
		switch (kickMode)
		{
			case PASS:
				double ballGroundDamp = 0.37;
				int bounces = 3;
				double p = 1;
				double a = 0;
				// a = 2 * (1 + p_1^2 + p_2^2 + ...), p_i: rel kickspeed
				for (int i = 0; i < bounces; i++)
				{
					a += p;
					p *= ballGroundDamp;
				}
				a *= 2;
				kickSpeed = Math.sqrt(((kickLength / 1000) * acc) / a);
				break;
			case MAX:
				kickSpeed = 8;
				break;
			case FIXED_SPEED:
				kickSpeed = this.kickSpeed;
				break;
			case POINT:
			default:
				// TODO put assumptions into model: 45deg chip angle, zero friction/damping
				kickSpeed = Math.sqrt(((kickLength / 1000) * acc) / 2);
				break;
		}
		
		return new ChipParams(kickSpeed, 0);
	}
	
	
	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		receiver.update(getWorldFrame());
		currentDriver.setRoleReady4Kick(ready4Kick);
	}
	
	
	@Override
	protected void afterDriverUpdate()
	{
		generateKickCmd();
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
		receiver.update(recv);
	}
	
	
	/**
	 * @param kickMode the kickMode to set
	 */
	public final void setKickMode(final EKickMode kickMode)
	{
		this.kickMode = kickMode;
	}
	
	
	/**
	 * @param passEndVel the passEndVel to set
	 */
	public final void setPassEndVel(final double passEndVel)
	{
		this.passEndVel = passEndVel;
	}
	
	
	/**
	 * @param moveMode
	 */
	public void setMoveMode(final EMoveMode moveMode)
	{
		this.moveMode = moveMode;
		currentDriver.setMoveMode(moveMode);
	}
	
	
	/**
	 * @return the kickSpeed
	 */
	public double getKickSpeed()
	{
		return kickSpeed;
	}
	
	
	/**
	 * @param kickSpeed the kickSpeed to set
	 */
	public void setKickSpeed(final double kickSpeed)
	{
		this.kickSpeed = kickSpeed;
		kickMode = EKickMode.FIXED_SPEED;
	}
	
	
	/**
	 * @return the dribbleSpeed
	 */
	public int getDribbleSpeed()
	{
		return dribbleSpeed;
	}
	
	
	/**
	 * @param dribbleSpeed the dribbleSpeed to set
	 */
	public void setDribbleSpeed(final int dribbleSpeed)
	{
		this.dribbleSpeed = dribbleSpeed;
	}
	
	
	/**
	 * @return
	 */
	public boolean isSkillReady4Kick()
	{
		return currentDriver.isSkillReady4Kick();
	}
	
	
	/**
	 * @param ready4Kick
	 */
	public void setRoleReady4Kick(final boolean ready4Kick)
	{
		this.ready4Kick = ready4Kick;
	}
	
	
	/**
	 * @param dest
	 */
	public void setDestForAvoidingOpponent(final IVector2 dest)
	{
		currentDriver.setDestForAvoidingOpponent(dest);
	}
	
	
	/**
	 * 
	 */
	public void unsetDestForAvoidingOpponent()
	{
		currentDriver.unsetDestForAvoidingOpponent();
	}
	
	
	/**
	 * @param pos
	 */
	public void setProtectPos(final IVector2 pos)
	{
		currentDriver.setProtectPos(pos);
	}
}
