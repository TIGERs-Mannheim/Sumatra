/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.behaviors;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.ASupportBehavior;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;


public class TestBehavior extends ASupportBehavior
{
	
	public TestBehavior(final ARole role)
	{
		super(role);
	}
	
	
	@Override
	public double calculateViability()
	{
		return 1;
	}
	
	
	@Override
	public void doEntryActions()
	{
		AMoveToSkill skill = AMoveToSkill.createMoveToSkill();
		getRole().setNewSkill(skill);
		getRole().getCurrentSkill().getMoveCon().updateDestination(Geometry.getCenter());
		
	}

	@Override
	public boolean getIsActive()
	{
		return false;
	}
	
	
	@Override
	public void doUpdate()
	{
		// Do nothing
	}
}
