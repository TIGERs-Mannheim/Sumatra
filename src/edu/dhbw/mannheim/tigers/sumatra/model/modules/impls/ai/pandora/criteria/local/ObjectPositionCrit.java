/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.03.2011
 * Author(s):
 * FlorianS
 * ChristianK
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.local;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACriterion;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.ECriterion;


/**
 * This class determines whether a TrackedObject is at or close to desired
 * point on the field. If this is the case the value 1 will be returned.
 * Otherwise the parameter 'penaltyFactor' will be decreased by every cm
 * outside the radius.
 * 
 * @author FlorianS
 * 
 */
public class ObjectPositionCrit extends ACriterion
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final TrackedPosition	object;
	private final float				radius;
	
	private final Vector2			destination;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Create criterion that checks if trackedObject is at destination
	 * 
	 * @param destination
	 * @param object
	 * @param radius
	 */
	public ObjectPositionCrit(Vector2 destination, ATrackedObject object, float radius)
	{
		super(ECriterion.OBJECT_POSITION);
		
		this.object = new TrackedPosition(object);
		this.radius = radius;
		this.destination = destination;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public float doCheckCriterion(AIInfoFrame currentFrame)
	{
		object.update(currentFrame);
		
		if (GeoMath.distancePP(object.getPos(), destination) < radius)
		{
			return MAX_SCORE;
		}
		return 10 / (GeoMath.distancePP(object.getPos(), destination) - radius);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
