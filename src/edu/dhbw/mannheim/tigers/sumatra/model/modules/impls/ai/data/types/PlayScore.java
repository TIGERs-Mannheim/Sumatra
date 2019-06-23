/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 15.04.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types;

import java.io.Serializable;
import java.util.Comparator;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.PlayTuple;


/**
 * Class containing a PlayTuple and a related score.
 * 
 * @author Malte
 * 
 */
public class PlayScore implements Serializable
{
	/**  */
	private static final long						serialVersionUID		= 6080176403854601374L;
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	public static final Comparator<PlayScore>	COMPARATOR				= new ScoreComparator();
	public static final Comparator<PlayScore>	COMPARATOR_INVERSE	= new ScoreComparatorInverse();
	
	public final PlayTuple							tuple;
	public final Integer								score;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param left
	 * @param right
	 */
	public PlayScore(PlayTuple tuple, Integer score)
	{
		this.tuple = tuple;
		this.score = score;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public static class ScoreComparator implements Comparator<PlayScore>
	{
		@Override
		public int compare(PlayScore p1, PlayScore p2)
		{
			return p1.score.compareTo(p2.score);
		}
	}
	
	public static class ScoreComparatorInverse implements Comparator<PlayScore>
	{
		@Override
		public int compare(PlayScore p1, PlayScore p2)
		{
			return -p1.score.compareTo(p2.score);
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
