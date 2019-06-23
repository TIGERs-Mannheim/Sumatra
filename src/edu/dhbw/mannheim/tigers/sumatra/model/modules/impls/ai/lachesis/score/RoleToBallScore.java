/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 27, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.exceptions.RoleNullInitDestinationException;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;


/**
 * Scores the distance of the ball to the init destination of the role
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class RoleToBallScore extends AScore
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log	= Logger.getLogger(RoleToBallScore.class.getName());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public RoleToBallScore()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected int doCalcScore(TrackedTigerBot tiger, ARole role, AIInfoFrame frame)
	{
		final IVector2 dest = role.getDestination();
		if (dest == null)
		{
			throw new RoleNullInitDestinationException("The destination of role '" + role + "' is null!!!");
		}
		return calcCostOnDestination(dest, frame);
		
	}
	
	
	@Override
	protected int doCalcScoreOnPos(IVector2 position, ARole role, AIInfoFrame frame)
	{
		return calcCostOnDestination(position, frame);
	}
	
	
	private int calcCostOnDestination(IVector2 dest, AIInfoFrame frame)
	{
		int roleDestDistToBall = 0;
		
		final TrackedBall ball = frame.worldFrame.getBall();
		if (ball != null)
		{
			IVector2 ballPos = ball.getPos();
			roleDestDistToBall = Math.round(GeoMath.distancePP(ballPos, dest));
		} else
		{
			log.debug("Distance to ball not included in Role Assignment because there is no ball");
		}
		return roleDestDistToBall * roleDestDistToBall;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
