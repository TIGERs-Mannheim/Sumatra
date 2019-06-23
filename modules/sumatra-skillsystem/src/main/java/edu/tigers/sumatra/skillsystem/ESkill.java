/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;

import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AKickSkill.EKickMode;
import edu.tigers.sumatra.skillsystem.skills.ASkill;
import edu.tigers.sumatra.skillsystem.skills.BallInterceptorTestSkill;
import edu.tigers.sumatra.skillsystem.skills.BallPlacementSkill;
import edu.tigers.sumatra.skillsystem.skills.BlockSkill;
import edu.tigers.sumatra.skillsystem.skills.BotSkillWrapperSkill;
import edu.tigers.sumatra.skillsystem.skills.CatchSkill;
import edu.tigers.sumatra.skillsystem.skills.IdentDelaysSkill;
import edu.tigers.sumatra.skillsystem.skills.IdentMotorSkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.InterceptionSkill;
import edu.tigers.sumatra.skillsystem.skills.KickChillSkill;
import edu.tigers.sumatra.skillsystem.skills.KickNormalSkill;
import edu.tigers.sumatra.skillsystem.skills.KickSampleSkill;
import edu.tigers.sumatra.skillsystem.skills.LatencyTestSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveBangBangSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveOnPenaltyAreaSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToTrajSkill;
import edu.tigers.sumatra.skillsystem.skills.PenaltyKeeperSkill;
import edu.tigers.sumatra.skillsystem.skills.PenaltyShootSkill;
import edu.tigers.sumatra.skillsystem.skills.PositionSkill;
import edu.tigers.sumatra.skillsystem.skills.ProtectBallSkill;
import edu.tigers.sumatra.skillsystem.skills.PullBackSkillV2;
import edu.tigers.sumatra.skillsystem.skills.PushAroundObstacleSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiverSkill;
import edu.tigers.sumatra.skillsystem.skills.RedirectSkill;
import edu.tigers.sumatra.skillsystem.skills.RotationSkill;
import edu.tigers.sumatra.skillsystem.skills.RunUpChipSkill;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * An enumeration that uniquely identifies each {@link ASkill}-implementation
 * 
 * @author Gero
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@SuppressWarnings("squid:S1192")
public enum ESkill implements IInstanceableEnum
{
	/** */
	KICK_CHILL(new InstanceableClass(KickChillSkill.class,
			new InstanceableParameter(DynamicPosition.class, "receiver", "4050,0"),
			new InstanceableParameter(EKickMode.class, "kickMode", "MAX"),
			new InstanceableParameter(EKickerDevice.class, "device", "STRAIGHT"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "6"))),
	/** */
	KICK_NORMAL(new InstanceableClass(KickNormalSkill.class,
			new InstanceableParameter(DynamicPosition.class, "receiver", "4050,0"),
			new InstanceableParameter(EKickMode.class, "kickMode", "MAX"),
			new InstanceableParameter(EKickerDevice.class, "device", "STRAIGHT"),
			new InstanceableParameter(Double.TYPE, "kickSpeed", "6"))),
	/** */
	RUN_UP_CHIP(new InstanceableClass(RunUpChipSkill.class,
			new InstanceableParameter(DynamicPosition.class, "receiver", "4050,0"),
			new InstanceableParameter(EKickMode.class, "kickMode", "MAX"))),
	/** */
	PROTECT_BALL(new InstanceableClass(ProtectBallSkill.class,
			new InstanceableParameter(DynamicPosition.class, "protectionTarget", "0,0"))),
	/** */
	PUSH_AROUND_OBSTACLE(new InstanceableClass(PushAroundObstacleSkill.class,
			new InstanceableParameter(DynamicPosition.class, "obstacle", "0 Y"),
			new InstanceableParameter(DynamicPosition.class, "target", "0,0"))),
	/**  */
	REDIRECT(new InstanceableClass(RedirectSkill.class,
			new InstanceableParameter(DynamicPosition.class, "target", "3205,0"),
			new InstanceableParameter(Double.TYPE, "passEndVel", "3.5"))),
	/**  */
	RECEIVER(new InstanceableClass(ReceiverSkill.class)),
	/** */
	CATCH(new InstanceableClass(CatchSkill.class)),
	/**  */
	BLOCK(new InstanceableClass(BlockSkill.class,
			new InstanceableParameter(Double.TYPE, "distToGoal", "500"))),
	/** */
	INTERCEPTION(new InstanceableClass(InterceptionSkill.class)),
	/**  */
	PENALTY_SHOOT(new InstanceableClass(PenaltyShootSkill.class,
			new InstanceableParameter(PenaltyShootSkill.ERotateDirection.class,
					"shootDirection", "CW"))),
	/**  */
	PENALTY_KEEPER(new InstanceableClass(PenaltyKeeperSkill.class, new InstanceableParameter(DynamicPosition.class,
			"shooterID",
			"3,YELLOW"))),
	
	MOVE_ON_PENALTY_AREA(new InstanceableClass(MoveOnPenaltyAreaSkill.class,
			new InstanceableParameter(DynamicPosition.class, "target", "0,0"))),

	// ************************
	// *** utility skills
	// ************************
	
	/**  */
	POSITION(new InstanceableClass(PositionSkill.class,
			new InstanceableParameter(IVector2.class, "dest", "0,0"),
			new InstanceableParameter(Double.TYPE, "targetAngle", "0"))),
	/**  */
	BOT_SKILL_WRAPPER(new InstanceableClass(BotSkillWrapperSkill.class)),
	/**  */
	LATENCY_TEST(new InstanceableClass(LatencyTestSkill.class)),
	/** */
	IDENT_MOTOR(new InstanceableClass(IdentMotorSkill.class,
			new InstanceableParameter(Double.TYPE, "maxSpeedW", "25.1"),
			new InstanceableParameter(Integer.TYPE, "numSteps", "10"))),
	/** */
	IDENT_DELAYS(new InstanceableClass(IdentDelaysSkill.class,
			new InstanceableParameter(Double.TYPE, "amplitude", "0.5"),
			new InstanceableParameter(Double.TYPE, "frequency", "2.0"),
			new InstanceableParameter(Double.TYPE, "runtime", "10"))),
	/**  */
	MOVE_BANG_BANG(new InstanceableClass(MoveBangBangSkill.class)),
	/** */
	KICK_SAMPLE(new InstanceableClass(KickSampleSkill.class,
			new InstanceableParameter(IVector2.class, "kickPos", "0,0"),
			new InstanceableParameter(Double.TYPE, "targetAngle", "0"),
			new InstanceableParameter(EKickerDevice.class, "device", "STRAIGHT"),
			new InstanceableParameter(Double.TYPE, "durationMs", "10.0"))),
	
	// ************************
	// *** normal skills
	// ************************
	
	/** */
	IDLE(new InstanceableClass(IdleSkill.class)),
	/**  */
	MOVE_TO_TRAJ(new InstanceableClass(MoveToTrajSkill.class)),
	
	
	// ************************
	// *** to be checked
	// ************************
	
	/**  */
	PULL_BACKV2(new InstanceableClass(PullBackSkillV2.class,
			new InstanceableParameter(IVector2.class, "target", "0,0"))),
	/** */
	BALL_PLACEMENT(new InstanceableClass(BallPlacementSkill.class,
			new InstanceableParameter(IVector2.class, "target", "0,0"))),
	/** */
	ROTATION(new InstanceableClass(RotationSkill.class,
			new InstanceableParameter(Double.TYPE, "angle", "3.0"))),

	BALL_INTERCEPTOR_TEST(new InstanceableClass(BallInterceptorTestSkill.class));
	
	
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
