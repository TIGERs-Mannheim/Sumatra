/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.08.2010
 * Author(s):
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.BlockSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.BlockSkillTrajV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.BotSkillWrapperSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipTestSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.DefenderSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.DelayedKickSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IdleSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.InterceptionSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EKickMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EMoveMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickTestSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToTrajSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PenaltyKeeperSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PenaltyShootSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PenaltyShootSkill.ERotateDirection;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PenaltyShootTrajSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PenaltyShootTrajSkill.ERotateTrajDirection;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PullBackSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ReceiverSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ReceiverSkillTraj;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.RedirectSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.RedirectSkillTraj;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.StraightMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test.CircleSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test.LatencyTestSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test.MoveBallToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test.MoveTestSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test.PositionSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.test.TurnWithBallSkill;
import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableEnum;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableParameter;


/**
 * An enumeration that uniquely identifies each {@link ASkill}-implementation
 * 
 * @author Gero
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum ESkillName implements IInstanceableEnum
{
	/**  */
	POSITION(new InstanceableClass(PositionSkill.class, new InstanceableParameter(IVector2.class, "destination", "0,0"),
			new InstanceableParameter(Float.TYPE, "orientation", "0"))),
			
	/**  */
	KICK(new InstanceableClass(KickSkill.class,
			new InstanceableParameter(DynamicPosition.class, "receiver", "4050,0"),
			new InstanceableParameter(EKickMode.class, "kickMode", "MAX"),
			new InstanceableParameter(EMoveMode.class, "moveMode", "NORMAL"),
			new InstanceableParameter(Integer.TYPE, "duration", "1200"))),
	/**  */
	REDIRECT(new InstanceableClass(RedirectSkill.class, new InstanceableParameter(DynamicPosition.class, "target",
			"3205,0"))),
	/**  */
	REDIRECT_TRAJ(new InstanceableClass(RedirectSkillTraj.class, new InstanceableParameter(DynamicPosition.class,
			"target",
			"3205,0"))),
	/**  */
	DELAYED_KICK(new InstanceableClass(DelayedKickSkill.class, new InstanceableParameter(DynamicPosition.class,
			"target",
			"3205,0"))),
			
	/**  */
	KICK_TEST(new InstanceableClass(KickTestSkill.class,
			new InstanceableParameter(DynamicPosition.class, "target", "3025,0"),
			new InstanceableParameter(EKickMode.class, "kickMode", "FIXED_DURATION"),
			new InstanceableParameter(EMoveMode.class, "moveMode", "CHILL"),
			new InstanceableParameter(Integer.TYPE, "duration", "2000"))),
			
	/**  */
	CHIP_FAST(new InstanceableClass(ChipSkill.class, new InstanceableParameter(DynamicPosition.class, "target",
			"3025,0"))),
			
	/**  */
	CHIP_FAST_TEST(new InstanceableClass(ChipTestSkill.class,
			new InstanceableParameter(DynamicPosition.class, "target", "0,0"),
			new InstanceableParameter(Integer.TYPE, "duration", "5000"),
			new InstanceableParameter(Integer.TYPE, "dribble", "0"))),
			
	/** */
	STRAIGHT_MOVE(new InstanceableClass(StraightMoveSkill.class, new InstanceableParameter(Integer.TYPE, "Distance",
			"1000"), new InstanceableParameter(Float.TYPE, "Angle", "0.0"))),
	/**  */
	TURN_WITH_BALL(new InstanceableClass(TurnWithBallSkill.class, new InstanceableParameter(IVector2.class,
			"lookAtTarget", "0,0"))),
			
	/**  */
	BOT_SKILL_WRAPPER(new InstanceableClass(BotSkillWrapperSkill.class)),
	
	/**  */
	DEFENDER(new InstanceableClass(DefenderSkill.class)),
	/**  */
	BLOCK(new InstanceableClass(BlockSkill.class, new InstanceableParameter(Float.TYPE, "distToGoal", "500"))),
	/**  */
	BLOCKTRAJV2(
			new InstanceableClass(BlockSkillTrajV2.class, new InstanceableParameter(Float.TYPE, "distToGoal", "500"))),
	/** */
	INTERCEPTION(new InstanceableClass(InterceptionSkill.class)),
	/**  */
	RECEIVER(new InstanceableClass(ReceiverSkill.class)),
	/**  */
	RECEIVER_TRAJ(new InstanceableClass(ReceiverSkillTraj.class)),
	
	/**  */
	PENALTY_SHOOT(new InstanceableClass(PenaltyShootSkill.class, new InstanceableParameter(ERotateDirection.class,
			"rotateDirection",
			"LEFT"))),
	/**	*/
	PENALTY_TRAJ_SHOOT(new InstanceableClass(PenaltyShootTrajSkill.class,
			new InstanceableParameter(ERotateTrajDirection.class, "rotateDirection", "LEFT"))),
	/**  */
	PENALTY_KEEPER(new InstanceableClass(PenaltyKeeperSkill.class, new InstanceableParameter(DynamicPosition.class,
			"shooterID",
			"3,YELLOW"))),
	/**  */
	CIRCLE(new InstanceableClass(CircleSkill.class, new InstanceableParameter(Float.TYPE, "duration [s]", "5"),
			new InstanceableParameter(Float.TYPE, "speed [m/s]", "1"))),
			
	// ************************
	// *** utility skills
	// ************************
	
	/**  */
	MOVE_BALL_TO(new InstanceableClass(MoveBallToSkill.class,
			new InstanceableParameter(IVector2.class, "Target", "0, 0"))),
	/**  */
	LATENCY_TEST(new InstanceableClass(LatencyTestSkill.class)),
	/**  */
	MOVE_TEST(new InstanceableClass(MoveTestSkill.class, new InstanceableParameter(MoveTestSkill.EMode.class, "mode",
			"FWD"), new InstanceableParameter(Boolean.TYPE, "loop", "false"))),
			
	// ************************
	// *** normal skills
	// ************************
	/** */
	IDLE(new InstanceableClass(IdleSkill.class)),
	/**  */
	MOVE_TO(new InstanceableClass(MoveToSkill.class)),
	/**  */
	MOVE_TO_TRAJ(new InstanceableClass(MoveToTrajSkill.class)),
	
	// ************************
	// *** to be checked
	// ************************
	
	/**  */
	PULL_BACK(new InstanceableClass(PullBackSkill.class,
			new InstanceableParameter(IVector2.class, "pullTo", "-500,0"),
			new InstanceableParameter(Float.TYPE, "orientation", "0")));
			
	private final InstanceableClass clazz;
	
	
	/**
	 */
	private ESkillName(final InstanceableClass clazz)
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
