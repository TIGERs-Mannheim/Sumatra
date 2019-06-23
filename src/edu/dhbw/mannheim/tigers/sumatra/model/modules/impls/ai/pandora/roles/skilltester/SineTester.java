/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.05.2011
 * Author(s): Oliver Steinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.skilltester;

import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;


/**
 * Sine skill tester.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class SineTester extends ASkillTester
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= 5293650820528590662L;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	public SineTester()
	{
		super(ERole.SINE_TESTER, "sine", 2000);
		exporter.setHeader("time", "v");
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	@Override
	protected void doPrepare(WorldFrame wFrame)
	{
		// do nothing
	}
	

	@Override
	protected void doStart(WorldFrame wFrame, SkillFacade skills)
	{
		skills.sinus(60000);
	}
	

	@Override
	protected void doMeasure(WorldFrame wFrame)
	{
		exporter.addValues(System.nanoTime(), wFrame.tigerBots.get(getBotID()).vel.getLength2());
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
