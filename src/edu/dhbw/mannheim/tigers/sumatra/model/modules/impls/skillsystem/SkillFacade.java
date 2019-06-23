/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.03.2011
 * Author(s): AndreR, Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem;

import edu.dhbw.mannheim.tigers.sumatra.model.data.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.AimingCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.AMoveSkillV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.AimInCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.AimV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.DirectMove;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.DribbleBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.ImmediateDisarm;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.ImmediateStop;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.KickAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.KickBallV1;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.KickBallV1.EKickDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.KickBallV1.EKickMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.MoveAhead;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.MoveDynamicTarget;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.MoveFast;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.MoveFixedGlobalOrientation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.MoveFixedTarget;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.MoveOnPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.SinusSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.TigerStraightMove;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.ballaim.GetBallAndAimSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.ballmove.BallMoveV2;


/**
 * A bag full of skills, which simplifies the use of skill!
 * 
 * @author AndreR, Gero
 * 
 */
public class SkillFacade
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	final ASkill				skills[]				= new ASkill[ESkillGroup.count()];
	private MoveConstraints	moveConstraints	= null;
	
	
	// --------------------------------------------------------------------------
	// --- Move -----------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * If <code>situation == {@link EGameSituation#SET_PIECE}</code> {@link AimInCircle} is used, else
	 * {@link GetBallAndAimSkill}
	 * 
	 * @param aimCon
	 * @param situation
	 */
	public void aiming(AimingCon aimCon, EGameSituation situation)
	{
		aiming(aimCon.getAimingTarget(), aimCon.getAimingTolerance(), situation);
	}
	

	/**
	 * If <code>situation == {@link EGameSituation#SET_PIECE}</code> {@link AimInCircle} is used, else
	 * {@link GetBallAndAimSkill}
	 * 
	 * @param target
	 * @param aimTolerance Radiant!!!
	 * @param situation
	 */
	public void aiming(IVector2 target, float aimTolerance, EGameSituation situation)
	{
		if (situation == EGameSituation.SET_PIECE || situation == EGameSituation.KICK_OFF)
		{
			setSlot(new AimV2(target, situation));
		} else
		{
			setSlot(new GetBallAndAimSkill(target, aimTolerance));
		}
	}
	

	/**
	 * {@link DirectMove#DirectMove(IVector2, float, float, int, boolean)} applys velocities as step, no acceleration
	 * 
	 * @param move
	 * @param v
	 * @param w
	 * @param runtime
	 */
	public void moveDirect(IVector2 move, float v, float w, int runtime)
	{
		setSlot(new DirectMove(move, v, w, runtime, false));
	}
	

	/**
	 * {@link DirectMove#DirectMove(IVector2, float, float, int, boolean)}
	 * 
	 * @param move
	 * @param v
	 * @param w
	 * @param runtime
	 * @param accelerate
	 */
	public void moveDirect(IVector2 move, float v, float w, int runtime, boolean accelerate)
	{
		setSlot(new DirectMove(move, v, w, runtime, accelerate));
	}
	

	/**
	 * {@link MoveOnPath#MoveOnPath(IVector2...)}
	 * 
	 * @param pathpoints
	 * @deprecated
	 */
	public void moveOnPath(IVector2... pathpoints)
	{
		setSlot(new MoveOnPath(pathpoints));
	}
	

	/**
	 * {@link MoveFast#MoveFast(IVector2)}
	 * 
	 * @param dest
	 */
	public void moveFast(IVector2 dest)
	{
		setSlot(new MoveFast(dest));
	}
	

	/**
	 * {@link MoveAhead#MoveAhead(IVector2)}
	 * 
	 * @param dest
	 */
	public void moveTo(IVector2 dest)
	{
		setSlot(new MoveAhead(dest));
		// setSlot(new MoveFixedCurrentOrientation(dest));
	}
	

	/**
	 * {@link MoveFixedTarget}
	 * 
	 * @param dest
	 * @param lookAtTarget
	 */
	public void moveTo(IVector2 dest, IVector2 lookAtTarget)
	{
		setSlot(new MoveFixedTarget(dest, lookAtTarget));
	}
	

	/**
	 * {@link MoveFixedGlobalOrientation}
	 * 
	 * @param dest
	 * @param withTargetOrientation
	 */
	public void moveTo(IVector2 dest, float withTargetOrientation)
	{
		setSlot(new MoveFixedGlobalOrientation(dest, withTargetOrientation));
	}
	

	/**
	 * {@link MoveDynamicTarget}
	 * 
	 * @param dest
	 * @param object
	 */
	public void moveTo(IVector2 dest, ATrackedObject object)
	{
		setSlot(new MoveDynamicTarget(dest, object));
	}
	

	/**
	 * {@link MoveDynamicTarget}
	 * 
	 * @param dest
	 * @param objectId
	 */
	public void moveTo(IVector2 dest, int objectId)
	{
		setSlot(new MoveDynamicTarget(dest, objectId));
	}
	

	/**
	 * {@link BallMoveV2}
	 * 
	 * @param dest
	 */
	public void moveBallTo(IVector2 dest)
	{
		setSlot(new BallMoveV2(dest));
	}
	

	/**
	 * {@link BallMoveV2}
	 * 
	 * @param dest
	 * @param target
	 */
	public void moveBallTo(IVector2 dest, IVector2 target)
	{
		setSlot(new BallMoveV2(dest, target));
	}
	

	/**
	 * {@link MoveFixedGlobalOrientation}
	 * @param targetOrientation
	 */
	public void rotateTo(float targetOrientation)
	{
		setSlot(new MoveFixedGlobalOrientation(null, targetOrientation));
	}
	

	/**
	 * {@link TigerStraightMove}
	 */
	public void moveStraight()
	{
		setSlot(new TigerStraightMove());
	}
	

	/**
	 * {@link TigerStraightMove#TigerStraightMove(int)}
	 * 
	 * @param time
	 */
	public void moveStraight(int time)
	{
		setSlot(new TigerStraightMove(time));
	}
	

	/**
	 * {@link ImmediateStop}
	 */
	public void stop()
	{
		setSlot(new ImmediateStop());
	}
	

	/**
	 * {@link SinusSkill}
	 */
	public void sinus(long runtime)
	{
		setSlot(new SinusSkill(runtime));
	}
	

	// --------------------------------------------------------------------------
	// --- Dribble --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * {@link DribbleBall#DribbleBall(boolean)}
	 * 
	 * @param on
	 */
	public void dribble(boolean on)
	{
		setSlot(new DribbleBall(on));
	}
	

	/**
	 * {@link DribbleBall#DribbleBall(int)}
	 * 
	 * @param rpm
	 */
	public void dribble(int rpm)
	{
		setSlot(new DribbleBall(rpm));
	}
	

	// --------------------------------------------------------------------------
	// --- Kick -----------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Does nothing more then drive forward and arm kicker. Should have dribbler turned off!!!
	 */
	public void kickAuto()
	{
		setSlot(new KickAuto(-1f));
	}
	

	/**
	 * Does nothing more then drive forward and arm kicker with kickLength. Should have dribbler turned off!!!
	 */
	public void kickAuto(float kickLength)
	{
		setSlot(new KickAuto(kickLength));
	}
	

	/**
	 * {@link KickBallV1#KickBallV1(EKickMode, EKickDevice)}
	 * 
	 * @param mode
	 */
	public void kickArm()
	{
		setSlot(new KickBallV1(EKickMode.ARM, EKickDevice.STRAIGHT));
	}
	

	/**
	 * {@link KickBallV1#KickBallV1(float, EKickMode, EKickDevice)}
	 * 
	 * @param kickLength
	 */
	public void kickArm(float kickLength)
	{
		setSlot(new KickBallV1(kickLength, EKickMode.ARM, EKickDevice.STRAIGHT));
	}
	

	/**
	 * {@link KickBallV1#KickBallV1(float, float, EKickMode, EKickDevice)}
	 * 
	 * @param kickLength
	 * @param ballEndVelocity
	 */
	public void kickArm(float kickLength, float ballEndVelocity)
	{
		setSlot(new KickBallV1(kickLength, ballEndVelocity, EKickMode.ARM, EKickDevice.STRAIGHT));
	}
	

	/**
	 * directly kicks with duration microseconds
	 * 
	 * @param duration
	 */
	public void kickArmDirect(float duration)
	{
		setSlot(new KickBallV1(EKickMode.ARM, EKickDevice.STRAIGHT, duration));
	}
	

	/**
	 * {@link KickBallV1#KickBallV1(EKickMode, EKickDevice)}
	 */
	public void kickForce()
	{
		setSlot(new KickBallV1(EKickMode.FORCE, EKickDevice.STRAIGHT));
	}
	

	/**
	 * {@link KickBallV1#KickBallV1(float, EKickMode, EKickDevice)}
	 * 
	 * @param kickLength
	 */
	public void kickForce(float kickLength)
	{
		setSlot(new KickBallV1(kickLength, EKickMode.FORCE, EKickDevice.STRAIGHT));
	}
	

	/**
	 * {@link KickBallV1#KickBallV1(float, float, EKickMode, EKickDevice)}
	 * 
	 * @param kickLength
	 * @param ballEndVelocity
	 */
	public void kickForce(float kickLength, float ballEndVelocity)
	{
		setSlot(new KickBallV1(kickLength, ballEndVelocity, EKickMode.FORCE, EKickDevice.STRAIGHT));
	}
	

	/**
	 * {@link ImmediateDisarm}
	 * <p>
	 * Disarms both chipper and kicker
	 * </p>
	 */
	public void disarm()
	{
		setSlot(new ImmediateDisarm());
	}
	

	// --------------------------------------------------------------------------
	// --- Chip -----------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * {@link KickBallV1#KickBallV1(EKickMode, EKickDevice)}
	 */
	public void chipArm()
	{
		setSlot(new KickBallV1(EKickMode.ARM, EKickDevice.CHIP));
	}
	

	/**
	 * {@link KickBallV1#KickBallV1(float, EKickMode, EKickDevice)}
	 * 
	 * @param kickLength
	 */
	public void chipArm(float kickLength)
	{
		setSlot(new KickBallV1(kickLength, EKickMode.ARM, EKickDevice.CHIP));
	}
	

	/**
	 * {@link KickBallV1#KickBallV1(float, float, EKickMode, EKickDevice)}
	 * 
	 * @param kickLength
	 * @param ballEndVelocity
	 */
	public void chipArm(float kickLength, float ballEndVelocity)
	{
		setSlot(new KickBallV1(kickLength, ballEndVelocity, EKickMode.ARM, EKickDevice.CHIP));
	}
	

	/**
	 * {@link KickBallV1#KickBallV1(EKickMode, EKickDevice)}
	 */
	public void chipForce()
	{
		setSlot(new KickBallV1(EKickMode.FORCE, EKickDevice.CHIP));
	}
	

	/**
	 * {@link KickBallV1#KickBallV1(float, EKickMode, EKickDevice)}
	 * 
	 * @param kickLength
	 */
	public void chipForce(float kickLength, EKickMode mode)
	{
		setSlot(new KickBallV1(kickLength, EKickMode.FORCE, EKickDevice.CHIP));
	}
	

	/**
	 * {@link KickBallV1#KickBallV1(float, float, EKickMode, EKickDevice)}
	 * 
	 * @param kickLength
	 * @param ballEndVelocity
	 */
	public void chipForce(float kickLength, float ballEndVelocity)
	{
		setSlot(new KickBallV1(kickLength, ballEndVelocity, EKickMode.FORCE, EKickDevice.CHIP));
	}
	

	private MoveConstraints getMoveConstraints()
	{
		if (moveConstraints == null)
		{
			moveConstraints = new MoveConstraints();
		}
		return moveConstraints;
	}
	

	/**
	 * Restricts the area the bot moves through. Is applied to all <b>AMoveSkills that already are and will be added this
	 * frame!</b>
	 * 
	 * @param situation
	 */
	public void setMoveConstrains(EGameSituation situation)
	{
		getMoveConstraints().setGameSituation(situation);
		
		ASkill skill = skills[ESkillGroup.MOVE.ordinal()];
		setMoveConstraints(skill, getMoveConstraints());
	}
	

	/**
	 * Restricts the area the bot moves through. Is applied to all <b>AMoveSkills that already are and will be added this
	 * frame!</b>
	 * 
	 * @param ballIsObstacle
	 */
	public void setBallIsObstacle(boolean ballIsObstacle)
	{
		getMoveConstraints().setIsBallObstacle(ballIsObstacle);
		
		ASkill skill = skills[ESkillGroup.MOVE.ordinal()];
		setMoveConstraints(skill, getMoveConstraints());
	}
	

	/**
	 * Restricts the area the bot moves through. Is applied to all <b>AMoveSkills that already are and will be added this
	 * frame!</b>
	 * 
	 * @param isGoalie
	 */
	public void setIsGoalie(boolean isGoalie)
	{
		getMoveConstraints().setIsGoalie(isGoalie);
		
		ASkill skill = skills[ESkillGroup.MOVE.ordinal()];
		setMoveConstraints(skill, getMoveConstraints());
	}
	

	/**
	 * 
	 * @param isMoveFast
	 */
	public void setMoveFast(boolean isMoveFast)
	{
		getMoveConstraints().setFastMove(isMoveFast);
		
		ASkill skill = skills[ESkillGroup.MOVE.ordinal()];
		setMoveConstraints(skill, getMoveConstraints());
	}
	

	// --------------------------------------------------------------------------
	// --- private functions ----------------------------------------------------
	// --------------------------------------------------------------------------
	private void setSlot(final ASkill skill)
	{
		ASkill oldSkill = skills[skill.getGroupID()];
		if (oldSkill == null)
		{
			skills[skill.getGroupID()] = skill;
			
			// Restrict move-area
			setMoveConstraints(skill, moveConstraints);
		} else
		{
			throw new SkillFacadeException("Set slot " + skill.getGroup() + " more than once!!!");
		}
	}
	

	/**
	 * Checks whether the given skill is not <code>null</code> and an instance of {@link AMoveSkill} and then applies it
	 * 
	 * @param skill
	 * @param moveConstraints
	 */
	private void setMoveConstraints(ASkill skill, MoveConstraints moveConstraints)
	{
		if (skill != null && moveConstraints != null && skill instanceof AMoveSkillV2)
		{
			AMoveSkillV2 move = (AMoveSkillV2) skill;
			move.setMoveConstraints(moveConstraints);
		}
	}
	

	// --------------------------------------------------------------------------
	// --- accessors ------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param group The {@link ESkillGroup} whose slot should be checked
	 * @return Whether the slot for the given {@link ESkillGroup} is set
	 */
	public boolean isSlotFree(ESkillGroup group)
	{
		return skills[group.ordinal()] == null;
	}
	
	
	/**
	 * Occurs when an error has been detected while filling a {@link SkillFacade}
	 * 
	 * @author Gero
	 */
	public static class SkillFacadeException extends RuntimeException
	{
		private static final long	serialVersionUID	= 4614341489718837566L;
		
		
		/**
		 * @param msg
		 */
		public SkillFacadeException(String msg)
		{
			super(msg);
		}
	}
}
