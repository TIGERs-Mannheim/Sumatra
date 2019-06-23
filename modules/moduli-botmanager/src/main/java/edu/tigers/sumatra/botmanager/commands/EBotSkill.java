/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.commands;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;

import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillCircleBall;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillFastGlobalPosition;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillGlobalPosition;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillGlobalVelXyPosW;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillGlobalVelocity;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalForce;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillMotorsOff;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillPenaltyShooter;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillSine;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillWheelVelocity;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author AndreR
 */
@SuppressWarnings("squid:S1192")
public enum EBotSkill implements IInstanceableEnum
{
	/** */
	MOTORS_OFF(0, new InstanceableClass(BotSkillMotorsOff.class)),
	
	/** */
	WHEEL_VELOCITY(1, new InstanceableClass(BotSkillWheelVelocity.class,
			new InstanceableParameter(Double.TYPE, "FR", "0"),
			new InstanceableParameter(Double.TYPE, "FL", "0"),
			new InstanceableParameter(Double.TYPE, "RL", "0"),
			new InstanceableParameter(Double.TYPE, "RR", "0"))),
	
	/** */
	LOCAL_VELOCITY(2, new InstanceableClass(BotSkillLocalVelocity.class,
			new InstanceableParameter(IVector2.class, "xy", "0,0"),
			new InstanceableParameter(Double.TYPE, "w", "0"),
			new InstanceableParameter(Double.TYPE, "accMax", "3"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "50"),
			new InstanceableParameter(Double.TYPE, "jerkMax", "30"),
			new InstanceableParameter(Double.TYPE, "jerkMaxW", "500"),
			new InstanceableParameter(Double.TYPE, "dribbleSpeed", "0"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "0"),
			new InstanceableParameter(EKickerDevice.class, "kickDevice", "STRAIGHT"),
			new InstanceableParameter(EKickerMode.class, "kickMode", "DISARM"))),
	
	/** */
	GLOBAL_VELOCITY(3, new InstanceableClass(BotSkillGlobalVelocity.class,
			new InstanceableParameter(IVector2.class, "xy", "0,0"),
			new InstanceableParameter(Double.TYPE, "w", "0"),
			new InstanceableParameter(Double.TYPE, "accMax", "3"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "50"),
			new InstanceableParameter(Double.TYPE, "jerkMax", "30"),
			new InstanceableParameter(Double.TYPE, "jerkMaxW", "500"),
			new InstanceableParameter(Double.TYPE, "dribbleSpeed", "0"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "0"),
			new InstanceableParameter(EKickerDevice.class, "kickDevice", "STRAIGHT"),
			new InstanceableParameter(EKickerMode.class, "kickMode", "DISARM"))),
	
	/** */
	GLOBAL_POSITION(4, new InstanceableClass(BotSkillGlobalPosition.class,
			new InstanceableParameter(IVector2.class, "dest", "0,0"),
			new InstanceableParameter(Double.TYPE, "orient", "0"),
			new InstanceableParameter(Double.TYPE, "velMax", "3"),
			new InstanceableParameter(Double.TYPE, "velMaxW", "10"),
			new InstanceableParameter(Double.TYPE, "accMax", "3"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "50"),
			new InstanceableParameter(IVector2.class, "primeDir", "0,0"))),
	
	/**  */
	GLOBAL_VEL_XY_POS_W(5, new InstanceableClass(BotSkillGlobalVelXyPosW.class,
			new InstanceableParameter(IVector2.class, "xyVel", "0,0"),
			new InstanceableParameter(Double.TYPE, "targetAngle", "0"),
			new InstanceableParameter(Double.TYPE, "accMax", "3"),
			new InstanceableParameter(Double.TYPE, "jerkMax", "50"),
			new InstanceableParameter(Double.TYPE, "velMaxW", "10"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "30"))),
	
