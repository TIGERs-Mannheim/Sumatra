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

import edu.dhbw.mannheim.tigers.sumatra.model.data.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
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
	
	private TrackedPosition	object;
	private float				radius;
	
	private Vector2			wish;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public ObjectPositionCrit(Vector2 wish, float penaltyFactor, ATrackedObject object, float radius)
	{
		super(ECriterion.OBJECT_POSITION, penaltyFactor);
		
		this.object = new TrackedPosition(object);
		this.radius = radius;
		this.wish = wish;
		this.penaltyFactor = normalizePenaltyFactor(penaltyFactor);
	}
	

	public ObjectPositionCrit(Vector2 wish, ATrackedObject object, float radius)
	{
		super(ECriterion.OBJECT_POSITION);
		
		this.object = new TrackedPosition(object);
		this.radius = radius;
		this.wish = wish;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public float doCheckCriterion(AIInfoFrame currentFrame)
	{
		object.update(currentFrame);
		
		if (AIMath.distancePP(object.getPos(), wish) < radius)
		{
			return 1.0f;
		} else
		{
			return penaltyFactor * 10 / (AIMath.distancePP(object.getPos(), wish) - radius);
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
