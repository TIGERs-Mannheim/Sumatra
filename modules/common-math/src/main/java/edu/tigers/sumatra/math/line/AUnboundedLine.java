/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.line;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * Abstract implementation for all line types that can be defined through a
 * support vector and a direction vector.
 * 
 * @author Lukas Magel
 */
abstract class AUnboundedLine extends ALine
{
	private final Vector2f supportVector;
	private final Vector2f directionVector;
	
	private final boolean valid;
	
	
	AUnboundedLine(final IVector2 supportVector, final IVector2 directionVector)
	{
		this.supportVector = Vector2f.copy(supportVector);
		this.directionVector = Vector2f.copy(directionVector);

		valid = !directionVector.isZeroVector();
	}
	
	
	@Override
	public IVector2 supportVector()
	{
		return supportVector;
	}
	
	
	@Override
	public IVector2 directionVector()
	{
		return directionVector;
	}
	
	
	@Override
	public boolean isValid()
	{
		return valid;
	}
}
