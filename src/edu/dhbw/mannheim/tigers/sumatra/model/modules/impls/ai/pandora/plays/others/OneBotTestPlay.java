/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.08.2010
 * Author(s):
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other.AimingTest;


/**
 * Temporary play for testing several situations wit only one bot.
 * Since these can differ, no more specific information are listed here.
 * 
 * Use it whenever you want test something quickly.
 * 
 * @author Some AI-guy
 * 
 */
public class OneBotTestPlay extends APlay
{
	
	/**  */
	private static final long	serialVersionUID	= -588907929412642318L;
	private ARole					testRole;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// allowed because this is a Pseudo-Play
	public OneBotTestPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.ONE_BOT_TEST, aiFrame);
		
		testRole = new AimingTest();
		addAggressiveRole(testRole);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public int calcPlayableScore(AIInfoFrame currentFrame)
	{
		// ususally scoring is not necessary for this testplay
		return 0;
	}
	
}
