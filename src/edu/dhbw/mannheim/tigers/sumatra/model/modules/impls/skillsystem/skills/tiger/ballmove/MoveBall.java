/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.06.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.ballmove;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Rectanglef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;


/**
 * Calculates turn speed and move vector for moving with ball.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class MoveBall extends ABallMoveCalculator
{
	

	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Sisyphus	sisyphus;
	
	private final float		POS_TOLERANCE	= 40; // 200
	private final boolean	hasTarget;
	
	private IVector2			destination;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param dest destination the bot should move to
	 * @param sisyphus
	 */
	public MoveBall(IVector2 dest, Sisyphus sisyphus, boolean hasTarget)
	{
		super(AIConfig.getSkills().getRotatePIDConf());
		this.destination = dest;
		this.sisyphus = sisyphus;
		this.hasTarget = hasTarget;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	protected EBallMoveState doCheckState()
	{
		float currentAngle = ballPos.subtractNew(getBot().pos).getAngle();
		float targetAngle = destination.subtractNew(ballPos).getAngle();
		float diffAngle = Math.abs(currentAngle - targetAngle);
		
		// System.out.println("diffAngle " + AIMath.deg(diffAngle));
		
		if (!AIMath.isZero(diffAngle, angleTolerance))
		{
			return EBallMoveState.TURN;
		} else
		{
			if (hasTarget && destination.equals(getBot().pos, POS_TOLERANCE))
			{
				return EBallMoveState.AIM;
			} else
			{
				return EBallMoveState.MOVE;
			}
		}
	}
	

	@Override
	public IVector2 calcMoveVector(double deltaT)
	{
		Path path = calcPath();
		
		// Check: Destination reached?
		float distanceToTarget = path.getLength(getBot().pos);
		// System.out.println("dist to targetPos: " + distanceToTarget);
		
		if (path.path.size() <= 1 && distanceToTarget < POS_TOLERANCE
				&& getBot().vel.getLength2() < AIConfig.getSkills().getMoveSpeedThreshold())
		{
			moveCompleted = true;
			return Vector2.ZERO_VECTOR; // No need to move anywhere
		}
		
		final Vector2 moveVec = new Vector2(path.path.get(0));
		moveVec.subtract(getBot().pos);
		

		// ##### Velocity
		// get distance to target
		final float distanceToNextPoint = moveVec.getLength2() / 1000; // [m]
		
		// calculate the appropriate velocity
		// [m/s]
		float velocity = calcVelocity(distanceToNextPoint, 0, deltaT);
		
		// apply velocity
		moveVec.scaleTo(velocity);
		
		// ##### Convert to local bot-system
		moveVec.turn(AIMath.PI_HALF - getBot().angle);
		
		return moveVec;
	}
	

	/**
	 * calculate a path
	 * by default, this is asking Sisyphus to do that
	 * can be overridden
	 * 
	 * @param wFrame
	 * @param target
	 * @return Path
	 */
	private Path calcPath()
	{
		// ##### Path
		// Safety check: If target outside field, choose nearest point inside! (suggested by MalteM)
		// and calculate a path
		Rectanglef field = AIConfig.getGeometry().getField();
		return sisyphus.calcPath(wFrame, getBot().id, field.nearestPointInside(destination), AIConfig.getSkills()
				.getBallAsObstacle());
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	

}
