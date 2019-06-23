/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.03.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * Defines a movement with a static global {@link #destOrientation} throughout the movement (thus 'fixed')<br/>
 * @see #MoveStaticRotation(IVector2, float)
 * 
 * @author Gero, DanielW
 */
public class MoveFixedGlobalOrientation extends AMoveSkillV2
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final float	POS_TOLERANCE	= AIConfig.getTolerances().getPositioning();
	private final float	ANG_TOLERANCE	= AIConfig.getTolerances().getViewAngle();
	
	/** The desired destination. */
	private Vector2f		dest;
	
	/** The desired orientation at the destination (will be tried reached as fast as possible). */
	private float			destOrientation;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * The bot will move to dest and try to reach targetOrientation as fast as possible
	 * 
	 * @param dest <code>null</code> means "current position" (for rotation only
	 * @param targetOrientation
	 */
	public MoveFixedGlobalOrientation(IVector2 dest, float targetOrientation)
	{
		super(ESkillName.MOVE_FIXED_GLOBAL_ORIENTATION);
		
		if (dest == null)
		{
			this.dest = null;
		} else
		{
			this.dest = new Vector2f(dest);
		}
		this.destOrientation = targetOrientation;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected float calcTargetOrientation(IVector2 move)
	{
		return destOrientation;
	}
	

	@Override
	protected IVector2 getTarget()
	{
		if (dest == null)
		{
			return getBot().pos;
		} else
		{
			return dest;
		}
	}
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		MoveFixedGlobalOrientation move = (MoveFixedGlobalOrientation) newSkill;
		boolean destEqual = false;
		if (dest != null && move.dest != null)
		{
			destEqual = dest.equals(move.dest, POS_TOLERANCE);
		} else
		{
			destEqual = dest == null && move.dest == null;
		}
		return destEqual && AIMath.isZero(destOrientation - move.destOrientation, ANG_TOLERANCE);
	}
}
