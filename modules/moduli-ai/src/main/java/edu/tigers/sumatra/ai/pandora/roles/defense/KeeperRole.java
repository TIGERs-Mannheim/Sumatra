/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.10.2010
 * Author(s): Malte
 * Rewirte by Chris
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.defense;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.math.AiMath;
import edu.tigers.sumatra.ai.data.math.OffensiveMath;
import edu.tigers.sumatra.ai.metis.defense.KeeperStateCalc;
import edu.tigers.sumatra.ai.metis.support.data.AdvancedPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.MathException;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.CatchSkill;
import edu.tigers.sumatra.skillsystem.skills.KickSkill;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EKickMode;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EMoveMode;
import edu.tigers.sumatra.skillsystem.skills.MoveToTrajSkill;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * The Keeper protect the Goal and tries to catch every shoot into the goal.
 * 
 * @author ChrisC
 */
public class KeeperRole extends ARole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@Configurable(comment = "Max acceleration of Keeper")
	private static double	keeperAcc							= 4.5;
	
	@Configurable(comment = "Angle of Keeper towards the ball in NormalBlockState")
	private static double	turnAngleOfKeeper					= Math.PI / 2;
	
	@Configurable(comment = "Dist [mm] - Distance around the penalty area, inside the bot will pass controlled, outside the bot will just shoot")
	private static int		safetyAroundPenaltyKickType	= -200;
	
	@Configurable(comment = "Dist [mm] to GoalCenter in NormalBlockState")
	private static double	distToGoalCenter					= Geometry.getDistanceToPenaltyArea() / 2;
	
	@Configurable(comment = "Keeper goes out if redirector is behind this margin added to the penaultyarea")
	private static double	goOutWhileRedirectMargin		= Geometry.getGoalOur().getGoalCenter().x() / 2;
	
	private boolean			allowChipkick						= true;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public KeeperRole()
	{
		super(ERole.KEEPER);
		setInitialState(new MoveOutsidePenaltyState());
		
		// Every Transition should be possible cause of less complexity and performance
		addTransition(KeeperStateCalc.EStateId.NORMAL, new NormalBlockState());
		addTransition(KeeperStateCalc.EStateId.DEFEND_BALL_VEL_DIRECTED_TO_GOAL, new BallVelIsDirectedToGoalState());
		addTransition(KeeperStateCalc.EStateId.DEFEND_REDIRECT, new CatchRedirectState());
		addTransition(KeeperStateCalc.EStateId.BALL_PLACEMENT, new BallPlacementState());
		addTransition(KeeperStateCalc.EStateId.CHIP_FAST, new ChipFastState());
		addTransition(KeeperStateCalc.EStateId.GO_OUT, new GoOutState());
		addTransition(KeeperStateCalc.EStateId.MOVE_TO_PENALTYAREA, new MoveOutsidePenaltyState());
		addTransition(KeeperStateCalc.EStateId.STOPPED, new BallPlacementState());
	}
	
	
	@Override
	protected void beforeUpdate()
	{
		if (getAiFrame().getTacticalField().getKeeperState() != getCurrentState())
		{
			triggerEvent(getAiFrame().getTacticalField().getKeeperState());
		}
		super.beforeUpdate();
	}
	
	// --------------------------------------------------------------------------
	// --- States --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * If there is an enemy near our penaultyarea with the ball, the Keeper should go out to block a shoot on the goal
	 * 
	 * @author ChrisC
	 */
	private class GoOutState implements IRoleState
	{
		MoveToTrajSkill posSkill;
		
		
		@Override
		public void doEntryActions()
		{
			posSkill = new MoveToTrajSkill();
			posSkill.getMoveCon().getMoveConstraints().setAccMax(keeperAcc);
			posSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
			posSkill.getMoveCon().setDestinationOutsideFieldAllowed(true);
			posSkill.getMoveCon().setBotsObstacle(false);
			posSkill.getMoveCon().setBallObstacle(false);
			posSkill.getMoveCon().setArmChip(true);
			setNewSkill(posSkill);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			posSkill.getMoveCon().updateDestination(GeoMath.stepAlongLine(Geometry.getGoalOur().getGoalCenter(),
					getWFrame().getBall().getPos(), Geometry.getPenaltyAreaOur().getRadiusOfPenaltyArea()));
			posSkill.getMoveCon().updateLookAtTarget(GeoMath.stepAlongLine(Geometry.getGoalOur().getGoalCenter(),
					getWFrame().getBall().getPos(), Geometry.getPenaltyAreaOur().getRadiusOfPenaltyArea()));
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return KeeperStateCalc.EStateId.GO_OUT;
		}
		
	}
	
	/**
	 * ball placement is active, the keeper should move away from the ball
	 * 
	 * @author ChrisC
	 */
	private class BallPlacementState implements IRoleState
	{
		MoveToTrajSkill posSkill;
		
		
		@Override
		public void doEntryActions()
		{
			posSkill = new MoveToTrajSkill();
			posSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
			posSkill.getMoveCon().setBallObstacle(true);
			posSkill.getMoveCon().setGoalPostObstacle(true);
			setNewSkill(posSkill);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 ballPos = getWFrame().getBall().getPos();
			
			IVector2 destination = GeoMath.stepAlongLine(Geometry.getGoalOur().getGoalCenter(),
					ballPos.subtractNew(Geometry.getGoalOur().getGoalCenter()), distToGoalCenter);
			
			try
			{
				double distToBall = Math.max(0, ((Geometry.getBotToBallDistanceStop() + (Geometry.getBotRadius() * 2))
						- destination.subtractNew(getWFrame().getBall().getPos()).getLength()));
				double distanceToGoalCenterWithBall = distToGoalCenter - distToBall;
				
				destination = GeoMath.stepAlongLine(GeoMath.intersectionPoint(ballPos,
						ballPos.subtractNew(Geometry.getGoalOur().getGoalCenter()), Geometry.getGoalOur().getGoalCenter(),
						AVector2.Y_AXIS), ballPos, distanceToGoalCenterWithBall);
				
				// Ball behind Goal?
				if (getWFrame().getBall().getPos().x() < Geometry.getGoalOur().getGoalCenter().x())
				{
					if (destination.y() > Geometry.getGoalOur().getGoalPostLeft().y())
					{
						destination = new Vector2(Geometry.getGoalOur().getGoalPostLeft().x() + Geometry.getBotRadius(),
								Geometry.getGoalOur().getGoalPostLeft().y());
					}
					if (destination.y() < Geometry.getGoalOur().getGoalPostRight().y())
					{
						destination = new Vector2(Geometry.getGoalOur().getGoalPostRight().x() + Geometry.getBotRadius(),
								Geometry.getGoalOur().getGoalPostRight().y());
					}
				}
				
			} catch (MathException e)
			{
			}
			destination = checkGoalPosts(destination);
			
			posSkill.getMoveCon().updateDestination(destination);
			posSkill.getMoveCon().updateTargetAngle(calcDefendingOrientation());
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			if (getAiFrame().getTacticalField().getGameState() == EGameStateTeam.BALL_PLACEMENT_THEY)
			{
				return KeeperStateCalc.EStateId.BALL_PLACEMENT;
			}
			return KeeperStateCalc.EStateId.STOPPED;
		}
		
	}
	
	/**
	 * Block intersection point BallVel -> Goal
	 * 
	 * @author ChrisC
	 */
	private class BallVelIsDirectedToGoalState implements IRoleState
	{
		private CatchSkill catchSkill;
		
		
		@Override
		public void doEntryActions()
		{
			catchSkill = new CatchSkill(ESkill.CATCH);
			catchSkill.getMoveCon().getMoveConstraints().setAccMax(keeperAcc);
			catchSkill.getMoveCon().setDestinationOutsideFieldAllowed(true);
			catchSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
			catchSkill.getMoveCon().setBotsObstacle(false);
			setNewSkill(catchSkill);
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
			return KeeperStateCalc.EStateId.DEFEND_BALL_VEL_DIRECTED_TO_GOAL;
		}
		
	}
	
	
	/**
	 * Move to the PenaltyArea
	 * 
	 * @author PhilippP
	 */
	private class MoveOutsidePenaltyState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			AMoveToSkill skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().setDestinationOutsideFieldAllowed(true);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			skill.getMoveCon().setGoalPostObstacle(true);
			skill.getMoveCon().updateDestination(Geometry.getGoalOur().getGoalCenter());
			setNewSkill(skill);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return KeeperStateCalc.EStateId.MOVE_TO_PENALTYAREA;
		}
		
		
		@Override
		public void doUpdate()
		{
		}
	}
	
	/**
	 * If the KeeperStateCalc detect a redirect of the enemy, the keeper drives direct between the redirect foe and the
	 * Goalcenter
	 * 
	 * @author ChrisC
	 */
	private class CatchRedirectState implements IRoleState
	{
		MoveToTrajSkill	posSkill;
		boolean				flag	= false;
		
		
		@Override
		public void doEntryActions()
		{
			posSkill = new MoveToTrajSkill();
			posSkill.getMoveCon().updateDestination(new Vector2());
			posSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
			posSkill.getMoveCon().setBotsObstacle(false);
			posSkill.getMoveCon().setBallObstacle(false);
			posSkill.getMoveCon().getMoveConstraints().setAccMax(keeperAcc);
			posSkill.getMoveCon().setGoalPostObstacle(false);
			setNewSkill(posSkill);
			
			flag = false;
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 redirectBot;
			BotID redirectBotId = OffensiveMath.getBestRedirector(getWFrame(), getWFrame().getFoeBots());
			if (redirectBotId != null)
			{
				redirectBot = getWFrame().foeBots.get(redirectBotId).getPos();
				Vector2 destination = GeoMath.stepAlongLine(Geometry.getGoalOur().getGoalCenter(), redirectBot,
						distToGoalCenter);
				posSkill.getMoveCon().updateDestination(destination);
				if (GeoMath.distancePP(getPos(), destination) < (Geometry.getBotRadius() / 2))
				{
					posSkill.getMoveCon().updateLookAtTarget(redirectBot);
				}
			} else
			{
				redirectBot = getWFrame()
						.getBot(AiMath.getFoeBotsNearestToPointSorted(getAiFrame(), getWFrame().getBall().getPos()).get(0))
						.getPos();
			}
			boolean isKeeperBetweenRedirectAndGoal = GeoMath.distancePL(getPos(), Geometry.getGoalOur().getGoalCenter(),
					redirectBot) < Geometry.getBotRadius();
			boolean isGoOutUsefull = redirectBot.x() < goOutWhileRedirectMargin;
			// Keeper in place -> drive to the redirector to protect more space from the goal
			if ((isKeeperBetweenRedirectAndGoal || flag) && isGoOutUsefull)
			{
				flag = true;
				posSkill.getMoveCon()
						.updateDestination(GeoMath.stepAlongLine(Geometry.getGoalOur().getGoalCenter(),
								redirectBot, Geometry.getPenaltyAreaOur().getRadiusOfPenaltyArea()));
				posSkill.getMoveCon().updateLookAtTarget(redirectBot);
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return KeeperStateCalc.EStateId.DEFEND_REDIRECT;
		}
		
	}
	
	/**
	 * If the ball is near the penaultyarea, the keeper should chip it to the best passtarget 3000mm away
	 * 
	 * @author ChrisC
	 */
	private class ChipFastState implements IRoleState
	{
		KickSkill skill;
		
		
		@Override
		public void doEntryActions()
		{
			final IVector2 ballPos = getWFrame().getBall().getPos();
			final IVector2 goalCenter = Geometry.getGoalOur().getGoalCenter();
			
			// in general a very dangerous situation: ball is close to penalty area, if our offensive or defensive would
			// try to get
			// it, it s a penalty shot for the enemy! (He needs to get his diameter of 180 behind the ball) So the
			// keeper should cover the shooting line ALL THE TIME. So it is too risky to chip to a reasonable direction.
			// Just drive straight to the ball and chip straight
			IVector2 chippingPosition = GeoMath.stepAlongLine(goalCenter, ballPos, 3000);
			EMoveMode moveMode = EMoveMode.PANIC;
			
			if (Geometry.getPenaltyAreaOur().isPointInShape(ballPos, safetyAroundPenaltyKickType))
			{
				// not dangerous, the ball is inside the penalty area, if an enemy tries to get it, we get a freekick
				// so, regard this situation like a freekick
				chippingPosition = new Vector2(Geometry.getCenter());
				for (AdvancedPassTarget bot : getAiFrame().getTacticalField().getAdvancedPassTargetsRanked())
				{
					if (bot.x() > (-Geometry.getFieldLength() / 3.0))
					{
						chippingPosition = bot.getXYVector();
						if (GeoMath.distancePP(chippingPosition, ballPos) < 3000)
						{
							chippingPosition = GeoMath.stepAlongLine(ballPos, chippingPosition, 3000);
						}
						break;
					}
				}
				
				moveMode = EMoveMode.CHILL_TOUCH;
			}
			skill = new KickSkill(new DynamicPosition(chippingPosition));
			skill.setDevice(EKickerDevice.CHIP);
			skill.setMoveMode(moveMode);
			skill.setKickMode(EKickMode.PASS);
			skill.getMoveCon().setGoalPostObstacle(true);
			skill.getMoveCon().setPenaltyAreaAllowedOur(true);
			setNewSkill(skill);
		}
		
		
		@Override
		public void doUpdate()
		{
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return KeeperStateCalc.EStateId.CHIP_FAST;
		}
	}
	
	/**
	 * If no other State is active the keeper should block the direct line between the ball and the Goalcenter.
	 * To prepare for a direct shoot the keeper turn around, cause of faster movement back and forward.
	 * 
	 * @author ChrisC
	 */
	private class NormalBlockState implements IRoleState
	{
		private MoveToTrajSkill posSkill;
		
		
		@Override
		public void doEntryActions()
		{
			posSkill = new MoveToTrajSkill();
			posSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
			posSkill.getMoveCon().setDestinationOutsideFieldAllowed(true);
			posSkill.getMoveCon().setBotsObstacle(false);
			posSkill.getMoveCon().setBallObstacle(true);
			posSkill.getMoveCon().getMoveConstraints().setAccMax(keeperAcc);
			posSkill.getMoveCon().setGoalPostObstacle(false);
			setNewSkill(posSkill);
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 ballPos = getWFrame().getBall().getPosByTime(0.1);
			
			IVector2 bisector = GeoMath.calculateBisector(ballPos, Geometry.getGoalOur().getGoalPostLeft(), Geometry
					.getGoalOur().getGoalPostRight());
			
			IVector2 destination = bisector.addNew(ballPos.subtractNew(bisector).scaleToNew(distToGoalCenter));
			
			// Ball behind Goal?
			if (destination.x() < Geometry.getGoalOur().getGoalCenter().x())
			{
				if (destination.y() > Geometry.getGoalOur().getGoalPostLeft().y())
				{
					destination = new Vector2(Geometry.getGoalOur().getGoalPostLeft().x() + Geometry.getBotRadius(),
							Geometry.getGoalOur().getGoalPostLeft().y());
				}
				if (destination.y() < Geometry.getGoalOur().getGoalPostRight().y())
				{
					destination = new Vector2(Geometry.getGoalOur().getGoalPostRight().x() + Geometry.getBotRadius(),
							Geometry.getGoalOur().getGoalPostRight().y());
				}
			}
			
			destination = checkGoalPosts(destination);
			
			posSkill.getMoveCon().updateDestination(destination);
			posSkill.getMoveCon().updateTargetAngle(calcDefendingOrientation());
			
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return KeeperStateCalc.EStateId.NORMAL;
		}
		
		
	}
	
	
	// --------------------------------------------------------------------------
	// ------------------------ End States --------------------------------------
	// --------------------------------------------------------------------------
	protected double calcDefendingOrientation()
	{
		return getAiFrame().getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle() + turnAngleOfKeeper;
	}
	
	
	/**
	 * Check if the given destination is inside a goalpost
	 * 
	 * @param destination to check
	 * @return new destination slightly offsetted, if the destination was inside a goalpost
	 * @author ChrisC
	 */
	private IVector2 checkGoalPosts(final IVector2 destination)
	{
		IVector2 newDestination = destination;
		// Is Bot able to hit Goalpost?
		
		if (GeoMath.distancePP(destination,
				Geometry.getGoalOur().getGoalPostLeft()) <= (Geometry.getBotRadius() + Geometry.getBallRadius()))
		{
			newDestination = GeoMath.stepAlongLine(Geometry.getGoalOur().getGoalPostLeft(), destination,
					Geometry.getBotRadius() + Geometry.getBallRadius());
		} else if (GeoMath.distancePP(destination,
				Geometry.getGoalOur().getGoalPostRight()) <= (Geometry.getBotRadius() + Geometry.getBallRadius()))
		{
			newDestination = GeoMath.stepAlongLine(Geometry.getGoalOur().getGoalPostRight(), destination,
					Geometry.getBotRadius() + Geometry.getBallRadius());
		}
		if ((newDestination.x() < Geometry.getGoalOur().getGoalCenter().x())
				&& ((newDestination.y() > (Geometry.getGoalOur().getGoalPostLeft().y() - Geometry.getBotRadius()))
						|| (newDestination.y() < (Geometry.getGoalOur().getGoalPostRight().y() + Geometry.getBotRadius()))))
		{
			newDestination = new Vector2(Geometry.getGoalOur().getGoalCenter().x() + Geometry.getBotRadius(),
					newDestination.y());
		}
		return newDestination;
		
	}
	
	
	@Override
	public boolean isKeeper()
	{
		return true;
	}
	
	
	/**
	 * @return the allowChipkick
	 */
	public boolean isAllowChipkick()
	{
		return allowChipkick;
	}
	
	
	/**
	 * @param allowChipkick the allowChipkick to set
	 */
	public void setAllowChipkick(final boolean allowChipkick)
	{
		this.allowChipkick = allowChipkick;
	}
	
	
	/**
	 * @return the distToGoalCenter
	 */
	public static double getDistToGoalCenter()
	{
		return distToGoalCenter;
	}
}
