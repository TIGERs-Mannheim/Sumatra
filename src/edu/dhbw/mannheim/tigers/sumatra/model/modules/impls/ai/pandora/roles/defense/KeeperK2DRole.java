/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.11.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Circlef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Linef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.AimingCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.KeeperPlus2DefenderPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * Keeper role for the {@link KeeperPlus2DefenderPlay}
 * 
 * @author Malte
 * 
 */
public class KeeperK2DRole extends ADefenseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -6391074063762130449L;
	
	private final Goal			goal					= AIConfig.getGeometry().getGoalOur();
	private final float			BOT_RADIUS			= AIConfig.getGeometry().getBotRadius();
	private final float			GOAL_DISTANCE		= AIConfig.getGeometry().getFieldLength() / 2;
	
	/** Indicates wheter the keeper shall try to gat the ball, aim and shoot it away! */
	private boolean				getBall				= false;
	

	/** Vector from Keeper to the {@link KeeperPlus2DefenderPlay#protectAgainst protectAgainst} */
	private Vector2				keeperToProtectAgainst;
	
	/**
	 * defines the circle in which the keeper is allowed to stay
	 * TODO: Actually its not a circle!
	 */
	private final int				RADIUS				= AIConfig.getRoles().getKeeperK2D().getRadius();
	
	/** Keeper Start Position: In the Middle of the Goal on the goalline */
	private Vector2f				goalieStartPos		= new Vector2f(goal.getGoalCenter());
	
	/** Where the keeper has to go finally. */
	private Vector2				destination;
	
	private boolean				criticalAngle		= false;
	
	/** Designated keeper position. calculated in #beforeUpdate in the play. */
	private Vector2				keeperPos;
	
	private final AimingCon		aimingCon;
	
	private ERefereeCommand latestCmd = null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public KeeperK2DRole()
	{
		super(ERole.KEEPER_K2D);
		aimingCon = new AimingCon(AIMath.deg2rad(8));
		aimingCon.setNearTolerance(AIConfig.getTolerances().getNearBall());
		addCondition(aimingCon);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Real update method!
	 * Keeper destination is calculated here.
	 * @param currentFrame
	 * 
	 */
	public Vector2 calcDestination(AIInfoFrame currentFrame)
	{
		Vector2f protectAgainst = currentFrame.worldFrame.ball.pos;
		destination = new Vector2(goalieStartPos);
		// bot has to stand in the bisector ("Winklehalbierende") to cover the goal as much as possible
		destination.y = AIMath.calculateBisector(protectAgainst, goal.getGoalPostLeft(), goal.getGoalPostRight()).y;
		
		// forces the bot not to drive outside the goal area
		keeperToProtectAgainst = protectAgainst.subtractNew(destination);
		keeperToProtectAgainst.scaleTo(RADIUS);
		destination.add(keeperToProtectAgainst);
		
		// "Hinter dem Gehäuse aus Holz von Buchen, hat der Tormann nichts zu suchen!"
		// TODO: If the Ball is close to the goalline. Maybe take one defender
		// not for blocking but covering the goal area to avoid header goals.
		if (destination.x < -GOAL_DISTANCE + BOT_RADIUS + 200)
		{
			destination.x = -GOAL_DISTANCE + BOT_RADIUS + 200;
		}
		return destination;
	}
	

	@Override
	public void update(AIInfoFrame currentFrame)
	{
		if(currentFrame.refereeMsg != null)
		{
			latestCmd = currentFrame.refereeMsg.cmd;
		}
		Vector2 destination;
		IVector2 lookAt;
		TrackedBall ball = currentFrame.worldFrame.ball;
		// Ball is in the center
		if (!criticalAngle)
		{			
			lookAt = ball.pos;
		}
		// Ball is on the sides
		else
		{
			// TODO: use most dangeros enemy here!
			Vector2f protectAgainst = currentFrame.tacticalInfo.getOpponentPassReceiver().pos;
			destination = new Vector2(goalieStartPos.x() + 100, goalieStartPos.y());
			// bot has to stand in the bisector ("Winklehalbierende") to cover the goal as much as possible
			destination.y = AIMath.calculateBisector(protectAgainst, goal.getGoalPostLeft(), goal.getGoalPostRight()).y;
			lookAt = protectAgainst;
		}
		// Wird der Ball aufs Tor geschossen?
		Vector2f interceptPoint;
		if (!ball.vel.equals(AVector2.ZERO_VECTOR))
		{
			Linef ballShootLine = new Linef(ball.pos, ball.vel);
			try
			{
				interceptPoint = new Vector2f(AIMath.intersectionPoint(ballShootLine, new Line(goal.getGoalCenter(),
						AVector2.Y_AXIS)));
			} catch (MathException err)
			{
				interceptPoint = new Vector2f(0, 99999);
			}
			
			// Ball wird auf unser Tor geschossen!
			if (ball.vel.x() < -0.2 && interceptPoint.y() < goal.getGoalPostLeft().y() + 50
					&& interceptPoint.y() > goal.getGoalPostRight().y() - 50)
			{
				destination = AIMath.leadPointOnLine(this.getPos(currentFrame), ballShootLine);
			} else
			{
				destination = keeperPos;
			}
		} else
		{
			destination = keeperPos;
		}
		

		// Wenn ball bei keeper rumliegt...soll er sich den holen!
		getBall = currentFrame.tacticalInfo.isBallInOurPenArea();
		
		if(latestCmd == ERefereeCommand.Stop)
		{
			getBall = false;
			Circlef ballCircle = new Circlef(ball.pos, 500);
			if(ballCircle.isPointInShape(destination))
			{
				destination = ballCircle.nearestPointOutside(destination);
			}
		}
		
		// Update Cons
		destCon.updateDestination(destination);
		lookAtCon.updateTarget(lookAt);
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		if (getBall)
		{
			Vector2 aimingTarget;
			try
			{
				aimingTarget = AIMath.intersectionPoint(new Line(AIConfig.getGeometry().getPenaltyMarkOur(),
						AVector2.Y_AXIS), Line.newLine(AIConfig.getGeometry().getField().bottomRight(), AIConfig
						.getGeometry().getField().bottomLeft()));
			} catch (MathException err)
			{
				aimingTarget = new Vector2(AIConfig.getGeometry().getCenter());
			}
			aimingCon.updateAimingTarget(aimingTarget);
			
			if (aimingCon.checkCondition(wFrame))
			{
				skills.kickArm();
				skills.kickAuto();
				skills.dribble(false);
			} else
			{
				skills.dribble(true);
				skills.aiming(aimingCon, EGameSituation.GAME);
				skills.disarm();
			}
		} else
		{
			skills.dribble(false);
			super.calculateSkills(wFrame, skills);
			float angle = wFrame.tigerBots.get(getBotID()).angle;
			if (angle < AIMath.PI_QUART && angle > -AIMath.PI_QUART)
			{
				skills.kickArm();
			} else
			{
				skills.disarm();
			}
			
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public boolean isKeeper()
	{
		return true;
	}
	

	/**
	 * @param criticalAngle the criticalAngle to set
	 */
	public void setCriticalAngle(boolean criticalAngle)
	{
		this.criticalAngle = criticalAngle;
	}
	

	/**
	 * @return the criticalAngle
	 */
	public boolean isCriticalAngle()
	{
		return criticalAngle;
	}
	

	/**
	 * Sets the designated keeper position, calculated by the Play.
	 * 
	 * @param keeperPos
	 */
	public void setKeeperPos(Vector2f keeperPos)
	{
		this.keeperPos = new Vector2(keeperPos);
		
	}
	

}
