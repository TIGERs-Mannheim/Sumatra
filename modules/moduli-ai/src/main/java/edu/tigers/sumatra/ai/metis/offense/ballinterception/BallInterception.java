/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.ballinterception;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.NonNull;
import lombok.Value;


@Value
public class BallInterception
{
	@NonNull
	BotID botID;
	double ballContactTime;
	@NonNull
	IVector2 pos;
}
