/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.botskills;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerMode;
import edu.tigers.sumatra.math.AngleMath;
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
			new InstanceableParameter(Double.TYPE, "dribbleCurrent", "3"),
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
			new InstanceableParameter(Double.TYPE, "dribbleCurrent", "3"),
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
			new InstanceableParameter(Double.TYPE, "dribbleCurrent", "3"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "0"),
			new InstanceableParameter(EKickerDevice.class, "kickDevice", "STRAIGHT"),
			new InstanceableParameter(EKickerMode.class, "kickMode", "DISARM"))),

	PENALTY_SHOOTER_SKILL(9, new InstanceableClass<>(BotSkillPenaltyShooter.class,
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

	LOCAL_FORCE(10, new InstanceableClass<>(BotSkillLocalForce.class,
			new InstanceableParameter(IVector2.class, "xy", "0,0"),
			new InstanceableParameter(Double.TYPE, "w", "0"),
			new InstanceableParameter(Double.TYPE, "dribbleSpeed", "0"),
			new InstanceableParameter(Double.TYPE, "dribbleCurrent", "3"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "0"),
			new InstanceableParameter(EKickerDevice.class, "kickDevice", "STRAIGHT"),
			new InstanceableParameter(EKickerMode.class, "kickMode", "DISARM"))),

	GET_BALL(11, new InstanceableClass<>(BotSkillGetBall.class,
			new InstanceableParameter(IVector2.class, "searchOrigin", "0,0"),
			new InstanceableParameter(Double.TYPE, "searchRadius", "0.0"),
			new InstanceableParameter(Double.TYPE, "velMax", "1.5"),
			new InstanceableParameter(Double.TYPE, "velMaxW", "10"),
			new InstanceableParameter(Double.TYPE, "accMax", "2.0"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "20"),
			new InstanceableParameter(Double.TYPE, "dribblerSpeed", "18000"),
			new InstanceableParameter(Double.TYPE, "dribblerCurrent", "3"),
			new InstanceableParameter(Double.TYPE, "rotationSpeed", "1.0"),
			new InstanceableParameter(Double.TYPE, "dockingSpeed", "0.3"),
			new InstanceableParameter(Double.TYPE, "aimSpeed", "0.6"))),


	INTERCEPT_BALL(12, new InstanceableClass<>(BotSkillInterceptBall.class,
			new InstanceableParameter(IVector2.class, "interceptPos", "0,0"),
			new InstanceableParameter(Double.TYPE, "interceptOrient", "0.0"),
			new InstanceableParameter(Boolean.TYPE, "usePose", "false"),
			new InstanceableParameter(Double.TYPE, "moveRadius", "0.0"),
			new InstanceableParameter(Double.TYPE, "velMax", "1.5"),
			new InstanceableParameter(Double.TYPE, "velMaxW", "10"),
			new InstanceableParameter(Double.TYPE, "accMax", "2.0"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "50"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "0"),
			new InstanceableParameter(EKickerDevice.class, "kickDevice", "STRAIGHT"),
			new InstanceableParameter(EKickerMode.class, "kickMode", "DISARM"))),

	KEEPER(13, new InstanceableClass<>(BotSkillKeeper.class,
			new InstanceableParameter(IVector2.class, "ballPos", "0,0"),
			new InstanceableParameter(IVector2.class, "ballVel", "0,0"),
			new InstanceableParameter(Double.TYPE, "penAreaDepth", "1000"),
			new InstanceableParameter(Double.TYPE, "goalWidth", "1000"),
			new InstanceableParameter(Double.TYPE, "goalOffset", "2000"),
			new InstanceableParameter(Double.TYPE, "velMax", "3"),
			new InstanceableParameter(Double.TYPE, "velMaxW", "10"),
			new InstanceableParameter(Double.TYPE, "accMax", "3"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "50"))),

	TC2022_STAGE1(14, new InstanceableClass<>(BotSkillTechChallenge2022Stage1.class,
			new InstanceableParameter(IVector2.class, "startPos", "0,0"),
			new InstanceableParameter(IVector2.class, "targetPos", "0,0"),
			new InstanceableParameter(Double.TYPE, "velMax", "1.5"),
			new InstanceableParameter(Double.TYPE, "velMaxW", "10"),
			new InstanceableParameter(Double.TYPE, "accMax", "2.0"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "20"),
			new InstanceableParameter(Double.TYPE, "dribblerSpeed", "18000"),
			new InstanceableParameter(Double.TYPE, "dribblerCurrent", "3"),
			new InstanceableParameter(Double.TYPE, "rotationSpeed", "1.0"))),

	TC2022_STAGE2(15, new InstanceableClass<>(BotSkillTechChallenge2022Stage2.class,
			new InstanceableParameter(IVector2.class, "startPos", "0,0"),
			new InstanceableParameter(IVector2.class, "targetPos", "0,0"),
			new InstanceableParameter(Double.TYPE, "velMax", "1.5"),
			new InstanceableParameter(Double.TYPE, "velMaxW", "10"),
			new InstanceableParameter(Double.TYPE, "accMax", "2.0"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "20"),
			new InstanceableParameter(Double.TYPE, "dribblerSpeed", "18000"),
			new InstanceableParameter(Double.TYPE, "dribblerCurrent", "3"),
			new InstanceableParameter(Double.TYPE, "rotationSpeed", "1.0"))),
	TC2022_STAGE3(16, new InstanceableClass<>(BotSkillTechChallenge2022Stage3.class,
			new InstanceableParameter(IVector2.class, "startPos", "0,0"),
			new InstanceableParameter(IVector2.class, "targetPos", "0,0"),
			new InstanceableParameter(Double.TYPE, "velMax", "1.5"),
			new InstanceableParameter(Double.TYPE, "velMaxW", "10"),
			new InstanceableParameter(Double.TYPE, "accMax", "2.0"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "20"),
			new InstanceableParameter(Double.TYPE, "dribblerSpeed", "18000"),
			new InstanceableParameter(Double.TYPE, "dribblerCurrent", "3"),
			new InstanceableParameter(Double.TYPE, "rotationSpeed", "1.0"))),

	TC2022_STAGE4(17, new InstanceableClass<>(BotSkillTechChallenge2022Stage4.class,
			new InstanceableParameter(IVector2.class, "startPos", "0,0"),
			new InstanceableParameter(IVector2.class, "targetPos", "0,0"),
			new InstanceableParameter(Double.TYPE, "velMax", "1.5"),
			new InstanceableParameter(Double.TYPE, "velMaxW", "10"),
			new InstanceableParameter(Double.TYPE, "accMax", "2.0"),
			new InstanceableParameter(Double.TYPE, "accMaxW", "20"),
			new InstanceableParameter(Double.TYPE, "dribblerSpeed", "18000"),
			new InstanceableParameter(Double.TYPE, "dribblerCurrent", "3"),
			new InstanceableParameter(Double.TYPE, "rotationSpeed", "1.0"))),

	RECEIVE_BALL(18, ic(BotSkillReceiveBall.class)
			.setterParam(IVector2.class, "receivePos", "0,0", BotSkillReceiveBall::setReceivePos)
			.setterParam(Double.TYPE, "receiveOrientation", "0.0", BotSkillReceiveBall::setReceiveOrientation)
			.setterParam(Double.TYPE, "velMaxW", "10", BotSkillReceiveBall::setVelMaxW)
			.setterParam(Double.TYPE, "accMax", "2.0", BotSkillReceiveBall::setAccMax)
			.setterParam(Double.TYPE, "accMaxW", "50", BotSkillReceiveBall::setAccMaxW)
			.setterParam(Double.TYPE, "kickSpeed", "0", BotSkillReceiveBall::setKickSpeed)
			.setterParam(EKickerDevice.class, "kickDevice", "STRAIGHT", BotSkillReceiveBall::setKickerDevice)
			.setterParam(EKickerMode.class, "kickMode", "DISARM", BotSkillReceiveBall::setKickerMode)
			.setterParam(IVector2.class, "ballPos", "0,0", BotSkillReceiveBall::setBallPos)
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
