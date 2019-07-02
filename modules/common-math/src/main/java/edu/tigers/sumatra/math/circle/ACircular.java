package edu.tigers.sumatra.math.circle;

import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.v2.ILineBase;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Implementations for circular shapes
 */
@Persistent
public abstract class ACircular implements ICircular
{
	@Override
	public final List<IVector2> lineIntersections(final ILine line)
	{
		return CircleMath.lineIntersections(this, line.v2());
	}
	
	
	@Override
	public final List<IVector2> lineIntersections(ILineBase line)
	{
		return CircleMath.lineIntersections(this, line);
	}
	
	
	@Override
	public final IVector2 nearestPointOutside(final IVector2 point)
	{
		return CircleMath.nearestPointOutsideCircle(this, point);
	}
}
