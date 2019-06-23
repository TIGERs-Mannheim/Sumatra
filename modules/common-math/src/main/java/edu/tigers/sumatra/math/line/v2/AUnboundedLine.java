package edu.tigers.sumatra.math.line.v2;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Abstract implementation of the {@link IUnboundedLine} interface for all line types that can be defined through a
 * support vector and a direction vector.
 * 
 * @author Lukas Magel
 */
abstract class AUnboundedLine extends ALine implements IUnboundedLine
{
	private final IVector2	supportVector;
	private final IVector2	directionVector;
	
	private final boolean	isValid;
	
	
	AUnboundedLine(final IVector2 supportVector, final IVector2 directionVector)
	{
		this.supportVector = supportVector;
		this.directionVector = directionVector;
		
		isValid = !directionVector.isZeroVector();
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
		return isValid;
	}
}
