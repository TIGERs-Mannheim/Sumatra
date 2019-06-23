/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line.v2;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * This class represents a base interface for {@code ILine} and {@code IHalfLine} and groups common operations.
 * 
 * @author Lukas Magel
 */
interface IUnboundedLine extends ILineBase
{
	
	/**
	 * Returns the support vector of this line. The support vector represents the starting point of the line, i.e. where
	 * it is anchored.
	 *
	 * @return
	 * 			The support vector which may have a length of zero
	 */
	IVector2 supportVector();
	
}
