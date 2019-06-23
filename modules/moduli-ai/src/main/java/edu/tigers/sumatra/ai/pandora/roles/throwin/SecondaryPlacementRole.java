/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 31, 2014
 * Author(s): MarkG
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.throwin;

import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.IdleSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiverSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiverSkill.EReceiverMode;
import edu.tigers.sumatra.skillsystem.skills.test.PositionSkill;
import edu.tigers.sumatra.statemachine.IRoleState;


/**
 * @author MarkG
 */
public class SecondaryPlacementRole extends APlacementRole
{
	
	@Override
	protected void beforeUpdate()
	{
		if (isBallAtTarget() && (getCurrentState() != EStateId.CLEAR)
				&& ((getWFrame().getBall().getVel().getLength() < 0.2) ||
						(getCurrentState() != EStateId.RECEIVE)))
		{
			triggerEvent(EStateId.CLEAR);
		}
		printText(getCurrentState().name(), 150);
	}
	
	
	/**
	  */
	public SecondaryPlacementRole()
	{
		super(ERole.SECONDARY_AUTOMATED_THROW_IN);
		setInitialState(new PrepareState());
		addTransition(EStateId.RECEIVE, new ReceiveState());
		addTransition(EStateId.PULL_TO_TARGET, new PullState());
		addTransition(EStateId.PREPARE, new PrepareState());
		addTransition(EStateId.CLEAR, new ClearState());
	}
	
	
	private enum EStateId
	{
		PREPARE,
		RECEIVE,
		PULL_TO_TARGET,
		CLEAR;
	}
	
	private class ClearState implements IRoleState
	{
		private AMoveToSkill skill = AMoveToSkill.createMoveToSkill();
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			setNewSkill(skill);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			IVector2 target = getWFrame().getBall().getPos()
					.addNew(getPos().subtractNew(getWFrame().getBall().getPos()).scaleToNew(400));
			if (GeoMath.distancePP(getPos(), getAiFrame().getTacticalField().getThrowInInfo().getPos()) > 400)
			{
				target = getPos();
			}
			skill.getMoveCon().getMoveConstraints().setAccMax(1);
			skill.getMoveCon().getMoveConstraints().setVelMax(1);
			skill.getMoveCon().updateDestination(target);
			if (!isBallAtTarget())
			{
				triggerEvent(EStateId.PREPARE);
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EStateId.CLEAR;
		}
		
	}
	
	private class PrepareState implements IRoleState
	{
		private AMoveSkill skill = AMoveToSkill.createMoveToSkill();
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveToSkill.createMoveToSkill();
			setNewSkill(skill);
			IVector2 kickerToBot = getBot().getBotKickerPos().subtractNew(getPos());
			IVector2 dest = getAiFrame().getTacticalField().getThrowInInfo().getPos().subtractNew(kickerToBot);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().updateLookAtTarget(getWFrame().getBall());
			skill.getMoveCon().updateDestination(dest);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 kickerToBot = getBot().getBotKickerPos().subtractNew(getPos());
			IVector2 dest = getAiFrame().getTacticalField().getThrowInInfo().getPos().subtractNew(kickerToBot);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().updateDestination(dest);
			skill.getMoveCon().updateLookAtTarget(getWFrame().getBall());
			
			if ((GeoMath.distancePP(dest, getPos()) < 50)
					&& getAiFrame().getTacticalField().getThrowInInfo().isReceiverReady())
			{
				triggerEvent(EStateId.RECEIVE);
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EStateId.PREPARE;
		}
	}
	
