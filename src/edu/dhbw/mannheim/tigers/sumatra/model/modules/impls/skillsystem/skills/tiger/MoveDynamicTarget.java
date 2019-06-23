/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.04.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * This skills move to the given {@link #dest} and always looks at the given object
 * 
 * @author Gero, DanielW
 */
public class MoveDynamicTarget extends AMoveSkillV2
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final float				POS_TOLERANCE	= AIConfig.getTolerances().getPositioning();
	
	/** The desired destination. */
	private Vector2f					dest;
	
	private final TrackedPosition	trackedPosition;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param dest
	 * @param object The object the bot should look at
	 */
	public MoveDynamicTarget(IVector2 dest, ATrackedObject object)
	{
		super(ESkillName.MOVE_DYNAMIC_TARGET);
		
		this.dest = new Vector2f(dest);
		this.trackedPosition = new TrackedPosition(object);
	}
	

	/**
	 * @param dest
	 * @param objectId The id identifying the object the bot should look at
	 */
	public MoveDynamicTarget(IVector2 dest, int objectId)
	{
		super(ESkillName.MOVE_DYNAMIC_TARGET);
		
		this.dest = new Vector2f(dest);
		this.trackedPosition = new TrackedPosition(objectId);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected float calcTargetOrientation(IVector2 move)
	{
		trackedPosition.update(getWorldFrame());
		if (trackedPosition.posUpdated())
		{
			// Calc difference
			return AIMath.angleBetweenXAxisAndLine(getBot().pos, trackedPosition.getPos());
		} else
		{
			// Target was not found, simply keep the current angle
			return getBot().angle;
		}
	}
	

	@Override
	protected IVector2 getTarget()
	{
		return dest;
	}
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		MoveDynamicTarget move = (MoveDynamicTarget) newSkill;
		return dest.equals(move.dest, POS_TOLERANCE) && trackedPosition.getObjId() == move.trackedPosition.getObjId();
	}
}
