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
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;


/**
 * Scores if the bot is not allowed to touch the ball
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class PenaltyScore extends AScore
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final int	maxDistScore;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public PenaltyScore()
	{
		int tmpMaxDistScore = (int) (AIConfig.getGeometry().getFieldLength() + AIConfig.getGeometry().getFieldWidth());
		maxDistScore = tmpMaxDistScore * tmpMaxDistScore;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected int doCalcScore(TrackedTigerBot tiger, ARole role, AIInfoFrame frame)
	{
		int prePenaltyScore = 0;
		
		// in some situations, bots are not allowed to touch the ball more than twice
		if (tiger.getId().equals(frame.tacticalInfo.getBotNotAllowedToTouchBall()))
		{
			prePenaltyScore += maxDistScore;
		}
		return prePenaltyScore;
	}
	
	
	@Override
	protected int doCalcScoreOnPos(IVector2 position, ARole role, AIInfoFrame frame)
	{
		return 0;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
