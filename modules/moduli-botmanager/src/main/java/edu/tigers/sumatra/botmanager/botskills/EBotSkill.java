/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.botskills;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.github.g3force.instanceables.InstanceableClass.ic;


/**
 * @author AndreR
 */
@SuppressWarnings("squid:S1192")
@Getter
@AllArgsConstructor
public enum EBotSkill implements IInstanceableEnum
{
	MOTORS_OFF(0, new InstanceableClass<>(BotSkillMotorsOff.class)),

	WHEEL_VELOCITY(1, new InstanceableClass<>(BotSkillWheelVelocity.class,
			new InstanceableParameter(Double.TYPE, "FR", "0"),
			new InstanceableParameter(Double.TYPE, "FL", "0"),
			new InstanceableParameter(Double.TYPE, "RL", "0"),
			new InstanceableParameter(Double.TYPE, "RR", "0"))),

	LOCAL_VELOCITY(2, new InstanceableClass<>(BotSkillLocalVelocity.class,
			new InstanceableParameter(IVector2.class, "xy", "0,0"),
			new InstanceableParameter(Double.TYPE, "w", "0"),
			new InstanceableParameter(Double.TYPE, "accMax", "3"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "50"),
			new InstanceableParameter(Double.TYPE, "jerkMax", "30"),
			new InstanceableParameter(Double.TYPE, "jerkMaxW", "500"),
			new InstanceableParameter(Double.TYPE, "dribbleSpeed", "0"),
			new InstanceableParameter(Double.TYPE, "dribbleForce", "3"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "0"),
			new InstanceableParameter(EKickerDevice.class, "kickDevice", "STRAIGHT"),
			new InstanceableParameter(EKickerMode.class, "kickMode", "DISARM"))),

	GLOBAL_VELOCITY(3, new InstanceableClass<>(BotSkillGlobalVelocity.class,
			new InstanceableParameter(IVector2.class, "xy", "0,0"),
			new InstanceableParameter(Double.TYPE, "w", "0"),
			new InstanceableParameter(Double.TYPE, "accMax", "3"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "50"),
			new InstanceableParameter(Double.TYPE, "jerkMax", "30"),
			new InstanceableParameter(Double.TYPE, "jerkMaxW", "500"),
			new InstanceableParameter(Double.TYPE, "dribbleSpeed", "0"),
			new InstanceableParameter(Double.TYPE, "dribbleForce", "3"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "0"),
			new InstanceableParameter(EKickerDevice.class, "kickDevice", "STRAIGHT"),
			new InstanceableParameter(EKickerMode.class, "kickMode", "DISARM"))),

	GLOBAL_POSITION(4, new InstanceableClass<>(BotSkillGlobalPosition.class,
			new InstanceableParameter(IVector2.class, "dest", "0,0"),
			new InstanceableParameter(Double.TYPE, "orient", "0"),
			new InstanceableParameter(Double.TYPE, "velMax", "3"),
			new InstanceableParameter(Double.TYPE, "velMaxW", "10"),
			new InstanceableParameter(Double.TYPE, "accMax", "3"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "50"),
			new InstanceableParameter(IVector2.class, "primeDir", "0,0"))),

	GLOBAL_VEL_XY_POS_W(5, new InstanceableClass<>(BotSkillGlobalVelXyPosW.class,
			new InstanceableParameter(IVector2.class, "xyVel", "0,0"),
			new InstanceableParameter(Double.TYPE, "targetAngle", "0"),
			new InstanceableParameter(Double.TYPE, "accMax", "3"),
			new InstanceableParameter(Double.TYPE, "jerkMax", "50"),
			new InstanceableParameter(Double.TYPE, "velMaxW", "10"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "30"))),

	BOT_SKILL_SINE(6, new InstanceableClass<>(BotSkillSine.class,
			new InstanceableParameter(IVector2.class, "xyVel", "0,0"),
			new InstanceableParameter(Double.TYPE, "wVel", "0"),
			new InstanceableParameter(Double.TYPE, "freq", "1"))),

