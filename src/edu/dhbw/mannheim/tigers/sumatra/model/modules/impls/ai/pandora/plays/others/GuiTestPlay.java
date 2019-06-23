/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.11.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.RoleFactory;


/**
 * This play is the role-container for any {@link ARole} selected by the GUI
 * 
 * @author Gero
 * 
 */
public class GuiTestPlay extends APlay
{
	/**  */
	private static final long	serialVersionUID	= -1726001125154831729L;
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final RoleFactory factory = RoleFactory.getInstance();
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Do not use this ctor. It is only used to instantiate this play for scoring calc.
	 * (feature was requested by malte)
	 */
	public GuiTestPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.GUI_TEST_PLAY, aiFrame);
		// do nothing
	}
	
	
	// allowed because this is a Pseudo-Play
	public GuiTestPlay(AIInfoFrame aiFrame, List<ERole> activeRoles)
	{
		super(EPlay.GUI_TEST_PLAY, aiFrame);
		

		for (ERole type : activeRoles)
		{
			try
			{
				ARole genRole = factory.createRole(type);
				addAggressiveRole(genRole);
				
			} catch (IllegalArgumentException iae)
			{
				log.warn(iae.getMessage());
			}
		}
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
 
	@Override
	public int calcPlayableScore(AIInfoFrame currentFrame)
	{
		return 0;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
