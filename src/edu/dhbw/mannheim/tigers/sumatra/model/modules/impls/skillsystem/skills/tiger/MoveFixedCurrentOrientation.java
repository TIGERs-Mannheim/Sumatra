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

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * This skills moves the bot to the given {@link #dest} and keeps the current global orientation.
 * 
 * @see #MoveFixedCurrentOrientation(IVector2)
 * 
 * @author Gero, DanielW
 */
public class MoveFixedCurrentOrientation extends AMoveSkillV2
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final float	POS_TOLERANCE	= AIConfig.getTolerances().getPositioning();
	
	/** The desired destination. */
	private Vector2f		dest;
	
	/** The desired orientation at the destination (will be tried reached as fast as possible). */
	private Float			destOrientation;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param
	 */
	public MoveFixedCurrentOrientation(IVector2 dest)
	{
		super(ESkillName.MOVE_FIXED_CURRENT_ORIENTATION);
		
		this.dest = new Vector2f(dest);
		this.destOrientation = null;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected float calcTargetOrientation(IVector2 move)
	{
		if (destOrientation == null)
		{
			destOrientation = getBot().angle;
		}
		
		return destOrientation;
	}
	

	@Override
	protected IVector2 getTarget()
	{
		return dest;
	}
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		// Does NOT include #destOrientation, as this is just a support-variable between multiple calls to
		// calcTargetOrientation, not a part of the state (or intention) of this object
		MoveFixedCurrentOrientation move = (MoveFixedCurrentOrientation) newSkill;
		return dest.equals(move.dest, POS_TOLERANCE);
	}
}
