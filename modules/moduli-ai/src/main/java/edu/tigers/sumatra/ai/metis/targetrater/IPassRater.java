/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Ulrike Leipscher <ulrike.leipscher@dlr.de>.
 */
public interface IPassRater
{
	double rateStraightPass(IVector2 passOrigin, IVector2 passTarget);
	
	
	double rateChippedPass(IVector2 passOrigin, IVector2 passTarget, double maxChipSpeed);
}
