/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;

import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.ASkill;
import edu.tigers.sumatra.skillsystem.skills.ApproachAndStopBallSkill;
import edu.tigers.sumatra.skillsystem.skills.ApproachBallLineSkill;
import edu.tigers.sumatra.skillsystem.skills.BotSkillWrapperSkill;
import edu.tigers.sumatra.skillsystem.skills.CatchBallSkill;
import edu.tigers.sumatra.skillsystem.skills.CommandListSkill;
import edu.tigers.sumatra.skillsystem.skills.CriticalKeeperSkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.InterceptionSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveOnPenaltyAreaSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToTrajSkill;
import edu.tigers.sumatra.skillsystem.skills.PenaltyKeeperSkill;
import edu.tigers.sumatra.skillsystem.skills.PenaltyShootSkill;
import edu.tigers.sumatra.skillsystem.skills.ProtectBallSkill;
import edu.tigers.sumatra.skillsystem.skills.PullBallSkill;
import edu.tigers.sumatra.skillsystem.skills.PushAroundObstacleSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiveBallSkill;
import edu.tigers.sumatra.skillsystem.skills.RedirectBallSkill;
import edu.tigers.sumatra.skillsystem.skills.RotationSkill;
import edu.tigers.sumatra.skillsystem.skills.RunUpChipSkill;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.test.IdentDelaysSkill;
import edu.tigers.sumatra.skillsystem.skills.test.IdentMotorSkill;
import edu.tigers.sumatra.skillsystem.skills.test.KickSampleSkill;
import edu.tigers.sumatra.skillsystem.skills.test.LatencyTestSkill;
import edu.tigers.sumatra.skillsystem.skills.test.MoveBangBangSkill;
import edu.tigers.sumatra.skillsystem.skills.test.PositionSkill;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * An enumeration that uniquely identifies each {@link ASkill}-implementation
 */
@SuppressWarnings("squid:S1192")
public enum ESkill implements IInstanceableEnum
{
	// ************************
	// *** match skills
	// ************************
	TOUCH_KICK(new InstanceableClass(TouchKickSkill.class,
			new InstanceableParameter(DynamicPosition.class, "target", "4050,0"),
			new InstanceableParameter(EKickerDevice.class, "device", "STRAIGHT"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "0"))),
	
	SINGLE_TOUCH_KICK(new InstanceableClass(SingleTouchKickSkill.class,
			new InstanceableParameter(DynamicPosition.class, "target", "4050,0"),
			new InstanceableParameter(EKickerDevice.class, "device", "STRAIGHT"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "0"))),
	
	RUN_UP_CHIP(new InstanceableClass(RunUpChipSkill.class,
			new InstanceableParameter(DynamicPosition.class, "target", "4050,0"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "6.5"))),
	
	RECEIVE_BALL(new InstanceableClass(ReceiveBallSkill.class,
			new InstanceableParameter(IVector2.class, "receivingPosition", "0,0"))),
	
	REDIRECT_BALL(new InstanceableClass(RedirectBallSkill.class,
			new InstanceableParameter(IVector2.class, "receivingPosition", "0,0"),
			new InstanceableParameter(DynamicPosition.class, "target", "6000,0"),
			new InstanceableParameter(EKickerDevice.class, "device", "STRAIGHT"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "6.5"))),
	
	APPROACH_BALL_LINE(new InstanceableClass(ApproachBallLineSkill.class)),
	
	APPROACH_AND_STOP_BALL(new InstanceableClass(ApproachAndStopBallSkill.class)),
	
	PROTECT_BALL(new InstanceableClass(ProtectBallSkill.class,
			new InstanceableParameter(DynamicPosition.class, "protectionTarget", "0,0"))),
	
	PUSH_AROUND_OBSTACLE(new InstanceableClass(PushAroundObstacleSkill.class,
			new InstanceableParameter(DynamicPosition.class, "obstacle", "0 Y"),
			new InstanceableParameter(DynamicPosition.class, "target", "0,0"))),
	
