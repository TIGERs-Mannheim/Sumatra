/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.04.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.PlayMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.PlayTuple;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.PlayScore;


/**
 * This is the match-state of the {@link AthenaGuiAdapter}. It does not replace play-finding or role-assignment but lets
 * the user force new play-decision or view the play-scores
 * 
 * @see AthenaGuiAdapter
 * @see AthenaControl
 * @see IGuiAdapterState
 * 
 * @author Gero
 */
public class MatchModeAdapterState extends AGuiAdapterState
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final List<PlayTuple>	tuples	= PlayMap.getInstance().getTuples();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param adapter
	 */
	public MatchModeAdapterState(AthenaGuiAdapter adapter)
	{
		super(adapter);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void beforePlayFinding(AIInfoFrame current, AIInfoFrame previous)
	{
		if (hasChanged())
		{
			if (getControl().isForceNewDecision())
			{
				current.playStrategy.setForceNewDecision();
				
				getControl().forceNewDecision(false); // Event, disable
			} else
			{
				current.playStrategy.setStateChanged(true);
			}
		}
	}
	

	@Override
	public void betweenPlayRole(AIInfoFrame current, AIInfoFrame previous)
	{
		
		// Calc scores
		List<PlayScore> scores = new ArrayList<PlayScore>();
		for (PlayTuple tuple : tuples)
		{
			scores.add(new PlayScore(tuple, tuple.calcPlayableScore(current, 5)));
		}
		Collections.sort(scores, PlayScore.COMPARATOR_INVERSE);
		
		// Add to frame
		current.playStrategy.getBestPlays().addAll(scores);
		
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public boolean overridePlayFinding()
	{
		return false;
	}
	

	@Override
	public boolean overrideRoleAssignment()
	{
		return false;
	}
}
