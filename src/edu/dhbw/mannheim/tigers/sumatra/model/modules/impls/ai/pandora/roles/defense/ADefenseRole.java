/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.11.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.LookAtCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * Abstract class for (most) defense roles.
 * It implements the methods: {@link ARole#calculateSkills calculateSkills} and {@link ARole#getDestination
 * getDestination}.
 * Superclass: {@link ABaseRole}
 * 
 * @author Malte
 * 
 */
public abstract class ADefenseRole extends ABaseRole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -9180754288107911528L;
	

	protected final LookAtCon	lookAtCon;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public ADefenseRole(ERole type)
	{
		super(type);
		
		lookAtCon = new LookAtCon();
		addCondition(lookAtCon);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		
		boolean dest = destCon.checkCondition(wFrame);
		boolean angl = lookAtCon.checkCondition(wFrame);
		
		// conditions completed?
		if (!dest || !angl)
		{
			skills.moveTo(destCon.getDestination(), lookAtCon.getLookAtTarget());
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
