/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 * @author: Arne
 */

package edu.tigers.sumatra.botmanager.commands.botskills;

import static java.lang.Math.abs;

import org.apache.commons.lang.Validate;

import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.data.DriveLimits;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Penalty Shooter Bot Skill
 */
public class BotSkillPenaltyShooter extends ABotSkill
{
	
	@SerialData(type = SerialData.ESerialDataType.INT16)
	private int targetAngle = 0;
	
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int timeToShoot = 0;
	
	@SerialData(type = SerialData.ESerialDataType.UINT16)
	private int approachSpeed = 0;
	
	@SerialData(type = SerialData.ESerialDataType.INT8)
	private int speedInTurnX = 0;
	
	@SerialData(type = SerialData.ESerialDataType.INT8)
	private int speedInTurnY = 0;
	
	@SerialData(type = SerialData.ESerialDataType.INT16)
	private int rotationSpeed = 0;
	
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int penaltyKickSpeed = 250;
	
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int dribblerSpeed = 100;
	
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int accMax = 0;
	
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int accMaxW = 0;
	
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int jerkMax = 0;
	
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int jerkMaxW = 0;
	
	
	/**
	 * Create PenaltyShooter Bot Skill
	 * 
	 * @param angle Angle to turn
	 * @param timeToShoot Time to wait to confuse opposing keeper
	 * @param approachSpeed local speed to approach the ball
	 * @param rotationSpeed rotation speed
	 * @param penaltyKickSpeed kick speed
	 * @param speedInTurn additional translation velocity component when turning with ball
	 * @param accMax maximum acceleration in translational direction
	 * @param accMaxW maximum acceleration in rotational direction
	 * @param jerkMax maximum jerk in translational direction
	 * @param jerkMaxW maximum jerk in rotational direction
	 * @param dribbleSpeed
	 */
	@SuppressWarnings("squid:S00107")
	public BotSkillPenaltyShooter(final double angle, final double timeToShoot, final double approachSpeed,
			final double rotationSpeed, final double penaltyKickSpeed, final IVector2 speedInTurn, final double accMax,
			final double accMaxW,
			final double jerkMax, final double jerkMaxW, final double dribbleSpeed)
	{
		this();
		setTimeToShoot(timeToShoot);
		setTargetAngle(angle);
		setApproachSpeed(approachSpeed);
		setRotationSpeed(rotationSpeed);
		setSpeedInTurn(speedInTurn);
		setPenaltyKickSpeed(penaltyKickSpeed);
		setAccMax(accMax);
		setAccMaxW(accMaxW);
		setJerkMax(jerkMax);
		setJerkMaxW(jerkMaxW);
		setDribblerSpeed(dribbleSpeed);
	}
	
	
	/**
	 * Create a new penalty shooter
	 */
	public BotSkillPenaltyShooter()
	{
		super(EBotSkill.PENALTY_SHOOTER_SKILL);
	}
	
	
	public void setRotationSpeed(final double rotationSpeed)
	{
		this.rotationSpeed = (int) (1e3 * rotationSpeed);
	}
	
	
	/**
	 * Max: 10m/s²
	 *
	 * @param val
	 */
	public final void setAccMax(final double val)
	{
		accMax = DriveLimits.toUInt8(val, DriveLimits.MAX_ACC);
	}
	
	
	/**
	 * Max: 100rad/s²
	 *
	 * @param val
	 */
	public final void setAccMaxW(final double val)
	{
		accMaxW = DriveLimits.toUInt8(val, DriveLimits.MAX_ACC_W);
	}
	
	
	/**
	 * Max: 10m/s²
	 *
	 * @param val
	 */
	public final void setJerkMax(final double val)
	{
		jerkMax = DriveLimits.toUInt8(val, DriveLimits.MAX_JERK);
	}
	
	
	/**
	 * Max: 100rad/s²
	 *
	 * @param val
	 */
	public final void setJerkMaxW(final double val)
	{
		jerkMaxW = DriveLimits.toUInt8(val, DriveLimits.MAX_JERK_W);
	}
	
	
	/**
	 * Set translational motion while in turn motion
	 * 
	 * @param speed
	 */
	public void setSpeedInTurn(final IVector2 speed)
	{
		speedInTurnX = (int) (speed.x() * 1e2);
		speedInTurnY = (int) (speed.y() * 1e2);
	}
	
	
	public void setDribblerSpeed(final double dribblerSpeed)
	{
		this.dribblerSpeed = (int) (1e-3 * dribblerSpeed);
	}
	
	
	public IVector2 getSpeedInTurn()
	{
		return Vector2.fromXY(speedInTurnX, speedInTurnY).multiply(1e-2);
	}
	
	
	public void setPenaltyKickSpeed(final double penaltyKickSpeed)
	{
		this.penaltyKickSpeed = (int) ((penaltyKickSpeed / 0.04) + 0.5);
	}
	
	
	public double getTimeToShoot()
	{
		return timeToShoot * 0.1;
	}
	
	
	public void setTimeToShoot(final double timeToShoot)
	{
		this.timeToShoot = (int) (timeToShoot * 10);
	}
	
	
	public double getTargetAngle()
	{
		return targetAngle * 1e-3;
	}
	
	
	public void setTargetAngle(final double targetAngle)
	{
		this.targetAngle = (int) (1e3 * targetAngle);
	}
	
	
	public void setApproachSpeed(final double approachSpeed)
	{
		this.approachSpeed = (int) (1e3 * approachSpeed);
	}
	
	
	public double getApproachSpeed()
	{
		return approachSpeed * 1e-3;
	}
	
	
	public double getRotationSpeed()
	{
		return 1e-3 * rotationSpeed;
	}
	
	
	public double getPenaltyKickSpeed()
	{
		return penaltyKickSpeed * 0.04;
	}
	
