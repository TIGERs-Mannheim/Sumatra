/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.11.2012
 * Author(s): Philipp
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.BangBangTrajectory2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.TrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.DefenseCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * DefenseRole for {@link DefensePoint}. Positioned a bot at a specific position and look at a target (normally ball
 * position)
 * 
 * @author PhilippP
 */
public class DefenderRole extends ARole
{
	// --- analyzing specifications ---
	/** Point to protect against **/
	private DefensePoint												defPoint;
	
	@Configurable
	private static float												forcePathTime					= 0.05f;
	
	@Configurable
	private static float												noPPAreaAroundPenaltyArea	= 2000;
	
	@Configurable(comment = "Radius the bot uses to catch incoming balls")
	private static float												catchRadius						= 500f;
	
	@Configurable(comment = "Lookahead of the defender to catch an incoming ball")
	private static float												catchLookahead					= 1f;
	
	/**  */
	public static final Comparator<? super DefenderRole>	Y_COMPARATOR					= new YComparator();
	
	
	/**
	 */
	public DefenderRole()
	{
		super(ERole.DEFENDER);
		IRoleState normalDefend = new NormalDefendState();
		setInitialState(normalDefend);
		IRoleState moveState = new MoveFromField();
		addTransition(EStateId.OUTSIDE, EEvent.NEAR_DEF_POINT, new NormalDefendState());
		addTransition(EStateId.NORMAL, EEvent.FAR_FROM_DEF_POINT, moveState);
		addTransition(EStateId.NORMAL, EEvent.INCOMINGBALL, new CatchBallState());
		addTransition(EStateId.INTERRUPTINGBALL, EEvent.INTERCEPTIONHOPELESS, new NormalDefendState());
	}
	
	private enum EStateId
	{
		NORMAL,
		OUTSIDE,
		INTERRUPTINGBALL,
	}
	
	private enum EEvent
	{
		DONE,
		NEAR_DEF_POINT,
		FAR_FROM_DEF_POINT,
		INCOMINGBALL,
		INTERCEPTIONHOPELESS,
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Set the Defender Position
	 * 
	 * @param defPoint - Position to Defend
	 */
	public void setDefPoint(final DefensePoint defPoint)
	{
		assert defPoint != null : "Defpoint should not be null!";
		if (defPoint != null)
		{
			this.defPoint = defPoint;
		}
	}
	
	
	/**
	 * Returns the acutal defensPoint
	 * 
	 * @return DefensPoints
	 */
	public IVector2 getDefPoint()
	{
		
		return defPoint;
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		features.add(EFeature.MOVE);
	}
	
	
	// --------------------------------------------------------------------------
	// --- InnerClasses --------------------------------------------------------
	// --------------------------------------------------------------------------
	private static class YComparator implements Comparator<DefenderRole>, Serializable
	{
		
		/**  */
		private static final long	serialVersionUID	= 1794858044291002364L;
		
		
		@Override
		public int compare(final DefenderRole v1, final DefenderRole v2)
		{
			if (v1.getPos().y() > v2.getPos().y())
			{
				return 1;
			} else if (v1.getPos().y() < v2.getPos().y())
			{
				return -1;
			} else
			{
				return 0;
			}
		}
	}
	
	private class MoveFromField implements IRoleState
	{
		private IMoveToSkill	skill;
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void doEntryActions()
		{
			skill = AMoveSkill.createMoveToSkill();
			skill.getMoveCon().setBotsObstacle(false);
			skill.getMoveCon().setBallObstacle(false);
			skill.getMoveCon().setDriveFast(true);
			skill.getMoveCon().setForcePathAfterTime(forcePathTime);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			if (null != defPoint)
			{
				skill.getMoveCon().updateDestination(defPoint);
				skill.getMoveCon().setForcePathAfterTime(forcePathTime);
				if (AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(getPos(), DefenseCalc.getPenaltyAreaMargin() +
						noPPAreaAroundPenaltyArea))
				{
					triggerEvent(EEvent.NEAR_DEF_POINT);
				}
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.OUTSIDE;
		}
		
	}
	
