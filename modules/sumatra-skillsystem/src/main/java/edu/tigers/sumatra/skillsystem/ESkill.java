/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableSetter;
import edu.tigers.sumatra.botmanager.botskills.EBotSkill;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.ABallArrivalSkill;
import edu.tigers.sumatra.skillsystem.skills.ASkill;
import edu.tigers.sumatra.skillsystem.skills.ATouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.ApproachAndStopBallSkill;
import edu.tigers.sumatra.skillsystem.skills.ApproachBallLineSkill;
import edu.tigers.sumatra.skillsystem.skills.BotSkillWrapperSkill;
import edu.tigers.sumatra.skillsystem.skills.CommandListSkill;
import edu.tigers.sumatra.skillsystem.skills.CriticalKeeperSkill;
import edu.tigers.sumatra.skillsystem.skills.DragBallSkill;
import edu.tigers.sumatra.skillsystem.skills.DribbleKickSkill;
import edu.tigers.sumatra.skillsystem.skills.DribbleSkill;
import edu.tigers.sumatra.skillsystem.skills.GetBallContactSkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.InterceptionSkill;
import edu.tigers.sumatra.skillsystem.skills.KeeperSkill;
import edu.tigers.sumatra.skillsystem.skills.ManualControlSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveOnPenaltyAreaSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveWithBallSkill;
import edu.tigers.sumatra.skillsystem.skills.PenaltyKeeperSkill;
import edu.tigers.sumatra.skillsystem.skills.PenaltyShootSkill;
import edu.tigers.sumatra.skillsystem.skills.PenaltyShootSkill.ERotateDirection;
import edu.tigers.sumatra.skillsystem.skills.ProtectAndMoveWithBallSkill;
import edu.tigers.sumatra.skillsystem.skills.ProtectBallSkill;
import edu.tigers.sumatra.skillsystem.skills.ProtectiveGetBallSkill;
import edu.tigers.sumatra.skillsystem.skills.PushAroundObstacleSkill;
import edu.tigers.sumatra.skillsystem.skills.RamboKeeperSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiveBallSkill;
import edu.tigers.sumatra.skillsystem.skills.RedirectBallSkill;
import edu.tigers.sumatra.skillsystem.skills.RotateWithBallSkill;
import edu.tigers.sumatra.skillsystem.skills.RotationSkill;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.test.AutoKickSampleSkill;
import edu.tigers.sumatra.skillsystem.skills.test.IdentDelaysSkill;
import edu.tigers.sumatra.skillsystem.skills.test.IdentMotorSkill;
import edu.tigers.sumatra.skillsystem.skills.test.KickSampleSkill;
import edu.tigers.sumatra.skillsystem.skills.test.LatencyIdentSkill;
import edu.tigers.sumatra.skillsystem.skills.test.LatencyTestSkill;
import edu.tigers.sumatra.skillsystem.skills.test.LocalForceSequenceSkill;
import edu.tigers.sumatra.skillsystem.skills.test.MoveBangBangSkill;
import edu.tigers.sumatra.wp.data.DynamicPosition;

import static com.github.g3force.instanceables.InstanceableClass.ic;


/**
 * An enumeration that uniquely identifies each {@link ASkill}-implementation
 */
@SuppressWarnings("squid:S1192")
public enum ESkill implements IInstanceableEnum
{
	// ************************
	// *** match skills
	// ************************

