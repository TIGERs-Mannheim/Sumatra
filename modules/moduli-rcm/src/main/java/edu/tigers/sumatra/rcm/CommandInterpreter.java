/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.05.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.rcm;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;
import com.github.g3force.instanceables.InstanceableClass.NotCreateableException;

import edu.dhbw.mannheim.tigers.sumatra.proto.BotActionCommandProtos.BotActionCommand;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillWheelVelocity;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.control.motor.EMotorModel;
import edu.tigers.sumatra.control.motor.IMotorModel;
import edu.tigers.sumatra.control.motor.MatrixMotorModel;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVectorN;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;


/**
 * Interpreter for TigerBotV2.
 * 
 * @author AndreR
 */
public class CommandInterpreter implements IConfigObserver, ICommandInterpreter
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log					= Logger.getLogger(CommandInterpreter.class.getName());
	
	private boolean					dribblerOn			= false;
	private final ABot				bot;
	private long						lastForceKick		= 0;
	private long						lastArmKick			= 0;
	
	@Configurable(comment = "Send smoothed velocities based on acceleration")
	private static boolean			smoothedVel			= true;
	
	@Configurable(comment = "Maximum speed for dribble roll [rotations per minute]")
	private static int				maxDribbleSpeed	= 40000;
	@Configurable(comment = "Maximum kickSpeed [m/s]")
	private static double			maxKickSpeed		= 5;
	@Configurable(comment = "Maximum kickSpeed [m/s]")
	private static double			maxChipSpeed		= 5;
	
	@Configurable
	private static EMovementType	moveType				= EMovementType.LOCAL_VEL;
	@Configurable
	private static EMotorModel		motorModelType		= EMotorModel.MATRIX;
	
	@Configurable
	private static double			accDef				= 3;
	@Configurable
	private static double			accMax				= 10;
	@Configurable
	private static double			accMin				= 1;
	
	@Configurable
	private static double			dccDef				= 5;
	@Configurable
	private static double			dccMax				= 20;
	@Configurable
	private static double			dccMin				= 3;
	
	@Configurable
	private static double			speedDef				= 1.0;
	@Configurable
	private static double			speedMin				= 0.1;
	@Configurable
	private static double			speedMaxLow			= 1.5;
	@Configurable
	private static double			speedMaxHigh		= 3.5;
	
	@Configurable
	private static double			rotateDef			= 4;
	@Configurable
	private static double			rotateMin			= 1;
	@Configurable
	private static double			rotateMax			= 10;
	
	private double						speedMax				= speedMaxLow;
	
	private static IMotorModel		mm						= new MatrixMotorModel();
	
	
	private boolean					highSpeedMode		= false;
	private boolean					paused				= false;
	
	private IVector2					lastVel				= new Vector2(0, 0);
	private long						lastTimestamp		= System.nanoTime();
	
	
	private double						compassThreshold	= 0;
	private final double				triggerThreshold	= 0.001;
	
	
	private enum EMovementType
	{
		LOCAL_VEL,
		MOTOR_VEL
	}
	
	
	static
	{
		ConfigRegistration.registerClass("rcm", CommandInterpreter.class);
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param bot
	 */
	public CommandInterpreter(final ABot bot)
	{
		this.bot = bot;
		ConfigRegistration.registerConfigurableCallback("rcm", this);
		ConfigRegistration.applySpezis(this, "rcm", bot.getType().name());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param command
	 */
	@Override
	public void interpret(final BotActionCommand command)
	{
		if (paused)
		{
			return;
		}
		if (highSpeedMode)
		{
			speedMax = speedMaxHigh;
		} else
		{
			speedMax = speedMaxLow;
		}
		
		getMove(command);
		
		if (command.hasDribble() && (command.getDribble() > 0.25))
		{
			dribblerOn = true;
			final int rpm = (int) (command.getDribble() * maxDribbleSpeed);
			bot.getMatchCtrl().setDribblerSpeed(rpm);
		} else if (dribblerOn)
		{
			// disable dribbler as soon has there is no dribble signal anymore
			dribblerOn = false;
			bot.getMatchCtrl().setDribblerSpeed(0);
		}
		
		if (command.hasKickForce())
		{
			lastForceKick = System.nanoTime();
			bot.getMatchCtrl()
					.setKick(command.getKickForce() * maxKickSpeed, EKickerDevice.STRAIGHT, EKickerMode.FORCE);
		}
		if ((command.hasChipForce()))
		{
			lastForceKick = System.nanoTime();
			bot.getMatchCtrl()
					.setKick(command.getChipForce() * maxKickSpeed, EKickerDevice.CHIP, EKickerMode.FORCE);
		}
		if (command.hasKickArm())
		{
			if (bot.getBotFeatures().get(EFeature.BARRIER) == EFeatureState.WORKING)
			{
				lastForceKick = 0;
				lastArmKick = System.nanoTime();
			} else
			{
				lastForceKick = System.nanoTime();
			}
			bot.getMatchCtrl()
					.setKick(command.getKickArm() * maxKickSpeed, EKickerDevice.STRAIGHT, EKickerMode.ARM);
		}
		if ((command.hasChipArm()))
		{
			lastForceKick = 0;
			lastArmKick = System.nanoTime();
			bot.getMatchCtrl()
					.setKick(command.getChipArm() * maxKickSpeed, EKickerDevice.CHIP, EKickerMode.ARM);
		}
		
		if ((command.hasDisarm() && command.getDisarm())
				|| ((lastForceKick != 0) && ((System.nanoTime() - lastForceKick) > 2e8))
				|| ((lastArmKick != 0) && ((System.nanoTime() - lastArmKick) > 15e9)))
		{
			bot.getMatchCtrl()
					.setKick(command.getKickForce() * maxKickSpeed, EKickerDevice.STRAIGHT, EKickerMode.DISARM);
		}
	}
	
	
	private void getMove(final BotActionCommand command)
	{
		double dt = (System.nanoTime() - lastTimestamp) / 1e9;
		lastTimestamp = System.nanoTime();
		
		if (dt > 0.5)
		{
			lastVel = AVector2.ZERO_VECTOR;
			return;
		}
		
		/*
		 * X-Y-Translation
		 */
		final Vector2 setVel = new Vector2();
		
		setVel.setX(command.getTranslateX());
		setVel.setY(command.getTranslateY());
		
		// respect controller dead zone
		if ((setVel.y() < compassThreshold) && (setVel.y() > -compassThreshold))
		{
			setVel.setY(0);
		}
		
		if ((setVel.x() < compassThreshold) && (setVel.x() > -compassThreshold))
		{
			setVel.setX(0);
		}
		
		// substract COMPASS_THRESHOLD to start at 0 value after dead zone
		if (setVel.y() < 0)
		{
			setVel.setY(setVel.y() + compassThreshold);
		}
		
		if (setVel.y() > 0)
		{
			setVel.setY(setVel.y() - compassThreshold);
		}
		
		if (setVel.x() < 0)
		{
			setVel.setX(setVel.x() + compassThreshold);
		}
		
		if (setVel.x() > 0)
		{
			setVel.setX(setVel.x() - compassThreshold);
		}
		
		// scale back to full range (-1.0 - 1.0)
		setVel.multiply(1.0f / (1.0f - compassThreshold));
		
		double speed = (speedDef + ((speedMax - speedDef) * command.getAccelerate()))
				- ((speedDef - speedMin) * command.getDecelerate());
		setVel.multiply(speed);
		
		double acc;
		if (setVel.isZeroVector())
		{
			// dcc
			acc = (dccDef + ((dccMax - dccDef) * command.getDecelerate()));
		} else if (lastVel.isZeroVector()
				|| (GeoMath.angleBetweenVectorAndVector(setVel, lastVel) < AngleMath.PI_HALF))
		{
			// acc
			acc = (accDef + ((accMax - accDef) * command.getAccelerate()));
		} else
		{
			// dcc
			acc = (dccDef + ((dccMax - dccDef) * command.getDecelerate()));
		}
		
		IVector2 outVel;
		if (smoothedVel)
		{
			
			IVector2 old2New = setVel.subtractNew(lastVel);
			if (old2New.getLength() > (acc * dt))
			{
				outVel = lastVel.addNew(old2New.scaleToNew(acc * dt));
			} else
			{
				outVel = setVel;
			}
		} else
		{
			outVel = setVel;
		}
		
		lastVel = outVel;
		
		/*
		 * Left-Right-Rotation
		 */
		double rotate = command.getRotate();
		
		if ((rotate < triggerThreshold) && (rotate > -triggerThreshold))
		{
			rotate = 0;
		}
		
		// substract COMPASS_THRESHOLD to start at 0 value after dead zone
		if (rotate < 0)
		{
			rotate += triggerThreshold;
		}
		
		if (rotate > 0)
		{
			rotate -= triggerThreshold;
		}
		
		// scale back to full range (-1.0 - 1.0)
		rotate *= 1.0 / (1.0f - triggerThreshold);
		
		double rotateSpeed = (rotateDef + ((rotateMax - rotateDef) * command.getAccelerate()))
				- ((rotateDef - rotateMin) * command.getDecelerate());
		
		rotate = Math.signum(rotate) * rotate * rotate * rotateSpeed;
		
		switch (moveType)
		{
			case LOCAL_VEL:
				BotSkillLocalVelocity skill = new BotSkillLocalVelocity(outVel, rotate, bot.getMoveConstraints());
				if (smoothedVel)
				{
					skill.setAccMax(acc);
				} else
				{
					skill.setAccMax(accMax);
				}
				bot.getMatchCtrl().setSkill(skill);
				break;
			case MOTOR_VEL:
				if (mm.getType() != motorModelType)
				{
					try
					{
						mm = (IMotorModel) motorModelType.getInstanceableClass().newDefaultInstance();
					} catch (NotCreateableException err)
					{
						log.error("Could not create motor model", err);
					}
				}
				
				IVectorN motors = mm.getWheelSpeed(new Vector3(outVel, rotate));
				bot.getMatchCtrl().setSkill(new BotSkillWheelVelocity(motors.toArray()));
				break;
			default:
				break;
		}
	}
	
	
	/**
	 * send a move, dribble and kick cmd to stop everything
	 */
	@Override
	public void stopAll()
	{
		bot.getMatchCtrl().setDribblerSpeed(0);
		bot.getMatchCtrl().setKick(0, EKickerDevice.STRAIGHT, EKickerMode.DISARM);
		bot.getMatchCtrl().setSkill(new BotSkillMotorsOff());
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	@Override
	public ABot getBot()
	{
		return bot;
	}
	
	
	@Override
	public void afterApply(final IConfigClient configClient)
	{
		ConfigRegistration.applySpezis(this, "rcm", bot.getType().name());
	}
	
	
	/**
	 * @param compassThreshold the compassThreshold to set
	 */
	public void setCompassThreshold(final double compassThreshold)
	{
		this.compassThreshold = compassThreshold;
	}
	
	
	/**
	 * @return the highSpeedMode
	 */
	@Override
	public boolean isHighSpeedMode()
	{
		return highSpeedMode;
	}
	
	
	/**
	 * @param highSpeedMode the highSpeedMode to set
	 */
	@Override
	public void setHighSpeedMode(final boolean highSpeedMode)
	{
		this.highSpeedMode = highSpeedMode;
		if (highSpeedMode)
		{
			log.info("High Speed Mode activated");
		} else
		{
			log.info("High Speed Mode deactivated");
		}
	}
	
	
	/**
	 * @return the paused
	 */
	@Override
	public boolean isPaused()
	{
		return paused;
	}
	
	
	/**
	 * @param paused the paused to set
	 */
	@Override
	public void setPaused(final boolean paused)
	{
		this.paused = paused;
	}
	
	
	/**
	 * @return the compassThreshold
	 */
	@Override
	public double getCompassThreshold()
	{
		return compassThreshold;
	}
}
