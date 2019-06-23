/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.11.2012
 * Author(s): Philipp
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.defense;

import java.awt.Color;
import java.io.Serializable;
import java.util.Comparator;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.BotDistance;
import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.ITacticalField;
import edu.tigers.sumatra.ai.metis.defense.KeeperStateCalc;
import edu.tigers.sumatra.ai.metis.defense.ZoneDefenseCalc;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseAux;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePoint;
import edu.tigers.sumatra.ai.pandora.plays.defense.DefensePlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.support.LegalPointChecker;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.CatchSkill;
import edu.tigers.sumatra.skillsystem.skills.KickSkill;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EKickMode;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EMoveMode;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * DefenseRole for {@link DefensePoint}. Positioned a bot at a specific position and look at a target
 * (normally ball position)
 * 
 * @author PhilippP
 */
public class DefenderRole extends ARole
{
	@SuppressWarnings("unused")
	private static final Logger	log						= Logger.getLogger(DefenderRole.class.getName());
	
	@Configurable(comment = "Radius where Defender tries to intercept ball if directed to goal")
	private static double			interceptRadius		= 500;
	
	// --- analyzing specifications ---
	/** Point to protect against **/
	private DefensePoint				defPoint;
	
	
	@Configurable
	private static double			noPP_dist2defPoint	= 1000;
	
	private EGameStateTeam			gameState				= EGameStateTeam.UNKNOWN;
	
	
	/**
	 * @return the gameState
	 */
	public EGameStateTeam getGameState()
	{
		return gameState;
	}
	
	
	/**
	 * @param gameState the gameState to set
	 */
	public void setGameState(final EGameStateTeam gameState)
	{
		this.gameState = gameState;
	}
	
