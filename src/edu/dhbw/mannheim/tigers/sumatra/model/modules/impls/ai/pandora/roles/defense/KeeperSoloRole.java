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

import edu.dhbw.mannheim.tigers.sumatra.model.data.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.KeeperSoloPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * <a href="http://www.gockel-09.de/91=92=1a.jpg">
 * Keeper</a> Role for the {@link KeeperSoloPlay}.
 * 
 * 
 * @author Malte
 * 
 */
public class KeeperSoloRole extends ADefenseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= -1193163364318765871L;
	
	private final Goal			goal					= AIConfig.getGeometry().getGoalOur();
	private final float			BOT_RADIUS			= AIConfig.getGeometry().getBotRadius();
	private final float			GOAL_DISTANCE		= AIConfig.getGeometry().getFieldLength() / 2;
	

	/** Ball position */
	private Vector2				protectAgainst;
	
	/** Vector from Keeper to the Ball */
	private Vector2				keeperToProtectAgainst;
	
	/**
	 * defines the circle in which the keeper is allowed to stay
	 * TODO: Actually its not a circle!
	 */
	private final int				RADIUS				= AIConfig.getRoles().getKeeperSolo().getRadius();
	
	/** Keeper Start Position: In the Middle of the Goal on the goalline */
	private Vector2f				goalieStartPos		= AIConfig.getGeometry().getGoalOur().getGoalCenter();
	
	/** Where the keeper has to go finally. */
	private Vector2				destination;
	
	/** Velocity vector of the ball. */
	private Vector2f				ballVel;
	
	/**
	 * If the velocity of the ball exceed that limit
	 * and directs to the goal, keeper will try to hinder it.
	 * Since we play on the negative side the value is negative, too.
	 */
	private final float			BALL_VEL_THRESHHOLD		= -3;
	
	/** Intercection where the goalline an the ball vector cut */
	private Vector2				intersectPoint;
	
	
	/** Determines wheter the keeper shall drive with full speed!*/
	private boolean fullSpeed = false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public KeeperSoloRole()
	{
		super(ERole.KEEPER_SOLO);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void update(AIInfoFrame currentFrame)
	{
		fullSpeed = false;
		protectAgainst = new Vector2(currentFrame.worldFrame.ball.pos);
		ballVel = currentFrame.worldFrame.ball.vel;
		// If the ball velocity is zero, the intersection point is null.
		try
		{
			intersectPoint = AIMath.intersectionPoint(protectAgainst, ballVel, goalieStartPos, Vector2.Y_AXIS);
		} catch (MathException err)
		{
			intersectPoint = null;
		}
		
		// If the ball moves very fast and directs to the goal goalie has to block the line ball-goal!
		if (intersectPoint != null && intersectPoint.y > goal.getGoalPostRight().y
				&& intersectPoint.y < goal.getGoalPostLeft().y)
		{
			// Goalie moves directly in the line ball-goal.
			Vector2 currentGoaliePos = new Vector2(currentFrame.worldFrame.tigerBots.get(this.getBotID()).pos);
			destination = AIMath.leadPointOnLine(currentGoaliePos, protectAgainst, intersectPoint);
			// The speed in direction to the goal is very high?
			if(ballVel.x() < BALL_VEL_THRESHHOLD)
			{
				fullSpeed = true;
			}

		} else
		{
			destination = new Vector2(goalieStartPos);
			// bot has to stand in the bisector ("Winklehalbierende") to cover the goal as much as possible
			destination.y = AIMath.calculateBisector(protectAgainst, goal.getGoalPostLeft(), goal.getGoalPostRight()).y;
			
			// forces the bot not to drive outside the goal area
			keeperToProtectAgainst = protectAgainst.subtractNew(destination);
			keeperToProtectAgainst.multiply(RADIUS / keeperToProtectAgainst.getLength2());
			destination.add(keeperToProtectAgainst);
			
			// "Hinter dem Gehäuse aus Holz von Buchen, hat der Tormann nichts zu suchen!"
			if (destination.x < -GOAL_DISTANCE + BOT_RADIUS + 40)
			{
				destination.x = -GOAL_DISTANCE + BOT_RADIUS + 40;
			}
		}
		
		destCon.updateDestination(destination);
		lookAtCon.updateTarget(protectAgainst);
	}
	
	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		
		boolean dest = destCon.checkCondition(wFrame);
		boolean angl = lookAtCon.checkCondition(wFrame);
		
		// conditions completed?
		if (!dest || !angl)
		{
			if(fullSpeed)
			{
				skills.moveFast(destination);
			}
			else{
				skills.moveTo(destCon.getDestination(), lookAtCon.getLookAtTarget());
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
	
}
