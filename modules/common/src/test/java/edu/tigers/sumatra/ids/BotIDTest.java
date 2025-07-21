/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 15, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ids;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


/**
 * Tests the {@link BotID} class
 */
class BotIDTest
{
	@Test
	void testEquals()
	{
		BotID bot0blue = BotID.createBotId(0, ETeamColor.BLUE);
		BotID bot0blueTigers = BotID.createBotId(0, ETeamColor.BLUE);
		BotID bot0Yellow = BotID.createBotId(0, ETeamColor.YELLOW);
		BotID bot1blue = BotID.createBotId(1, ETeamColor.BLUE);
		BotID bot2blueTigers = BotID.createBotId(2, ETeamColor.BLUE);
		BotID bot2blueOpponents = BotID.createBotId(2, ETeamColor.BLUE);
		BotID bot3blueTigers = BotID.createBotId(3, ETeamColor.BLUE);
		BotID bot3yellowTigers = BotID.createBotId(3, ETeamColor.YELLOW);

		assertEquals(bot0blue, bot0blue);
		assertEquals(bot0blueTigers, bot0blueTigers);
		assertEquals(bot0Yellow, bot0Yellow);
		assertEquals(bot1blue, bot1blue);
		assertEquals(bot2blueTigers, bot2blueTigers);
		assertEquals(bot2blueOpponents, bot2blueOpponents);
		assertEquals(bot3blueTigers, bot3blueTigers);
		assertEquals(bot3yellowTigers, bot3yellowTigers);

		assertEquals(bot2blueTigers, bot2blueOpponents);
		assertEquals(bot0blueTigers, bot0blue);
		assertEquals(bot0blue, bot0blueTigers);

		assertNotEquals(bot0blue, bot0Yellow);
		assertNotEquals(bot0blue, bot1blue);
		assertNotEquals(bot0blue, bot2blueTigers);
		assertNotEquals(bot1blue, bot2blueTigers);
		assertNotEquals(bot2blueOpponents, bot3blueTigers);
		assertNotEquals(bot2blueOpponents, bot3yellowTigers);
		assertNotEquals(bot3blueTigers, bot3yellowTigers);
	}
}