	TOUCH_KICK(ic(TouchKickSkill.class)
			.setterParam(IVector2.class, "target", "4050,0", ATouchKickSkill::setTarget)
			.setterParam(EKickerDevice.class, "device", "STRAIGHT", ATouchKickSkill::setKickerDevice)
			.setterParam(Double.TYPE, "kickSpeed", "0", ATouchKickSkill::setKickSpeed)
	),
	DRIBBLE_KICK(ic(DribbleKickSkill.class)
			.setterParam(IVector2.class, "target", "4050,0", DribbleKickSkill::setTarget)
			.setterParam(IVector2.class, "destination", "0,0", DribbleKickSkill::setDestination)
	),
	SINGLE_TOUCH_KICK(ic(SingleTouchKickSkill.class)
			.setterParam(IVector2.class, "target", "4050,0", ATouchKickSkill::setTarget)
			.setterParam(EKickerDevice.class, "device", "STRAIGHT", ATouchKickSkill::setKickerDevice)
			.setterParam(Double.TYPE, "kickSpeed", "0", ATouchKickSkill::setKickSpeed)
	),
	RECEIVE_BALL(ic(ReceiveBallSkill.class)
			.setterParam(IVector2.class, "receivingPosition", "0,0", ABallArrivalSkill::setBallReceivingPosition)
	),
	REDIRECT_BALL(ic(RedirectBallSkill.class)
			.setterParam(IVector2.class, "receivingPosition", "0,0", ABallArrivalSkill::setBallReceivingPosition)
			.setterParam(IVector2.class, "target", "6000,0", RedirectBallSkill::setTarget)
			.setterParam(EKickerDevice.class, "device", "STRAIGHT", RedirectBallSkill::setKickerDevice)
			.setterParam(Double.TYPE, "kickSpeed", "6.5", RedirectBallSkill::setKickSpeed)
	),
	APPROACH_BALL_LINE(ic(ApproachBallLineSkill.class)
			.setterParam(new InstanceableSetter<>(IVector2.class, "target", "", ApproachBallLineSkill::setTarget))
	),
	APPROACH_AND_STOP_BALL(ic(ApproachAndStopBallSkill.class)),
	PROTECT_BALL(ic(ProtectBallSkill.class)
			.setterParam(DynamicPosition.class, "protectionTarget", "0,0", ProtectBallSkill::setProtectionTarget)
	),
	PUSH_AROUND_OBSTACLE(ic(PushAroundObstacleSkill.class)
			.setterParam(DynamicPosition.class, "obstacle", "0 Y", PushAroundObstacleSkill::setObstacle)
			.setterParam(DynamicPosition.class, "target", "0,0", PushAroundObstacleSkill::setTarget)
	),
	ROTATION(ic(RotationSkill.class)
			.ctorParam(Double.TYPE, "angle", "3.0")
	),
	COMMAND_LIST(ic(CommandListSkill.class)
			.setterParam(String.class, "commandSequence", "0 vy 0.5|0 kd c|1.0 k 5|1.1 noop",
					CommandListSkill::setCommandList)
	),
	MOVE_WITH_BALL(ic(MoveWithBallSkill.class)
			.setterParam(IVector2.class, "dest", "0,0", MoveWithBallSkill::setFinalDest)
			.setterParam(Double.class, "orientation", "0", MoveWithBallSkill::setFinalOrientation)
	),
	GET_BALL_CONTACT(ic(GetBallContactSkill.class)),

	PROTECT_AND_MOVE_WITH_BALL(ic(ProtectAndMoveWithBallSkill.class)
			.setterParam(IVector2.class, "protectionTarget", "0,0", ProtectAndMoveWithBallSkill::setProtectionTarget)
	),

	ROTATE_WITH_BALL(ic(RotateWithBallSkill.class)
			.setterParam(IVector2.class, "protectionTarget", "0,0", RotateWithBallSkill::setProtectionTarget)
	),

	DRAG_BALL(ic(DragBallSkill.class)
			.setterParam(IVector2.class, "destination", "0,0", DragBallSkill::setDestination)
			.setterParam(Double.class, "targetOrientation", "0.0", DragBallSkill::setTargetOrientation)
	),

	PROTECTIVE_GET_BALL(ic(ProtectiveGetBallSkill.class)
			.setterParam(IVector2.class, "protectionTarget", "0,0", ProtectiveGetBallSkill::setProtectionTarget)
	),
	// Skills for standards

