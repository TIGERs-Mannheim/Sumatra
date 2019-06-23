/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 27, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.ScoreResult.EUsefulness;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;


/**
 * Interface for different scores for te role assigner
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public abstract class AScore
{
	private boolean	active	= true;
	
	
	/**
	 * activates or deactivates the score.
	 * 
	 * @param active
	 */
	public void setActive(final boolean active)
	{
		this.active = active;
	}
	
	
	/**
	 * @param tiger
	 * @param role
	 * @param frame
	 * @return
	 */
	protected abstract ScoreResult doCalcScore(TrackedTigerBot tiger, ARole role, MetisAiFrame frame);
	
	
	/**
	 * Calculates the score for the given input
	 * 
	 * @param tiger
	 * @param role
	 * @param frame
	 * @return
	 */
	public ScoreResult calcScore(final TrackedTigerBot tiger, final ARole role, final MetisAiFrame frame)
	{
		if (active)
		{
			return doCalcScore(tiger, role, frame);
		}
		return ScoreResult.defaultResult();
	}
	
	
	/**
	 * Get cumulated ScoreResult from all score calculators
	 * 
	 * @param scores
	 * @param tiger
	 * @param role
	 * @param frame
	 * @return
	 */
	public static ScoreResult getCumulatedResult(final Collection<AScore> scores, final TrackedTigerBot tiger,
			final ARole role, final MetisAiFrame frame)
	{
		List<ScoreResult> results = new ArrayList<ScoreResult>(scores.size());
		for (AScore score : scores)
		{
			results.add(score.calcScore(tiger, role, frame));
		}
		return getCumulatedResult(results);
	}
	
	
	/**
	 * Create a cumulated result from all scoreResults.
	 * It will be the worst usefulness with product of degree
	 * 
	 * @param scores
	 * @return
	 */
	public static ScoreResult getCumulatedResult(final List<ScoreResult> scores)
	{
		int degree = 0;
		EUsefulness usefulness = EUsefulness.NEUTRAL;
		for (ScoreResult result : scores)
		{
			int lvl = result.getUsefulness().getLevel();
			if (lvl > usefulness.getLevel())
			{
				usefulness = result.getUsefulness();
				degree = result.getDegree();
			}
			else if (lvl == usefulness.getLevel())
			{
				degree += result.getDegree();
			}
		}
		return new ScoreResult(usefulness, degree);
	}
	
}
