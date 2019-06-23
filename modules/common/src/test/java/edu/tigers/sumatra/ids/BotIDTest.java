/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 15, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ids;

import org.junit.Assert;
import org.junit.Test;


/**
 * Tests the {@link BotID} class
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotIDTest
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	@Test
	public void testEquals()
	{
		BotID bot0blue = BotID.createBotId(0, ETeamColor.BLUE);
		BotID bot0blueTigers = BotID.createBotId(0, ETeamColor.BLUE);
		BotID bot0Yellow = BotID.createBotId(0, ETeamColor.YELLOW);
		BotID bot1blue = BotID.createBotId(1, ETeamColor.BLUE);
		BotID bot2blueTigers = BotID.createBotId(2, ETeamColor.BLUE);
		BotID bot2blueOpponents = BotID.createBotId(2, ETeamColor.BLUE);
		BotID bot3blueTigers = BotID.createBotId(3, ETeamColor.BLUE);
		BotID bot3yellowTigers = BotID.createBotId(3, ETeamColor.YELLOW);
		
		Assert.assertTrue(bot0blue.equals(bot0blue));
		Assert.assertTrue(bot0blueTigers.equals(bot0blueTigers));
		Assert.assertTrue(bot0Yellow.equals(bot0Yellow));
		Assert.assertTrue(bot1blue.equals(bot1blue));
		Assert.assertTrue(bot2blueTigers.equals(bot2blueTigers));
		Assert.assertTrue(bot2blueOpponents.equals(bot2blueOpponents));
		Assert.assertTrue(bot3blueTigers.equals(bot3blueTigers));
		Assert.assertTrue(bot3yellowTigers.equals(bot3yellowTigers));
		
		Assert.assertTrue(bot2blueTigers.equals(bot2blueOpponents));
		Assert.assertTrue(bot0blueTigers.equals(bot0blue));
		Assert.assertTrue(bot0blue.equals(bot0blueTigers));
		
		Assert.assertFalse(bot0blue.equals(bot0Yellow));
		Assert.assertFalse(bot0blue.equals(bot1blue));
		Assert.assertFalse(bot0blue.equals(bot2blueTigers));
		Assert.assertFalse(bot1blue.equals(bot2blueTigers));
		Assert.assertFalse(bot2blueOpponents.equals(bot3blueTigers));
		Assert.assertFalse(bot2blueOpponents.equals(bot3yellowTigers));
		Assert.assertFalse(bot3blueTigers.equals(bot3yellowTigers));
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
