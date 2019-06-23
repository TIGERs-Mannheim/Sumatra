/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 6, 2012
 * Author(s): dirk
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.PlayAndRoleCount;


/**
 * Simple IComparisonResult implementation. Reperesents the comparisonresult of two fields by a value between 0 and 1.
 * @author dirk
 * 
 */
public class ComparisonResult implements IComparisonResult
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** play for this result and the number of roles used */
	private PlayAndRoleCount	play;
	/** Comparison Result between 0 and 1 */
	private final double			compResult;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param result
	 */
	public ComparisonResult(double result)
	{
		compResult = result;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * returns a value how similar the compared fields are
	 * 
	 * @return
	 */
	@Override
	public double calcResult()
	{
		return compResult;
	}
	
	
	/**
	 * Compares to Comparisonresults based on its reuslt calculation. <br />
	 * Used for sorting PlaySets based on its Comparisonreuslt.
	 * @param o Object for comparison.
	 * @return
	 */
	@Override
	public int compareTo(IComparisonResult o)
	{
		if (calcResult() < o.calcResult())
		{
			return -1;
		} else if (calcResult() > o.calcResult())
		{
			return 1;
		} else
		{
			return 0;
		}
	}
	
	
	@Override
	public String toString()
	{
		return "ComparisonResult [play=" + play + ", result=" + compResult + "]";
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((play == null) ? 0 : play.hashCode());
		long temp;
		temp = Double.doubleToLongBits(compResult);
		return (prime * result) + (int) (temp ^ (temp >>> 32));
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		ComparisonResult other = (ComparisonResult) obj;
		if (play == null)
		{
			if (other.play != null)
			{
				return false;
			}
		} else if (!play.equals(other.play))
		{
			return false;
		}
		if (Double.doubleToLongBits(compResult) != Double.doubleToLongBits(other.compResult))
		{
			return false;
		}
		return true;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the play
	 */
	@Override
	public PlayAndRoleCount getPlay()
	{
		return play;
	}
	
	
	/**
	 * @param play the play to set
	 */
	@Override
	public void setPlay(PlayAndRoleCount play)
	{
		this.play = play;
	}
	
}