	FAST_GLOBAL_POSITION(7, new InstanceableClass<>(BotSkillFastGlobalPosition.class,
			new InstanceableParameter(IVector2.class, "dest", "0,0"),
			new InstanceableParameter(Double.TYPE, "orient", "0"),
			new InstanceableParameter(Double.TYPE, "velMax", "3"),
			new InstanceableParameter(Double.TYPE, "velMaxW", "10"),
			new InstanceableParameter(Double.TYPE, "accMax", "3"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "50"),
			new InstanceableParameter(Double.TYPE, "accMaxFast", "5"))),

	CIRCLE_BALL(8, new InstanceableClass<>(BotSkillCircleBall.class,
			new InstanceableParameter(Double.TYPE, "speeed", "1"),
			new InstanceableParameter(Double.TYPE, "radius", "200"),
			new InstanceableParameter(Double.TYPE, "targetAngle", "0"),
			new InstanceableParameter(Double.TYPE, "friction", "0.02"),
			new InstanceableParameter(Double.TYPE, "accMax", "3"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "50"),
			new InstanceableParameter(Double.TYPE, "jerkMax", "30"),
			new InstanceableParameter(Double.TYPE, "jerkMaxW", "500"),
			new InstanceableParameter(Double.TYPE, "dribbleSpeed", "0"),
			new InstanceableParameter(Double.TYPE, "dribbleForce", "3"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "0"),
			new InstanceableParameter(EKickerDevice.class, "kickDevice", "STRAIGHT"),
			new InstanceableParameter(EKickerMode.class, "kickMode", "DISARM"))),

	LOCAL_FORCE(10, new InstanceableClass<>(BotSkillLocalForce.class,
			new InstanceableParameter(IVector2.class, "xy", "0,0"),
			new InstanceableParameter(Double.TYPE, "w", "0"),
			new InstanceableParameter(Double.TYPE, "dribbleSpeed", "0"),
			new InstanceableParameter(Double.TYPE, "dribbleForce", "3"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "0"),
			new InstanceableParameter(EKickerDevice.class, "kickDevice", "STRAIGHT"),
			new InstanceableParameter(EKickerMode.class, "kickMode", "DISARM"))),

	KICK_BALL(19, ic(BotSkillKickBall.class)
			.setterParam(IVector2.class, "ballPos", "0,0", BotSkillKickBall::setBallPos)
			.setterParam(IVector2.class, "targetPos", "0,0", BotSkillKickBall::setTargetPos)
			.setterParam(IVector2.class, "fieldSize", "0,0", BotSkillKickBall::setFieldSize)
			.setterParam(Double.TYPE, "velMax", "3", BotSkillKickBall::setVelMax)
			.setterParam(Double.TYPE, "velMaxW", "10", BotSkillKickBall::setVelMaxW)
			.setterParam(Double.TYPE, "accMax", "3", BotSkillKickBall::setAccMax)
			.setterParam(Double.TYPE, "accMaxW", "50", BotSkillKickBall::setAccMaxW)
			.setterParam(Double.TYPE, "dribblerSpeed", "5", BotSkillKickBall::setDribblerSpeed)
			.setterParam(Double.TYPE, "dribblerForce", "3", BotSkillKickBall::setDribblerForce)
			.setterParam(Double.TYPE, "kickSpeed", "0", BotSkillKickBall::setKickSpeed)
			.setterParam(EKickerDevice.class, "kickDevice", "STRAIGHT", BotSkillKickBall::setKickerDevice)
			.setterParam(EKickerMode.class, "kickMode", "DISARM", BotSkillKickBall::setKickerMode)
	),

	;

	private final int id;
	private final InstanceableClass<?> instanceableClass;


	@Override
	public IInstanceableEnum parse(String value)
	{
		return valueOf(value);
	}
}
