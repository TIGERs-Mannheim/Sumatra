/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Base for touch kick skills
 */
public abstract class ATouchKickSkill extends AMoveSkill
{
	protected final DynamicPosition target;
	protected final KickParams kickParams;
	
	
	protected ATouchKickSkill(final ESkill skill, final DynamicPosition target, final KickParams kickParams)
	{
		super(skill);
		this.target = target;
		this.kickParams = kickParams;
	}
}