	private class ReceiveState implements IRoleState
	{
		private ReceiverSkill	skill;
		private boolean			finishSkillSet	= false;
		
		
		@Override
		public void doEntryActions()
		{
			skill = new ReceiverSkill(EReceiverMode.KEEP_DRIBBLING);
			setNewSkill(skill);
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			finishSkillSet = false;
		}
		
		
		@Override
		public void doUpdate()
		{
			skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			IVector2 kickerToBot = getBot().getBotKickerPos().subtractNew(getPos());
			IVector2 dest = getAiFrame().getTacticalField().getThrowInInfo().getPos().subtractNew(kickerToBot);
			if ((getWFrame().getBall().getVel().getLength() < 0.3) &&
					(GeoMath.distancePP(dest, getWFrame().getBall().getPos()) < (OffensiveConstants
							.getAutomatedThrowInFinalTolerance() - 3)))
			{
				setNewSkill(new IdleSkill());
				if (!finishSkillSet)
				{
					// do nothing here
				}
				// lieber wegfahren hier.
			} else if ((getWFrame().getBall().getVel().getLength() < 0.3) &&
					(GeoMath.distancePP(dest, getWFrame().getBall().getPos()) < OffensiveConstants
							.getAutomatedThrowInPushDinstance()))
			{
				// Dribble Ball To Target
				triggerEvent(EStateId.PULL_TO_TARGET);
				return;
			} else
			{
				// skill.setDestination(
				// getWFrame().getBall().getPos().addNew(getPos()
				// .subtractNew(getWFrame().getBall().getPos().multiplyNew(getBot().getCenter2DribblerDist()))));
				skill.setReceiveDestination(dest);
				finishSkillSet = false;
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.RECEIVE;
		}
	}
	
	private class PullState implements IRoleState
	{
		
		private PositionSkill	skill					= null;
		private AMoveSkill		mskill				= null;
		private boolean			moveCloseToBall	= true;
		private IVector2			sBall					= null;
		
		
		@Override
		public void doEntryActions()
		{
			moveCloseToBall = true;
			sBall = getWFrame().getBall().getPos();
			// skill = new PositionSkill(getPos(), getBot().getAngle());
			// setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
			sBall = getWFrame().getBall().getPos();
			IVector2 dest = getAiFrame().getTacticalField().getThrowInInfo().getPos();
			if (GeoMath.distancePP(dest, getWFrame().getBall().getPos()) > OffensiveConstants
					.getAutomatedThrowInPushDinstance())
			{
				triggerEvent(EStateId.PREPARE);
				return;
			} else if (moveCloseToBall)
			{
				if (!getCurrentSkill().toString().toLowerCase().contains("move"))
				{
					// set Move Skill here
					mskill = AMoveToSkill.createMoveToSkill();
					setNewSkill(mskill);
				}
				mskill.getMoveCon().setBallObstacle(true);
				IVector2 ballToDest = dest.subtractNew(getWFrame().getBall().getPos());
				IVector2 botDest = getWFrame().getBall().getPos().addNew(ballToDest.normalizeNew().multiplyNew(-250));
				mskill.getMoveCon().updateDestination(botDest);
				sBall = getWFrame().getBall().getPos();
				mskill.getMoveCon().updateLookAtTarget(dest);
				if ((GeoMath.distancePP(getPos(), botDest) < 50) && (getBot().getVel().getLength() < 0.1))
				{
					moveCloseToBall = false;
				}
			} else
			{
				if (!getCurrentSkill().toString().equals(ESkill.POSITION.toString()))
				{
					// set Position Skill here
					skill = new PositionSkill(getPos(), getBot().getAngle());
					setNewSkill(skill);
				}
				IVector2 ballToDest = dest.subtractNew(getWFrame().getBall().getPos());
				IVector2 botDest = sBall.addNew(ballToDest.normalizeNew().multiplyNew(GeoMath.distancePP(getPos(), dest)));
				skill.setDestination(botDest);
				// skill.setOrientation(
				// sBall.addNew(ballToDest.normalizeNew().multiplyNew(4500)).subtractNew(getPos()).getAngle());
				// skill.setOrientation(sBall.subtractNew(getPos()).getAngle(0));
				if (ballToDest.getLength() > 50)
				{
					// skill.setOrientation(ballToDest.getAngle());
					skill.setOrientation(sBall.subtractNew(getPos()).getAngle(0));
				}
				skill.setDestination(sBall.addNew(ballToDest.scaleToNew(20)));
				skill.getMoveCon().getMoveConstraints().setVelMax(0.25);
				skill.getMoveCon().getMoveConstraints().setAccMax(0.25);
				printText("Pushing ball to Target", 50);
			}
			// if (GeoMath.distancePP(getWFrame().getBall().getPos(),
			// getAiFrame().getTacticalField().getThrowInInfo().getPos()) < OffensiveConstants
			// .getAutomatedThrowInFinalTolerance())
			// {
			// triggerEvent(EStateId.RECEIVE);
			// return;
			// }
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.PULL_TO_TARGET;
		}
	}
	
}
