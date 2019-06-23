/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 30, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ATrackedObject;


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
	
	private final List<ATrackedObject>	passingBots;
	
	
	/**
	  * 
	  */
	public IntersectionPoint()
	{
		super();
		passingBots = new ArrayList<ATrackedObject>(2);
	}
	
	
	/**
	 * @param point
	 * @param firstObject
	 * @param secondObject
	 */
	public IntersectionPoint(final IVector2 point, final ATrackedObject firstObject, final ATrackedObject secondObject)
	{
		super(point);
		passingBots = new ArrayList<ATrackedObject>(2);
		passingBots.add(firstObject);
		passingBots.add(secondObject);
	}
	
}
