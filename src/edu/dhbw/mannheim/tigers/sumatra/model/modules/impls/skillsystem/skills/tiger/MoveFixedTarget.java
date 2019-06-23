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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * This skills moves to the desired position {@link #dest} while looking at {@link #lookAtTarget}
 * 
 * @author Gero, DanielW
 */
public class MoveFixedTarget extends AMoveSkillV2
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final float	POS_TOLERANCE	= AIConfig.getTolerances().getPositioning();
	
	/** The desired destination. */
	private Vector2f		dest;
	
	/** The target the bot should look at while moving. */
	private Vector2f		lookAtTarget;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param dest <code>null</code> means "current position"
	 * @param lookAtTarget
	 */
	public MoveFixedTarget(IVector2 dest, IVector2 lookAtTarget)
	{
		super(ESkillName.MOVE_FIXED_TARGET);
		
		if (dest == null)
		{
			this.dest = null;
		} else
		{
			this.dest = new Vector2f(dest);
		}
		this.lookAtTarget = new Vector2f(lookAtTarget);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected float calcTargetOrientation(IVector2 move)
	{
		return AIMath.angleBetweenXAxisAndLine(getBot().pos, lookAtTarget);
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
		MoveFixedTarget move = (MoveFixedTarget) newSkill;
		boolean destEqual = false;
		if (dest != null && move.dest != null)
		{
			destEqual = dest.equals(move.dest, POS_TOLERANCE);
		} else
		{
			destEqual = dest == null && move.dest == null;
		}
		return destEqual && lookAtTarget.equals(move.lookAtTarget);
	}
}
