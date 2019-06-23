/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 30, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.defense.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedObject;


/**
 * TODO FelixB <bayer.fel@gmail.com>, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author FelixB <bayer.fel@gmail.com>
 */
@Persistent(version = 1)
public class IntersectionPoint extends Vector2
{
	/**  */
	public static final Comparator<? super IntersectionPoint>	DIST_TO_GOAL	= new Dist2GoalComparator();
	
	private final List<ITrackedObject>									passingBots;
	
	
	/**
	  * 
	  */
	public IntersectionPoint()
	{
		super();
		passingBots = new ArrayList<>(2);
	}
	
	
	/**
	 * @param point
	 * @param firstObject
	 * @param secondObject
	 */
	public IntersectionPoint(final IVector2 point, final ITrackedObject firstObject, final ITrackedObject secondObject)
	{
		super(point);
		passingBots = new ArrayList<>(2);
		passingBots.add(firstObject);
		passingBots.add(secondObject);
	}
	
	
	private static class Dist2GoalComparator implements Comparator<IntersectionPoint>
	{
		@Override
		public int compare(final IntersectionPoint point1, final IntersectionPoint point2)
		{
			IVector2 goalCenter = Geometry.getGoalOur().getGoalCenter();
			
			return (int) Math
					.signum(goalCenter.subtractNew(point2).getLength() - goalCenter.subtractNew(point1).getLength());
		}
	}
}
