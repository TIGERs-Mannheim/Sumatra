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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;


/**
 * Interface for different scores for te role assigner
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public abstract class AScore
{
	private boolean	active	= true;
	
	
	/**
	 * activates or deactivates the score.
	 * @param active
	 */
	public void setActive(boolean active)
	{
		this.active = active;
	}
	
	
	/**
	 * @param tiger
	 * @param role
	 * @param frame
	 * @return high score is a low priority in role assignment
	 */
	protected abstract int doCalcScore(TrackedTigerBot tiger, ARole role, AIInfoFrame frame);
	
	
	/**
	 * Calculates the score for the given input
	 * @param tiger
	 * @param role
	 * @param frame
	 * @return
	 */
	public int calcScore(TrackedTigerBot tiger, ARole role, AIInfoFrame frame)
	{
		if (active)
		{
			return doCalcScore(tiger, role, frame);
		}
		return 0;
	}
	
	
	/**
	 * Calculates Score on a vector instead of a role
	 * @param position
	 * @param role
	 * @param frame
	 * @return
	 */
	protected abstract int doCalcScoreOnPos(IVector2 position, ARole role, AIInfoFrame frame);
	
	
	/**
	 * Calculates the score for the given input
	 * @param position
	 * @param role
	 * @param frame
	 * @return
	 */
	public int calcScoreOnPos(IVector2 position, ARole role, AIInfoFrame frame)
	{
		if (active)
		{
			return doCalcScoreOnPos(position, role, frame);
		}
		return 0;
	}
	
}
