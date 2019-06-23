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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.EKickDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.BlockSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipFastSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipFastTestSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipTestSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.CurveTestSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.EightSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.GetBallSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ImmediateStopSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KeeperPositionSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EKickMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickTestSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.LookAtBallTestSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveBallToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToV2Skill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.NormalStopSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PenaltyShootSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PenaltyShootSkill.ERotateDirection;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PositionSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.PullBackSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.RedirectSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.RotateTestSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ShooterBotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.StraightMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.TurnAroundBallPosSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.TurnAroundBallSplineSkill;
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
	KICK(new InstanceableClass(KickSkill.class, new InstanceableParameter(DynamicPosition.class, "receiver", "3025,0"),
			new InstanceableParameter(EKickMode.class, "kickMode", "POINT"))),
	/**  */
	KICK_TEST(new InstanceableClass(KickTestSkill.class, new InstanceableParameter(IVector2.class, "target", "3025,0"),
			new InstanceableParameter(Integer.TYPE, "duration", "10000"))),
	
	/**  */
	CHIP_FAST(new InstanceableClass(ChipFastSkill.class, new InstanceableParameter(DynamicPosition.class, "target",
			"3025,0"))),
	
	/**  */
	CHIP_FAST_TEST(new InstanceableClass(ChipFastTestSkill.class, new InstanceableParameter(Integer.TYPE, "duration",
			"5000"))),
	
	/**  */
	CHIP_DURATION(new InstanceableClass(ChipTestSkill.class,
			new InstanceableParameter(Integer.TYPE, "duration", "5000"), new InstanceableParameter(Integer.TYPE,
					"dribble", "5000"))),
	
	/**  */
	CHIP_KICK_TARGET(new InstanceableClass(ChipSkill.class, new InstanceableParameter(IVector2.class, "target", "0, 0"),
			new InstanceableParameter(Float.TYPE, "endVel", "0"))),
	
	/**  */
	CHIP_KICK_LENGTH(new InstanceableClass(ChipSkill.class, new InstanceableParameter(Float.TYPE, "length", "0, 0"),
			new InstanceableParameter(Float.TYPE, "endVel", "0"))),
	/** */
	STRAIGHT_MOVE(new InstanceableClass(StraightMoveSkill.class, new InstanceableParameter(Integer.TYPE, "Distance",
			"1000"), new InstanceableParameter(Float.TYPE, "Angle", "0.0"))),
	/**  */
	TURN_AROUND_BALL_SPLINE(new InstanceableClass(TurnAroundBallSplineSkill.class, new InstanceableParameter(
			IVector2.class, "lookAtTarget", "0, 0"))),
	/**  */
	TURN_AROUND_BALL_SPLINE_ANGLE(new InstanceableClass(TurnAroundBallSplineSkill.class, new InstanceableParameter(
			Float.TYPE, "angle", "1.5708"))),
	/**  */
	TURN_AROUND_BALL_POS(new InstanceableClass(TurnAroundBallPosSkill.class, new InstanceableParameter(Float.TYPE,
			"radius", "50"))),
	
	/**  */
	TURN(new InstanceableClass(CurveTestSkill.class, new InstanceableParameter(Float.TYPE, "size", "100"),
			new InstanceableParameter(Float.TYPE, "angle", "3.14"))),
	/**  */
	ROTATE(new InstanceableClass(RotateTestSkill.class, new InstanceableParameter(Float.TYPE, "angle", "1.5708"))),
	/**  */
	EIGHT(new InstanceableClass(EightSkill.class, new InstanceableParameter(Float.TYPE, "size", "1500"))),
	/**  */
	POSITION_KEEPER(new InstanceableClass(KeeperPositionSkill.class)),
	/**  */
	BOT_SHOOTER(new InstanceableClass(ShooterBotSkill.class, new InstanceableParameter(IVector2.class, "shootTarget",
			"3050,0"), new InstanceableParameter(EKickDevice.class, "device", EKickDevice.STRAIGHT.name()),
			new InstanceableParameter(Integer.TYPE, "duration", "5000"))),
	/**  */
	LOOK_AT_BALL(new InstanceableClass(LookAtBallTestSkill.class)),
	
	/**  */
	REDIRECT(new InstanceableClass(RedirectSkill.class, new InstanceableParameter(DynamicPosition.class, "target",
			"3205,0"))),
	/**  */
	BLOCK(new InstanceableClass(BlockSkill.class)),
	
	// ************************
	// *** utility skills
	// ************************
	
	/**  */
	MOVE_BALL_TO(new InstanceableClass(MoveBallToSkill.class,
			new InstanceableParameter(IVector2.class, "Target", "0, 0"))),
	/** used by moveBallTo */
	GET_BALL(new InstanceableClass(GetBallSkill.class, new InstanceableParameter(Boolean.TYPE, "dribble", "false"))),
	/**  */
	POSITION(new InstanceableClass(PositionSkill.class, new InstanceableParameter(IVector2.class, "destination", "0,0"),
			new InstanceableParameter(Float.TYPE, "orientation", "0"))),
	
	
	// ************************
	// *** normal skills
	// ************************
	/** */
	IMMEDIATE_STOP(new InstanceableClass(ImmediateStopSkill.class)),
	/**  */
	NORMAL_STOP(new InstanceableClass(NormalStopSkill.class)),
	/**  */
	MOVE_TO(new InstanceableClass(MoveToSkill.class)),
	/**  */
	MOVE_AND_STAY(new InstanceableClass(MoveAndStaySkill.class)),
	/**  */
	MOVE_TO_V2(new InstanceableClass(MoveToV2Skill.class)),
	
	// ************************
	// *** to be checked
	// ************************
	/**  */
	PULL_BACK(new InstanceableClass(PullBackSkill.class)),
	/**  */
	PENALTY_SHOOT(new InstanceableClass(PenaltyShootSkill.class, new InstanceableParameter(ERotateDirection.class,
			"rotateDirection",
			"LEFT"))), ;
	
	private final InstanceableClass	clazz;
	
	
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
