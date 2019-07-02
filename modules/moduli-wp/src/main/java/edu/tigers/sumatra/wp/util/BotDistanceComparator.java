package edu.tigers.sumatra.wp.util;

import java.util.Comparator;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorDistanceComparator;
import edu.tigers.sumatra.wp.data.ITrackedBot;


public class BotDistanceComparator implements Comparator<ITrackedBot>
{
	private VectorDistanceComparator comparator;
	
	
	public BotDistanceComparator(final IVector2 pos)
	{
		comparator = new VectorDistanceComparator(pos);
	}
	
	
	@Override
	public int compare(final ITrackedBot o1, final ITrackedBot o2)
	{
		return comparator.compare(o1.getPos(), o2.getPos());
	}
}
