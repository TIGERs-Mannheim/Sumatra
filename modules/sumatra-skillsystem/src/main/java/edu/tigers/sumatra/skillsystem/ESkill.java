/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
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
import edu.tigers.sumatra.skillsystem.skills.CriticalKeeperSkill;
import edu.tigers.sumatra.skillsystem.skills.DropBallSkill;
import edu.tigers.sumatra.skillsystem.skills.GetBallContactSkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.InterceptionSkill;
import edu.tigers.sumatra.skillsystem.skills.ManualControlSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveOnShapeBoundarySkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveWithBallSkill;
import edu.tigers.sumatra.skillsystem.skills.ProtectBallSkill;
import edu.tigers.sumatra.skillsystem.skills.ProtectiveGetBallSkill;
import edu.tigers.sumatra.skillsystem.skills.RamboKeeperSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiveBallSkill;
import edu.tigers.sumatra.skillsystem.skills.RedirectBallSkill;
import edu.tigers.sumatra.skillsystem.skills.RotateWithBallSkill;
import edu.tigers.sumatra.skillsystem.skills.RotationSkill;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.dribbling.DragBallSkill;
import edu.tigers.sumatra.skillsystem.skills.dribbling.DribbleKickSkill;
import edu.tigers.sumatra.skillsystem.skills.test.BotLocalKickBallSkill;
import edu.tigers.sumatra.skillsystem.skills.test.IdentDelaysSkill;
import edu.tigers.sumatra.skillsystem.skills.test.KickSampleSkill;
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
	ROTATION(ic(RotationSkill.class)
			.ctorParam(Double.TYPE, "angle", "3.0")
	),
	MOVE_WITH_BALL(ic(MoveWithBallSkill.class)
			.setterParam(IVector2.class, "dest", "0,0", MoveWithBallSkill::setFinalDest)
			.setterParam(Double.class, "orientation", "0", MoveWithBallSkill::setFinalOrientation)
	),
	GET_BALL_CONTACT(ic(GetBallContactSkill.class)),

	ROTATE_WITH_BALL(ic(RotateWithBallSkill.class)
			.setterParam(IVector2.class, "protectionTarget", "0,0", RotateWithBallSkill::setProtectionTarget)
	),

	DRAG_BALL(ic(DragBallSkill.class)
			.setterParam(IVector2.class, "destination", "0,0", DragBallSkill::setDestination)
			.setterParam(Double.class, "targetOrientation", "0.0", DragBallSkill::setTargetOrientation)
	),

	PROTECTIVE_GET_BALL(ic(ProtectiveGetBallSkill.class)
			.setterParam(IVector2.class, "protectionTarget", "0,0", ProtectiveGetBallSkill::setTarget)
			.setterParam(Boolean.TYPE, "strongDribblerContactNeeded", "false",
					ProtectiveGetBallSkill::setStrongDribblerContactNeeded)
	),

	// ************************
	// *** standard skills
	// ************************

	DROP_BALL(ic(DropBallSkill.class)),
	INTERCEPTION(ic(InterceptionSkill.class)),
	MOVE_ON_PENALTY_AREA(ic(MoveOnShapeBoundarySkill.class)
			.setterParam(IVector2.class, "destination", "0,0", MoveOnShapeBoundarySkill::setDestination)
	),
	CRITICAL_KEEPER(ic(CriticalKeeperSkill.class)),
	RAMBO_KEEPER(ic(RamboKeeperSkill.class)),

	// ************************
	// *** utility skills
	// ************************


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

	LOCAL_FORCE_SEQUENCE(ic(LocalForceSequenceSkill.class)
			.ctorParam(Double[].class, "durations", "1; 2")
			.ctorParam(Double[].class, "forces", "8; 0")
			.ctorParam(Double[].class, "directions", "1.57; 0")
			.ctorParam(Double[].class, "torques", "0; 0")
	),

	BOT_LOCAL_KICK(ic(BotLocalKickBallSkill.class)
			.setterParam(IVector2.class, "targetPos", "0,0", BotLocalKickBallSkill::setTargetPos)
			.setterParam(Double.TYPE, "kickSpeed", "0.0", BotLocalKickBallSkill::setKickSpeed)
			.setterParam(Boolean.TYPE, "enableDribbler", "false", BotLocalKickBallSkill::setDribbler)
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