	DRIBBLE_BALL(ic(DribbleSkill.class)
			.setterParam(new InstanceableSetter<>(DynamicPosition.class, "target", "4050,0", DribbleSkill::setTargetPos))
			.setterParam(new InstanceableSetter<>(Double.TYPE, "safeDistance", "0", DribbleSkill::setSafeDistance))
	),
	INTERCEPTION(ic(InterceptionSkill.class)),
	PENALTY_SHOOT(ic(PenaltyShootSkill.class)
			.setterParam(ERotateDirection.class, "shootDirection", "CW", PenaltyShootSkill::setShootDirection)
	),
	PENALTY_KEEPER(ic(PenaltyKeeperSkill.class)
			.setterParam(DynamicPosition.class, "shooterID", "3,YELLOW", PenaltyKeeperSkill::setShooterPos)
	),
	MOVE_ON_PENALTY_AREA(ic(MoveOnPenaltyAreaSkill.class)
			.setterParam(IVector2.class, "destination", "0,0", MoveOnPenaltyAreaSkill::setDestination)
	),
	CRITICAL_KEEPER(ic(CriticalKeeperSkill.class)),
	RAMBO_KEEPER(ic(RamboKeeperSkill.class)),
	KEEPER(ic(KeeperSkill.class)),

	// ************************
	// *** utility skills
	// ************************


	LATENCY_TEST(ic(LatencyTestSkill.class)),

	LATENCY_IDENT(ic(LatencyIdentSkill.class)
			.setterParam(Double.TYPE, "amplitude", "1.0", LatencyIdentSkill::setAmplitude)
			.setterParam(Double.TYPE, "frequency", "0.3", LatencyIdentSkill::setFrequency)
			.setterParam(Double.TYPE, "duration", "10.0", LatencyIdentSkill::setDuration)),

	IDENT_MOTOR(ic(IdentMotorSkill.class)
			.ctorParam(Double.TYPE, "maxSpeedW", "25.1")
			.ctorParam(Integer.TYPE, "numSteps", "10")
	),

	IDENT_DELAYS(ic(IdentDelaysSkill.class)
			.ctorParam(Double.TYPE, "amplitude", "0.5")
			.ctorParam(Double.TYPE, "frequency", "2.0")
			.ctorParam(Double.TYPE, "runtime", "10")
	),

	MOVE_BANG_BANG(ic(MoveBangBangSkill.class)
			.ctorParam(IVector2.class, "destination", "0,0")
			.ctorParam(Double.TYPE, "orientation", "0")
			.ctorParam(EBotSkill.class, "botSkill", "LOCAL_VELOCITY")
			.ctorParam(Boolean.TYPE, "rollOut", "false")
	),

	KICK_SAMPLE(ic(KickSampleSkill.class)
			.ctorParam(IVector2.class, "kickPos", "0,0")
			.ctorParam(Double.TYPE, "targetAngle", "0")
			.ctorParam(EKickerDevice.class, "device", "STRAIGHT")
			.ctorParam(Double.TYPE, "durationMs", "10.0")
			.ctorParam(Double.TYPE, "rightOffset", "0.0")
	),

	AUTO_KICK_SAMPLE(ic(AutoKickSampleSkill.class)
			.ctorParam(DynamicPosition.class, "target", "0,0")
			.ctorParam(EKickerDevice.class, "device", "STRAIGHT")
			.ctorParam(Double.TYPE, "kickDuration", "0")
	),

	LOCAL_FORCE_SEQUENCE(ic(LocalForceSequenceSkill.class)
			.ctorParam(Double[].class, "durations", "1; 2")
			.ctorParam(Double[].class, "forces", "8; 0")
			.ctorParam(Double[].class, "directions", "1.57; 0")
			.ctorParam(Double[].class, "torques", "0; 0")
	),

	// ************************
	// *** generic skills
	// ************************

	MOVE_TO(ic(MoveToSkill.class)),
	IDLE(ic(IdleSkill.class)),
	MANUAL_CONTROL(ic(ManualControlSkill.class)),
	BOT_SKILL_WRAPPER(ic(BotSkillWrapperSkill.class)),

	;


	private final InstanceableClass<?> clazz;


	/**
	 *
	 */
	ESkill(final InstanceableClass<?> clazz)
	{
		this.clazz = clazz;
	}


	/**
	 * @return the paramImpls
	 */
	@Override
	public final InstanceableClass<?> getInstanceableClass()
	{
		return clazz;
	}


	@Override
	public IInstanceableEnum parse(String value)
	{
		return valueOf(value);
	}
}
