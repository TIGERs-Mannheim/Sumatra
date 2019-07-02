package edu.tigers.sumatra.pathfinder.obstacles;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.drawable.IDrawableShape;


/**
 * Base class for obstacles.
 */
@Persistent
public abstract class AObstacle implements IObstacle
{
	private transient boolean critical = false;
	protected transient List<IDrawableShape> shapes;


	protected abstract void initializeShapes();


	@Override
	public final List<IDrawableShape> getShapes()
	{
		if (shapes == null)
		{
			shapes = new ArrayList<>();
			initializeShapes();
		}
		return shapes;
	}


	@Override
	public final boolean isCritical()
	{
		return critical;
	}


	public void setCritical(final boolean critical)
	{
		this.critical = critical;
	}
}
