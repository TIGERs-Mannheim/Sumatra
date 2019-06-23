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
 * This interface is used to get the results of an comparison between current situation and a historic one. ALso it has
 * the possibility to returun the according PlaySet.<br />
 * Extends Comparable<IComparisonResult> for comparing IComparisonResult objects against each other and sorting them.
 * 
 * @author dirk
 * 
 */
public interface IComparisonResult extends Comparable<IComparisonResult>
{
	/**
	 * 
	 * returns a value how similar the compared fields are. <br />
	 * If other comparating factors exist, write a new IComparisonResult implementation.
	 * 
	 * @return
	 */
	double calcResult();
	
	
	/**
	 * Return the play that is selected by this comparison of the current situation with a historic situation.
	 * 
	 * @return
	 */
	PlayAndRoleCount getPlay();
	
	
	/**
	 * Set the play.
	 * 
	 * @param play
	 */
	void setPlay(PlayAndRoleCount play);
}