	private class NormalDefendState implements IRoleState
	{
		private IMoveToSkill	defSkill	= null;
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void doEntryActions()
		{
			defSkill = AMoveSkill.createMoveToSkill();
			defSkill.getMoveCon().setBotsObstacle(false);
			defSkill.getMoveCon().setBallObstacle(false);
			defSkill.getMoveCon().setDriveFast(true);
			defSkill.getMoveCon().setForcePathAfterTime(forcePathTime);
			setNewSkill(defSkill);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			if (!AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(getPos(), DefenseCalc.getPenaltyAreaMargin() +
					noPPAreaAroundPenaltyArea))
			{
				triggerEvent(EEvent.FAR_FROM_DEF_POINT);
			}
			
			TrackedBall ball = getAiFrame().getWorldFrame().getBall();
			Circle nearBot = new Circle(getPos(), catchRadius);
			
			if ((nearBot.isLineSegmentIntersectingShape(ball.getPos(), ball.getPosByTime(catchLookahead)) ||
					nearBot.isPointInShape(ball.getPos())) &&
					!AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(ball.getPos()) &&
					!AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(ball.getPosByTime(catchLookahead)) &&
					(EGameState.RUNNING == getAiFrame().getTacticalField().getGameState()))
			{
				triggerEvent(EEvent.INCOMINGBALL);
			}
			
			if (defPoint != null)
			{
				EGameState curGameState = getAiFrame().getTacticalField().getGameState();
				if ((EGameState.STOPPED == curGameState) ||
						(EGameState.HALTED == curGameState))
				{
					defSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
				}
				else
				{
					defSkill.getMoveCon().setPenaltyAreaAllowedOur(false);
				}
				defSkill.getMoveCon().updateLookAtTarget(ball);
				defSkill.getMoveCon().updateDestination(defPoint);
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.NORMAL;
		}
	}
	
	private class CatchBallState implements IRoleState
	{
		
		private IMoveToSkill	defSkill	= null;
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void doEntryActions()
		{
			defSkill = AMoveSkill.createMoveToSkill();
			defSkill.getMoveCon().setBotsObstacle(false);
			defSkill.getMoveCon().setBallObstacle(false);
			defSkill.getMoveCon().setDriveFast(true);
			defSkill.getMoveCon().setForcePathAfterTime(forcePathTime);
			setNewSkill(defSkill);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			if (EGameState.RUNNING != getAiFrame().getTacticalField().getGameState())
			{
				triggerEvent(EEvent.INTERCEPTIONHOPELESS);
			}
			
			TrackedBall ball = getAiFrame().getWorldFrame().getBall();
			Circle nearBot = new Circle(getPos(), catchRadius);
			Line ballPath = Line.newLine(ball.getPos(), ball.getPosByTime(catchLookahead));
			
			if ((nearBot.isLineSegmentIntersectingShape(ball.getPos(), ball.getPosByTime(catchLookahead)) ||
					nearBot.isPointInShape(ball.getPos())) &&
					!AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(ball.getPos()) &&
					!AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(ball.getPosByTime(catchLookahead)))
			{
				IVector2 catchPoint;
				if (0 == ball.getVelByPos(ball.getPos()))
				{
					catchPoint = ball.getPos().addNew(
							AIConfig.getGeometry().getGoalOur().getGoalCenter().subtractNew(ball.getPos())
									.scaleToNew(AIConfig.getGeometry().getBotRadius() * 1.5f));
					catchPoint = AIConfig.getGeometry().getPenaltyAreaOur()
							.nearestPointOutside(catchPoint, Geometry.getPenaltyAreaMargin());
					defSkill.getMoveCon().updateLookAtTarget(ball);
					defSkill.getMoveCon().updateDestination(catchPoint);
				} else
				{
					catchPoint = GeoMath.leadPointOnLine(getPos(), ballPath);
					
					BangBangTrajectory2D pathToIntercept = TrajectoryGenerator.generatePositionTrajectory(getBot(),
							catchPoint);
					if (pathToIntercept.getTotalTime() < ball.getTimeByPos(catchPoint))
					{
						defSkill.getMoveCon().updateLookAtTarget(ball);
						defSkill.getMoveCon().updateDestination(catchPoint);
					} else
					{
						triggerEvent(EEvent.INTERCEPTIONHOPELESS);
					}
				}
			} else
			{
				triggerEvent(EEvent.INTERCEPTIONHOPELESS);
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EStateId.INTERRUPTINGBALL;
		}
		
	}
	
}