	/** */
	BOT_SKILL_SINE(6, new InstanceableClass(BotSkillSine.class,
			new InstanceableParameter(IVector2.class, "xyVel", "0,0"),
			new InstanceableParameter(Double.TYPE, "wVel", "0"),
			new InstanceableParameter(Double.TYPE, "freq", "1"))),
	
	/** */
	FAST_GLOBAL_POSITION(7, new InstanceableClass(BotSkillFastGlobalPosition.class,
			new InstanceableParameter(IVector2.class, "dest", "0,0"),
			new InstanceableParameter(Double.TYPE, "orient", "0"),
			new InstanceableParameter(Double.TYPE, "velMax", "3"),
			new InstanceableParameter(Double.TYPE, "velMaxW", "10"),
			new InstanceableParameter(Double.TYPE, "accMax", "3"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "50"),
			new InstanceableParameter(Double.TYPE, "accMaxFast", "5"))),
	
	CIRCLE_BALL(8, new InstanceableClass(BotSkillCircleBall.class,
			new InstanceableParameter(Double.TYPE, "speeed", "1"),
			new InstanceableParameter(Double.TYPE, "radius", "200"),
			new InstanceableParameter(Double.TYPE, "targetAngle", "0"),
			new InstanceableParameter(Double.TYPE, "friction", "0.02"),
			new InstanceableParameter(Double.TYPE, "accMax", "3"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "50"),
			new InstanceableParameter(Double.TYPE, "jerkMax", "30"),
			new InstanceableParameter(Double.TYPE, "jerkMaxW", "500"),
			new InstanceableParameter(Double.TYPE, "dribbleSpeed", "0"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "0"),
			new InstanceableParameter(EKickerDevice.class, "kickDevice", "STRAIGHT"),
			new InstanceableParameter(EKickerMode.class, "kickMode", "DISARM"))),
	
	PENALTY_SHOOTER_SKILL(9, new InstanceableClass(BotSkillPenaltyShooter.class,
			new InstanceableParameter(Double.TYPE, "targetAngle", Double.toString(AngleMath.deg2rad(18.0))),
			new InstanceableParameter(Double.TYPE, "timeToShoot", "0.05"),
			new InstanceableParameter(Double.TYPE, "approachSpeed", "0.05"),
			new InstanceableParameter(Double.TYPE, "rotationSpeed", "30.0"),
			new InstanceableParameter(Double.TYPE, "penaltyKickSpeed", "8.0"),
			new InstanceableParameter(IVector2.class, "speedInTurn", "0.1,0.1"),
			new InstanceableParameter(Double.TYPE, "accMax", "3"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "150"),
			new InstanceableParameter(Double.TYPE, "jerkMax", "300"),
			new InstanceableParameter(Double.TYPE, "jerkMaxW", "500"),
			new InstanceableParameter(Double.TYPE, "dribbleSpeed", "3000"))),
	
	/** */
	LOCAL_FORCE(10, new InstanceableClass(BotSkillLocalForce.class,
			new InstanceableParameter(IVector2.class, "xy", "0,0"),
			new InstanceableParameter(Double.TYPE, "w", "0"),
			new InstanceableParameter(Double.TYPE, "dribbleSpeed", "0"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "0"),
			new InstanceableParameter(EKickerDevice.class, "kickDevice", "STRAIGHT"),
			new InstanceableParameter(EKickerMode.class, "kickMode", "DISARM")));
	
	private final InstanceableClass clazz;
	private final int id;
	
	
	/**
	 */
	EBotSkill(final int id, final InstanceableClass clazz)
	{
		this.clazz = clazz;
		this.id = id;
	}
	
	
	/**
	 * @return the paramImpls
	 */
	@Override
	public final InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
	
	
	/**
	 * @return
	 */
	public Class<?> getClazz()
	{
		return clazz.getImpl();
	}
	
	
	/**
	 * @return
	 */
	public int getId()
	{
		return id;
	}
}