	PULL_BALL(new InstanceableClass(PullBallSkill.class,
			new InstanceableParameter(IVector2.class, "target", "0,0"))),
	
	ROTATION(new InstanceableClass(RotationSkill.class,
			new InstanceableParameter(Double.TYPE, "angle", "3.0"))),
	
	CATCH(new InstanceableClass(CatchBallSkill.class)),
	
	INTERCEPTION(new InstanceableClass(InterceptionSkill.class)),
	
	PENALTY_SHOOT(new InstanceableClass(PenaltyShootSkill.class,
			new InstanceableParameter(PenaltyShootSkill.ERotateDirection.class,
					"shootDirection", "CW"))),
	
	PENALTY_KEEPER(new InstanceableClass(PenaltyKeeperSkill.class, new InstanceableParameter(DynamicPosition.class,
			"shooterID",
			"3,YELLOW"))),
	
	MOVE_ON_PENALTY_AREA(new InstanceableClass(MoveOnPenaltyAreaSkill.class,
			new InstanceableParameter(DynamicPosition.class, "target", "0,0"))),
	
	COMMAND_LIST(new InstanceableClass(CommandListSkill.class,
			new InstanceableParameter(String.class, "commandSequence", "0 vy 0.5|0 kd c|1.0 k 5|1.1 noop"))),
	
	// The Keeper skill
	
	CRITICAL_KEEPER(new InstanceableClass(CriticalKeeperSkill.class)),
	
	// ************************
	// *** utility skills
	// ************************
	
	POSITION(new InstanceableClass(PositionSkill.class,
			new InstanceableParameter(IVector2.class, "dest", "0,0"),
			new InstanceableParameter(Double.TYPE, "targetAngle", "0"))),
	
	BOT_SKILL_WRAPPER(new InstanceableClass(BotSkillWrapperSkill.class)),
	
	LATENCY_TEST(new InstanceableClass(LatencyTestSkill.class)),
	
	IDENT_MOTOR(new InstanceableClass(IdentMotorSkill.class,
			new InstanceableParameter(Double.TYPE, "maxSpeedW", "25.1"),
			new InstanceableParameter(Integer.TYPE, "numSteps", "10"))),
	
	IDENT_DELAYS(new InstanceableClass(IdentDelaysSkill.class,
			new InstanceableParameter(Double.TYPE, "amplitude", "0.5"),
			new InstanceableParameter(Double.TYPE, "frequency", "2.0"),
			new InstanceableParameter(Double.TYPE, "runtime", "10"))),
	
	MOVE_BANG_BANG(new InstanceableClass(MoveBangBangSkill.class,
			new InstanceableParameter(IVector2.class, "destination", "0,0"),
			new InstanceableParameter(Double.TYPE, "orientation", "0"),
			new InstanceableParameter(EBotSkill.class, "botSkill", "LOCAL_VELOCITY"),
			new InstanceableParameter(Boolean.TYPE, "rollOut", "false"))),
	
	KICK_SAMPLE(new InstanceableClass(KickSampleSkill.class,
			new InstanceableParameter(IVector2.class, "kickPos", "0,0"),
			new InstanceableParameter(Double.TYPE, "targetAngle", "0"),
			new InstanceableParameter(EKickerDevice.class, "device", "STRAIGHT"),
			new InstanceableParameter(Double.TYPE, "durationMs", "10.0"))),
	
	// ************************
	// *** generic skills
	// ************************
	
	IDLE(new InstanceableClass(IdleSkill.class)),
	
	MOVE_TO_TRAJ(new InstanceableClass(MoveToTrajSkill.class)),
	
	;
	
	
	private final InstanceableClass clazz;
	
	
	/**
	 */
	ESkill(final InstanceableClass clazz)
	{
		this.clazz = clazz;
	}
	
	
	/**
	 * @return the paramImpls
	 */
	@Override
	public final InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
}
