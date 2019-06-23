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
 * Scores the behaviour of the role
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class BehaviourScore extends AScore
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
	public BehaviourScore()
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
		int score = 0;
		switch (role.getBehavior())
		{
			case AGGRESSIVE:
				score = 0;
				break;
			case CREATIVE:
				score += maxDistScore / 2;
				break;
			case DEFENSIVE:
				score += maxDistScore;
				break;
			case UNKNOWN:
			default:
				break;
		}
		return score;
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
