/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.10.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense;

import java.awt.Color;
import java.util.List;
import java.util.Map.Entry;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EPossibleGoal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.DrawablePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circlef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.field.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ChipAutoSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndBlockSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndBlockSkill.EBlockModus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveAndStaySkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;


/**
 * <a href="http://www.gockel-09.de/91=92=1a.jpg">
 * Keeper</a> Role for the
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.KeeperSoloPlay}.
 * 
 * 
 * @author PhilippP {ph.posovszky@gmail.com}
 * 
 */
public class KeeperSoloRole extends ADefenseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Goal		goal				= AIConfig.getGeometry().getGoalOur();
	private final float		botRadius		= AIConfig.getGeometry().getBotRadius();
	private final float		goalDistance	= AIConfig.getGeometry().getFieldLength() / 2;
	
	
	/** Ball position */
	private Vector2			protectAgainst;
	
	/** Vector from Keeper to the Ball */
	private Vector2			keeperToProtectAgainst;
	
	/**
	 * defines the circle in which the keeper is allowed to stay
	 * TODO PhilippP: Actually its not a circle!
	 */
	private final int			radius			= AIConfig.getRoles().getKeeperSolo().getRadius();
	
	/** Keeper Start Position: In the Middle of the Goal on the goalline */
	private final Vector2f	goalieStartPos	= AIConfig.getGeometry().getGoalOur().getGoalCenter();
	
	/** Where the keeper has to go finally. */
	private Vector2			destination		= new Vector2(-2500, 0);
	
	// /** Velocity vector of the ball. */
	// private IVector2 ballVel;
	
	/** Intercection where the goalline an the ball vector cut */
	private Vector2			intersectPoint;
	
	private RefereeMsg		latestCmd		= null;
	
	// private final AimingCon aimingCon;
	private TrackedBall		ball;
	
	private enum EStateId
	{
		DEFEND,
		BLOCK,
		MOVE_TO_PENALTYAREA,
		CHIP_KICK,
		MOVE_CHIP_KICK
	}
	
	private enum EEvent
	{
		OUSTIDE_PENALTYAREA,
		INSIDE_PENALTYAREA,
		INTERSECTION,
		INTERSECT_DONE,
		CHIP_KICK_DONE,
		CHIP_KICK_CANCELD,
		CHIP_KICK,
		MOVE_CHIP_KICK
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public KeeperSoloRole()
	{
		super(ERole.KEEPER_SOLO, true, true);
		setInitialState(new NormalBlockState());
		addTransition(EStateId.BLOCK, EEvent.INTERSECT_DONE, new NormalBlockState());
		addTransition(EStateId.DEFEND, EEvent.INTERSECTION, new FastBlockState());
		// If outside Penaltyarea use MoveState to move to penatlyArea
		addTransition(EStateId.BLOCK, EEvent.OUSTIDE_PENALTYAREA, new MoveOutsidePenaltyState());
		addTransition(EStateId.DEFEND, EEvent.OUSTIDE_PENALTYAREA, new MoveOutsidePenaltyState());
		addTransition(EStateId.DEFEND, EEvent.MOVE_CHIP_KICK, new MoveChipKickState());
		addTransition(EStateId.MOVE_CHIP_KICK, EEvent.CHIP_KICK, new ChipKickState());
		addTransition(EStateId.MOVE_CHIP_KICK, EEvent.CHIP_KICK_CANCELD, new NormalBlockState());
		addTransition(EStateId.CHIP_KICK, EEvent.CHIP_KICK_DONE, new NormalBlockState());
		// If reach penalty area, switch to state
		addTransition(EStateId.MOVE_TO_PENALTYAREA, EEvent.INSIDE_PENALTYAREA, new NormalBlockState());
		
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * Move to the PenaltyArea with Playfinder
	 * 
	 * @author PhilippP
	 * 
	 */
	private class MoveOutsidePenaltyState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(new MoveAndStaySkill(getMoveCon()));
		}
		
		
		@Override
		public void doUpdate()
		{
			if (AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(getBot().getPos()))
			{
				nextState(EEvent.INSIDE_PENALTYAREA);
			}
			
			if (checkAllConditions(getAiFrame().worldFrame))
			{
				nextState(EEvent.INSIDE_PENALTYAREA);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			nextState(EEvent.INSIDE_PENALTYAREA);
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.MOVE_TO_PENALTYAREA;
		}
		
	}
	
	// --------------------------------------------------------------------------
	private class NormalBlockState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			MovementCon moveCon = getMoveCon();
			moveCon.setBotsObstacle(true);
			setNewSkill(new MoveAndStaySkill(moveCon));
		}
		
		
		/**
		 * Calculate the final destination
		 * 
		 * @return destination
		 */
		private Vector2 calculateDestination(Vector2 destination)
		{
			ball = getAiFrame().worldFrame.ball;
			protectAgainst = new Vector2(ball.getPos());
			
			// bot has to stand in the bisector ("Winklehalbierende") to cover the goal as much as possible
			destination.y = GeoMath.calculateBisector(protectAgainst, goal.getGoalPostLeft(), goal.getGoalPostRight()).y;
			
			// forces the bot not to drive outside the goal area
			keeperToProtectAgainst = protectAgainst.subtractNew(destination);
			
			keeperToProtectAgainst.multiply(getRadius() / keeperToProtectAgainst.getLength2());
			destination.add(keeperToProtectAgainst);
			
			// "Hinter dem Gehï¿œuse aus Holz von Buchen, hat der Tormann nichts zu suchen!"
			if (destination.x < (-goalDistance + botRadius + 40))
			{
				destination.x = -goalDistance + botRadius + 40;
			}
			
			if ((latestCmd != null) && (latestCmd.getCommand() == Command.STOP))
			{
				final Circlef ballCircle = new Circlef(ball.getPos(), 500);
				if (ballCircle.isPointInShape(destination))
				{
					destination = ballCircle.nearestPointOutside(destination);
				}
			}
			return destination;
		}
		
		
		/**
		 * Calculate the radius for the Keeper
		 * 
		 * @return RADIUS
		 */
		private float getRadius()
		{
			int radiusUsed = 0;
			int defenderCount = countDefender();
			switch (defenderCount)
			{
				case 1:
					radiusUsed = radius * 5;
					break;
				case 2:
					radiusUsed = radius * 4;
					break;
				case 3:
					radiusUsed = radius * 4;
					break;
				default:
					radiusUsed = radius * 5;
					break;
			}
			return radiusUsed;
		}
		
		
		@Override
		public void doUpdate()
		{
			
			ball = getAiFrame().worldFrame.ball;
			protectAgainst = new Vector2(ball.getPos());
			
			if (getAiFrame().refereeMsg != null)
			{
				latestCmd = getAiFrame().refereeMsg;
			}
			try
			{
				intersectPoint = GeoMath.intersectionPoint(protectAgainst, ball.getVel(), goalieStartPos, AVector2.Y_AXIS);
			} catch (final MathException err)
			{
				intersectPoint = null;
			}
			
			// If the ball velocity is zero, the intersection point is null.
			
			if ((intersectPoint != null) && (intersectPoint.y > goal.getGoalPostRight().y())
					&& (intersectPoint.y < goal.getGoalPostLeft().y()) && (getAiFrame().worldFrame.ball.getVel().x() < 0))
			{
				nextState(EEvent.INTERSECTION);
			} else
			{
				destination = calculateDestination(new Vector2(goalieStartPos));
				
				updateDestination(destination);
				updateLookAtTarget(protectAgainst);
				
				if (!AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(getBot().getPos()))
				{
					nextState(EEvent.OUSTIDE_PENALTYAREA);
				}
			}
			if (AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(ball.getPos())
					&& (ball.getVel().equals(Vector2.ZERO_VECTOR, 0.4f)))
			{
				nextState(EEvent.MOVE_CHIP_KICK);
			}
			
		}
		
		
		/**
		 * Counts the Amount of Defender
		 * 
		 * @return
		 */
		private int countDefender()
		{
			int defenderCount = 0;
			for (Entry<BotID, ERole> elements : getAiFrame().getAssigendERoles())
			{
				if (elements.getValue() == ERole.DEFENDER_KNDWDP)
				{
					defenderCount++;
				}
			}
			return defenderCount;
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.DEFEND;
		}
	}
	
	// --------------------------------------------------------------------------
	private class FastBlockState implements IRoleState
	{
		MoveAndBlockSkill	moveAndBlockSkill;
		
		
		@Override
		public void doEntryActions()
		{
			MovementCon moveAndBlock = getMoveCon();
			// moveAndBlock.setForceNewSpline(true);
			moveAndBlockSkill = new MoveAndBlockSkill(moveAndBlock, intersectPoint, EBlockModus.KEEPER_INTERSEC);
			getAiFrame().addDebugShape(new DrawablePoint(intersectPoint, Color.pink));
			setNewSkill(moveAndBlockSkill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if ((moveAndBlockSkill != null) && (moveAndBlockSkill.getDestination() != null))
			{
				getAiFrame().addDebugShape(new DrawablePoint(moveAndBlockSkill.getDestination(), Color.white));
			}
			if ((moveAndBlockSkill != null) && (moveAndBlockSkill.getIntersectPoint() != null))
			{
				getAiFrame().addDebugShape(new DrawablePoint(moveAndBlockSkill.getIntersectPoint(), Color.red));
			}
			if (!AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(getBot().getPos())
					&& (getAiFrame().tacticalInfo.getPossibleGoal() == EPossibleGoal.THEY))
			{
				nextState(EEvent.OUSTIDE_PENALTYAREA);
			}
			try
			{
				intersectPoint = GeoMath.intersectionPoint(getAiFrame().worldFrame.getBall().getPos(), ball.getVel(),
						goalieStartPos, AVector2.Y_AXIS);
				moveAndBlockSkill.setIntersectPoint(intersectPoint);
				getAiFrame().addDebugShape(new DrawablePoint(intersectPoint, Color.blue));
			} catch (final MathException err)
			{
				intersectPoint = null;
			}
			
			// Switch state if intersect is not in goal and keeper is away from ball
			if (((intersectPoint == null) || (intersectPoint.y < goal.getGoalPostRight().y())
					|| (intersectPoint.y > goal.getGoalPostLeft().y()) || (getAiFrame().worldFrame.ball.getVel().x() > 0))
					|| getAiFrame().worldFrame.getBall().getVel().equals(Vector2.ZERO_VECTOR, 0.1f))
			{
				nextState(EEvent.INTERSECT_DONE);
			}
			
			
		}
		
		
		@Override
		public void doExitActions()
		{
			getMoveCon().setForceNewSpline(false);
		}
		
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			
			nextState(EEvent.INTERSECT_DONE);
			
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.BLOCK;
		}
	}
	
	private class MoveChipKickState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			IVector2 pos = getAiFrame().worldFrame.ball.getPos();
			pos = new Vector2(pos.x() - 100, pos.y());
			
			updateDestination(pos);
			updateLookAtTarget(new Vector2(0, 0));
			MovementCon moveCon = getMoveCon();
			moveCon.setBotsObstacle(true);
			setNewSkill(new MoveToSkill(moveCon));
		}
		
		
		@Override
		public void doUpdate()
		{
			if (!AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(getAiFrame().worldFrame.ball.getPos()))
			{
				nextState(EEvent.CHIP_KICK_CANCELD);
			}
			// TODO config rausziehen
			
			if (checkMoveCondition())
			{
				nextState(EEvent.CHIP_KICK);
			} else
			{
				IVector2 pos = getAiFrame().worldFrame.ball.getPos();
				pos = new Vector2(pos.x() - 100, pos.y());
				updateDestination(pos);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.MOVE_CHIP_KICK;
		}
	}
	
	private class ChipKickState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			// TODO in config bringen
			// FIXME MÖGLICHES EIGENTOR IN DIESEM STATE....ich weiß nicht ob es wirklich auftritt, DAnielA meinte er hat
			// sich einmal ins Tor gechipt. Aber ich hab den MoveToSkill schon rausgenommen und ein MoveTo gemacht
			setNewSkill(new ChipAutoSkill(new Vector2(0, 0), 1));
		}
		
		
		@Override
		public void doUpdate()
		{
			if (!isBallInPenaltyArea())
			{
				nextState(EEvent.CHIP_KICK_DONE);
			}
			updateLookAtTarget(getAiFrame().worldFrame.ball.getPos());
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void onSkillStarted(ASkill skill, BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(ASkill skill, BotID botID)
		{
			
			nextState(EEvent.CHIP_KICK_DONE);
			
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.CHIP_KICK;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Checks if the ball is in our penalty Area
	 * 
	 * @return true for in PenaltyArea or false for not
	 */
	private boolean isBallInPenaltyArea()
	{
		IVector2 ballPosition = getAiFrame().worldFrame.ball.getPos();
		return AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(ballPosition);
	}
	
	
	@Override
	public boolean isKeeper()
	{
		return true;
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
	}
	
	
	@Override
	protected void updateMoveCon(AIInfoFrame aiFrame)
	{
		// nothing to do
		
	}
	
}
