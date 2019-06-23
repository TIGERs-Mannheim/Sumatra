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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.exceptions.RoleNullInitDestinationException;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;


/**
 * Scores the requested and available features of a bot
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class RoleToBotScore extends AScore
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public RoleToBotScore()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected int doCalcScore(TrackedTigerBot tiger, ARole role, AIInfoFrame frame)
	{
		return scoreToPos(tiger.getPos(), role);
	}
	
	
	@Override
	protected int doCalcScoreOnPos(IVector2 position, ARole role, AIInfoFrame frame)
	{
		return scoreToPos(position, role);
	}
	
	
	private int scoreToPos(IVector2 position, ARole role)
	{
		int score = 0;
		final IVector2 dest = role.getDestination();
		if (dest == null)
		{
			throw new RoleNullInitDestinationException("The destination of role '" + role + "' is null!!!");
		}
		int dist = Math.round(GeoMath.distancePP(position, dest));
		// distance squared will make long paths more expensive.
		// Else, Bots that already have their position will more likely stay their, but it could be better if it takes the
		// other position in the time, in that other bots have to take their way to their position
		score = dist * dist;
		return score;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