	/**
	 * Builder for penalty shooter bot skill
	 */
	public static final class Builder
	{
		private double timeToShoot;
		private double targetAngle;
		private double approachSpeed;
		private double rotationSpeed;
		private double penaltyKickSpeed;
		private IVector2 speedInTurn;
		private double accMax = 3.0;
		private double accMaxW = 50;
		private double jerkMax = 30;
		private double jerkMaxW = 500;
		private double dribbleSpeed = 1500;
		
		
		/**
		 * create a builder
		 * 
		 * @return
		 */
		public static Builder create()
		{
			return new Builder();
		}
		
		
		/**
		 * dribble speed
		 * 
		 * @param dribbleSpeed
		 * @return
		 */
		public Builder dribbleSpeed(final double dribbleSpeed)
		{
			this.dribbleSpeed = dribbleSpeed;
			return this;
		}
		
		
		/**
		 * time to shoot
		 * 
		 * @param timeToShoot
		 * @return
		 */
		public Builder timeToShoot(final double timeToShoot)
		{
			this.timeToShoot = timeToShoot;
			return this;
		}
		
		
		/**
		 * Set additional speed while in quick turn motion
		 * 
		 * @param speedInTurn
		 * @return
		 */
		public Builder speedInTurn(final IVector2 speedInTurn)
		{
			this.speedInTurn = speedInTurn;
			return this;
		}
		
		
		/**
		 * target angle
		 * 
		 * @param targetAngle
		 * @return
		 */
		public Builder targetAngle(final double targetAngle)
		{
			this.targetAngle = targetAngle;
			return this;
		}
		
		
		/**
		 * apprach speed
		 * 
		 * @param approachSpeed
		 * @return
		 */
		public Builder approachSpeed(final double approachSpeed)
		{
			this.approachSpeed = approachSpeed;
			return this;
		}
		
		
		/**
		 * rotation speed
		 * 
		 * @param rotationSpeed
		 * @return
		 */
		public Builder rotationSpeed(final double rotationSpeed)
		{
			this.rotationSpeed = rotationSpeed;
			return this;
		}
		
		
		/**
		 * penalty kick speed
		 * 
		 * @param penaltyKickSpeed
		 * @return
		 */
		public Builder penaltyKickSpeed(final double penaltyKickSpeed)
		{
			this.penaltyKickSpeed = penaltyKickSpeed;
			return this;
		}
		
		
		/**
		 * limit for translational acceleration
		 * 
		 * @param accMax
		 * @return
		 */
		public Builder accMax(final double accMax)
		{
			this.accMax = accMax;
			return this;
		}
		
		
		/**
		 * limit for angular acceleration
		 * 
		 * @param accMaxW
		 * @return
		 */
		public Builder accMaxW(final double accMaxW)
		{
			this.accMaxW = accMaxW;
			return this;
		}
		
		
		/**
		 * limit for translational jerk
		 * 
		 * @param jerkMax
		 * @return
		 */
		public Builder jerkMax(final double jerkMax)
		{
			this.jerkMax = jerkMax;
			return this;
		}
		
		
		/**
		 * limit for rotational jerk
		 * 
		 * @param jerkMaxW
		 * @return
		 */
		public Builder jerkMaxW(final double jerkMaxW)
		{
			this.jerkMaxW = jerkMaxW;
			return this;
		}
		
		
		/**
		 * validate
		 */
		private void validate()
		{
			Validate.isTrue(timeToShoot > 0);
			Validate.isTrue(approachSpeed > 0);
			Validate.isTrue(abs(rotationSpeed) > 0);
			Validate.notNull(speedInTurn);
			Validate.isTrue(penaltyKickSpeed > 0);
		}
		
		
		/**
		 * build a new penalty shooter bot skill
		 * 
		 * @return
		 */
		public BotSkillPenaltyShooter build()
		{
			validate();
			return new BotSkillPenaltyShooter(targetAngle, timeToShoot, approachSpeed,
					rotationSpeed, penaltyKickSpeed, speedInTurn,
					accMax, accMaxW, jerkMax, jerkMaxW, dribbleSpeed);
		}
	}
}
