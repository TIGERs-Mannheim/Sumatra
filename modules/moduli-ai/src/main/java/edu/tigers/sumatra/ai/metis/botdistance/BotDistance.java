/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.botdistance;

import edu.tigers.sumatra.ids.BotID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;


/**
 * Simple data holder which associates a {@link BotID} with a double-value
 */
@RequiredArgsConstructor
@Value
public class BotDistance
{
	/**
	 * Used to provide a not-null {@link BotDistance} even if no distances have been calculated
	 */
	public static final BotDistance NULL_BOT_DISTANCE = new BotDistance(BotID.noBot(), Double.MAX_VALUE);


	@NonNull
	BotID botId;
	double dist;
}
