/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.04.2011
 * Author(s): Malte
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;

/**
 * Container that holds a combination of some Plays.
 * 
 * @author Malte
 * 
 */
public class PlayTuple
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final List<EPlay> plays = new ArrayList<EPlay>();

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public PlayTuple(EPlay... plays)
	{
		for(EPlay play : plays)
		{
			this.plays.add(play);
		}
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Calculates the playable score of a given play tuple
	 * based on its plays. Makes sure, there is only one keeper, one shooter and 
	 * the number of roles fits to the given 'numberOfBots' parameter.
	 * TODO: add overloaded method, that can weight the current gameBehaviour.
	 * TODO: maybe remove checks, because they are already done in initTupleCheck
	 * 
	 * @param frame
	 * @param numberOfBots
	 * @author Malte
	 */
	public int calcPlayableScore(AIInfoFrame frame, int numberOfBots)
	{
		float score = 0;
		int keepers = 0;
		int roles = 0;
		int shooters = 0;
		for (EPlay play : plays)
		{
			APlay fake = PlayMap.getInstance().getFakePlay(play);
			keepers = fake.hasKeeperRole() ?  keepers+1 : keepers;
			roles += fake.getRoles().size();
			shooters = fake.isBallCarrying() ? shooters+1 : shooters;
			score += fake.calcPlayableScore(frame);
			// Are two equal plays in the tuple?
			if(plays.indexOf(play) != plays.lastIndexOf(play))
			{
				return 0;
			}
		}
		if (keepers != 1) {
			return 0;
		}
		if (shooters != 1) {
			return 0;
		}
		if (roles != numberOfBots) {
			return 0;
		}
		
		return (int) (score / plays.size());
	}
	
	
	public void addPlay(EPlay play)
	{
		plays.add(play);
	}
	
	public void removePlay(EPlay play)
	{
		plays.remove(play);
	}

	public List<EPlay> getPlays()
	{
		return plays;
	}

	/**
	 * Returns the numbers of all roles of this tuple.
	 * 
	 */
	public int getRoleCount()
	{
		int roles = 0;
		for(EPlay play : plays)
		{
			APlay fake = PlayMap.getInstance().getFakePlay(play);
			roles += fake.getRoleCount();
		}
		return roles;
	}
	
	@Override
	public String toString()
	{
		String s = "";
		for (EPlay play : plays)
		{
			s += play.toString()+" ";
		}
		return s;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
