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
 * This skills moves to the desired {@link #dest} and always looks in the direction of movement
 * 
 * @author Gero, DanielW
 */
public class MoveAhead extends AMoveSkillV2
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final float	POS_TOLERANCE	= AIConfig.getTolerances().getPositioning();
	
	/** The desired destination. */
	private Vector2f		dest;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param dest
	 */
	public MoveAhead(IVector2 dest)
	{
		super(ESkillName.MOVE_AHEAD);
		
		this.dest = new Vector2f(dest);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected float calcTargetOrientation(IVector2 move)
	{
		
		if (move.isZeroVector())
		{
			return getBot().angle;
		}
		
		return move.getAngle();
	}
	

	@Override
	protected IVector2 getTarget()
	{
		return dest;
	}
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		MoveAhead move = (MoveAhead) newSkill;
		return dest.equals(move.dest, POS_TOLERANCE);
	}
}
