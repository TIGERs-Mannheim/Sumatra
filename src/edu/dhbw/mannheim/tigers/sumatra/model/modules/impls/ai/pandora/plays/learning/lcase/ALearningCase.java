/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 3, 2015
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.learning.lcase;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;


/**
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public abstract class ALearningCase implements ILearningCase
{
	protected List<ERole>	activeRoleTypes	= new ArrayList<ERole>();
	protected boolean			active				= false;
	
	
	@Override
	public boolean isActive(final AthenaAiFrame frame)
	{
		return active;
	}
	
	
	/**
	 * @return the activeRoleTypes
	 */
	public List<ERole> getActiveRoleTypes()
	{
		return activeRoleTypes;
	}
	
	
	/**
	 * @param activeRoleTypes the activeRoleTypes to set
	 */
	public void setActiveRoleTypes(final List<ERole> activeRoleTypes)
	{
		this.activeRoleTypes = activeRoleTypes;
	}
	
	
	/**
	 * @param active the active to set
	 */
	public void setActive(final boolean active)
	{
		this.active = active;
	}
}
