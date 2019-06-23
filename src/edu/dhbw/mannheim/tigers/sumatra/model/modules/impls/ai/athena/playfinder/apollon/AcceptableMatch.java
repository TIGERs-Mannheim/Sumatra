/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 18, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon;

import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.SumatraMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EMatchBehavior;


/**
 * This class calculates an acceptable match parameter for learning play finder with current aiframe info.
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 */
public class AcceptableMatch
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log								= Logger.getLogger(AcceptableMatch.class.getName());
	
	private static final double	MAX_CHANGE						= 0.05;
	private static final int		COUNT_FOR_CHANGE_SUCCESS	= 8;
	private static final int		COUNT_FOR_CHANGE_RANDOM		= 30;
	
	private double						acceptableMatch;
	private int							successDecisions				= 0;
	private int							randomDecisions				= 0;
	private double						changeFactor;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param matchBehavior
	 */
	public AcceptableMatch(EMatchBehavior matchBehavior)
	{
		switch (matchBehavior)
		{
			case DEFENSIVE:
			case CONSERVATIVE:
				changeFactor = 0.5;
				break;
			case AGGRESSIVE:
			case CREATIVE:
				changeFactor = 1.0;
				break;
			case NOT_DEFINED:
			default:
				changeFactor = 0.0;
				break;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Calculates the new acceptable match
	 */
	private void calculateAccaptableMatch()
	{
		if ((successDecisions == 0) && (randomDecisions == 0))
		{
			// nothing changed since last call or first call
			return;
		}
		if ((successDecisions >= COUNT_FOR_CHANGE_SUCCESS) && (randomDecisions == 0))
		{
			increase(changeFactor);
			// reset counter
			successDecisions = 0;
		} else if ((randomDecisions >= COUNT_FOR_CHANGE_RANDOM) && (successDecisions == 0))
		{
			decrease(changeFactor);
			// reset counter
			randomDecisions = 0;
		}
	}
	
	
	/**
	 * increases acceptableMatch with MAX_CHANGE and factor
	 */
	private void increase(double factor)
	{
		acceptableMatch += MAX_CHANGE * factor;
		if ((acceptableMatch > 1.00) || SumatraMath.isEqual(acceptableMatch, 1.00))
		{
			acceptableMatch = 0.99;
		}
		log.debug("Acceptable match is now increased to" + acceptableMatch);
	}
	
	
	/**
	 * decreases acceptableMatch with MAX_CHANGE and factor
	 */
	private void decrease(double factor)
	{
		acceptableMatch -= MAX_CHANGE * factor;
		if ((acceptableMatch < 0.0) || SumatraMath.isEqual(acceptableMatch, 0.00))
		{
			acceptableMatch = 0.01;
		}
		log.debug("Acceptable match is now decreased to " + acceptableMatch);
	}
	
	
	/**
	 * @param finishedPlays
	 */
	public void onFinishedPlays(List<APlay> finishedPlays)
	{
		for (APlay play : finishedPlays)
		{
			switch (play.getSelectionReason())
			{
				case SUCCESSFUL_EQUAL_MATCH:
				case SUCCESSFUL_FIRST_TRY:
				case SUCCESSFUL_MULTIPLE_TRIES:
					successDecisions++;
					randomDecisions = 0;
					log.trace("selection reasons in a row: success " + successDecisions);
					break;
				case RANDOM:
					successDecisions = 0;
					randomDecisions++;
					log.trace("selection reasons in a row: random " + randomDecisions);
					break;
				case UNKNOWN:
				default:
					break;
			}
		}
		calculateAccaptableMatch();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	public double getAcceptableMatch()
	{
		return acceptableMatch;
	}
	
	
	/**
	 * @param newAcceptableMatch
	 */
	public void setAcceptableMatch(double newAcceptableMatch)
	{
		acceptableMatch = newAcceptableMatch;
	}
}
