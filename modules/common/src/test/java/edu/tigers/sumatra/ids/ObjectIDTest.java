/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ids;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


/**
 * This is a unit test for {@link AObjectID} and subtract classes.
 */
class ObjectIDTest
{
	@Test
	void testID()
	{
		final BotID botId = BotID.createBotId(1, ETeamColor.YELLOW);
		
		BotID botId2 = botId;
		assertEquals(botId, botId2);
		
		botId2 = BotID.createBotId(1, ETeamColor.YELLOW);
		assertEquals(botId, botId2);
		
		botId2 = BotID.createBotId(2, ETeamColor.YELLOW);
		assertNotEquals(botId, botId2);

		assertNotEquals(null, botId);
		assertNotEquals(this, botId);
	}
}