	/**  */
	public static final Comparator<? super DefenderRole> Y_COMPARATOR = new YComparator();
	
	
	/**
	 */
	public DefenderRole()
	{
		super(ERole.DEFENDER);
		
		IRoleState normalDefend = new NormalDefendState();
		IRoleState clearBall = new ClearBallDefenseState(this);
		IRoleState intercept = new InterceptState();
		
		addTransition(normalDefend, EDefenderEvent.NEED_TO_CLEAR, clearBall);
		addTransition(clearBall, EDefenderEvent.CALM_DOWN, normalDefend);
		addTransition(intercept, EDefenderEvent.CALM_DOWN, normalDefend);
		
		addTransition(EDefenderEvent.INTERCEPT, intercept);
		
		setInitialState(normalDefend);
	}
	
	
	@Override
	protected void beforeUpdate()
	{
		try
		{
			TrackedBall ball = getAiFrame().getWorldFrame().getBall();
			IVector2 intersectionPoint = GeoMath.intersectionPoint(Geometry.getGoalLineOur(),
					new Line(ball.getVel(), ball.getPos()));
			if (getAiFrame().getTacticalField().getKeeperState()
					.equals(KeeperStateCalc.EStateId.DEFEND_BALL_VEL_DIRECTED_TO_GOAL)
					&& LegalPointChecker.checkPoint(intersectionPoint, getAiFrame(), getAiFrame().getTacticalField()))
			{
				IVector2 leadpoint = GeoMath.leadPointOnLine(getPos(), new Line(ball.getPos(), ball.getVel()));
				getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.DEFENSE)
						.add(new DrawableCircle(leadpoint, 100, Color.PINK));
				getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.DEFENSE)
						.add(new DrawableCircle(getPos(), 100, Color.RED));
				if (GeoMath.distancePP(getPos(), leadpoint) < interceptRadius)
				{
					triggerEvent(EDefenderEvent.INTERCEPT);
				}
			} else
			{
				triggerEvent(EDefenderEvent.CALM_DOWN);
			}
		} catch (MathException e)
		{
			triggerEvent(EDefenderEvent.CALM_DOWN);
		}
	}
	
	
	/**
	 * @param gameState
	 */
	public DefenderRole(final EGameStateTeam gameState)
	{
		this();
		this.gameState = gameState;
	}
	
	/**
	 * @author Felix Bayer <bayer.fel@gmail.com>
	 */
	public enum EDefenseStates
	{
		/**  */
		NORMAL,
		/**  */
		CLEAR_BALL,
	}
	
	/**
	 * @author Felix Bayer <bayer.fel@gmail.com>
	 */
	public enum EDefenderEvent
	{
		/**  */
		NEED_TO_CLEAR,
		/**  */
		CALM_DOWN,
		/***/
		INTERCEPT
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
	 * Returns the actual defensePoint
	 * 
	 * @return DefensePoints
	 */
	public IVector2 getDefPoint()
	{
		return defPoint;
	}
	
	
	// --------------------------------------------------------------------------
	// --- InnerClasses --------------------------------------------------------
	// --------------------------------------------------------------------------
	private static class YComparator implements Comparator<DefenderRole>, Serializable
	{
		/**  */
		private static final long serialVersionUID = 1794858044291002364L;
		
		
		@Override
		public int compare(final DefenderRole v1, final DefenderRole v2)
		{
			return (int) Math.signum(v1.getPos().y() - v2.getPos().y());
		}
	}
	
	private class InterceptState implements IRoleState
	{
		
		CatchSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			skill = new CatchSkill(ESkill.CATCH);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EDefenderEvent.INTERCEPT;
		}
		
	}
	
	private class ClearBallDefenseState implements IRoleState
	{
		
		private KickSkill		kickSkill	= null;
		
		private DefenderRole	parentRole	= null;
		
		private boolean		panicMode	= false;
		
		
		/**
		 * @param parentRole
		 */
		public ClearBallDefenseState(final DefenderRole parentRole)
		{
			this.parentRole = parentRole;
		}
		
		
		@Override
		public void doEntryActions()
		{
			IVector2 chipTarget = getChipTarget();
			
			kickSkill = new KickSkill(new DynamicPosition(chipTarget));
			kickSkill.setMoveMode(EMoveMode.AGGRESSIVE);
			kickSkill.setKickMode(EKickMode.POINT);
			kickSkill.setDevice(EKickerDevice.CHIP);
			setNewSkill(kickSkill);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			ITacticalField tacticalField = getAiFrame().getTacticalField();
			WorldFrame worldFrame = getAiFrame().getWorldFrame();
			
			IVector2 ballPos = DefenseAux.getBallPosDefense(worldFrame.getBall());
			
			if (!DefensePlay.switchToClear(tacticalField, getAiFrame(), parentRole))
			{
				
				triggerEvent(EDefenderEvent.CALM_DOWN);
			}
			
			if (!panicMode && isPanicMode(ballPos, tacticalField))
			{
				kickSkill.setReceiver(new DynamicPosition(getChipTarget()));
			}
		}
		
		
		private IVector2 getChipTarget()
		{
			ITacticalField tacticalField = getAiFrame().getTacticalField();
			WorldFrame worldFrame = getAiFrame().getWorldFrame();
			
			IVector2 ballPos = DefenseAux.getBallPosDefense(worldFrame.getBall());
			IVector2 defenderPos = parentRole.getBot().getPosByTime(DefenseAux.foeLookAheadDefenders);
			IVector2 enemyGoalCenter = Geometry.getGoalTheir().getGoalCenter();
			double chipDistance = DefenseAux.clearingChipDistance;
			
			IVector2 chipTarget = null;
			
			if (isPanicMode(ballPos, tacticalField))
			{
				panicMode = true;
				chipTarget = ballPos.addNew(ballPos.subtractNew(defenderPos).scaleTo(chipDistance));
			} else
			{
				panicMode = false;
				chipTarget = ballPos.addNew(enemyGoalCenter.subtractNew(ballPos).scaleTo(chipDistance));
			}
			
			return chipTarget;
		}
		
		
		private boolean isPanicMode(final IVector2 ballPos, final ITacticalField tacticalField)
		{
			BotDistance enemyClosestToBallDistance = tacticalField.getEnemyClosestToBall();
			ITrackedBot enemyClosestToBall = enemyClosestToBallDistance.getBot();
			IVector2 foe2Ball = ballPos
					.subtractNew(enemyClosestToBall.getPosByTime(DefenseAux.foeLookAheadDefenders));
			double distFoe2Ball = foe2Ball.getLength2();
			
			return distFoe2Ball < ZoneDefenseCalc.getFoe2ballClearingPanicDistance();
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EDefenseStates.CLEAR_BALL;
		}
	}
	
	private class NormalDefendState implements IRoleState
	{
		private AMoveToSkill defSkill = null;
		
		
		@Override
		public void doEntryActions()
		{
			defSkill = AMoveToSkill.createMoveToSkill();
			defSkill.getMoveCon().setOptimizeOrientation(false);
			setNewSkill(defSkill);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			
			if (defPoint != null)
			{
				double dist2DefPoint = GeoMath.distancePP(defPoint, getPos());
				if ((dist2DefPoint > noPP_dist2defPoint) || (gameState == EGameStateTeam.STOPPED))
				{
					defSkill.getMoveCon().setBotsObstacle(true);
					defSkill.getMoveCon().setBallObstacle(true);
				} else
				{
					defSkill.getMoveCon().setTheirBotsObstacle(false);
					defSkill.getMoveCon().setOurBotsObstacle(true);
					defSkill.getMoveCon().setBallObstacle(false);
				}
				
				TrackedBall ball = getAiFrame().getWorldFrame().getBall();
				
				defSkill.getMoveCon().updateLookAtTarget(DefenseAux.getBallPosDefense(ball));
				defSkill.getMoveCon().updateDestination(defPoint);
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return EDefenseStates.NORMAL;
		}
	}
}
