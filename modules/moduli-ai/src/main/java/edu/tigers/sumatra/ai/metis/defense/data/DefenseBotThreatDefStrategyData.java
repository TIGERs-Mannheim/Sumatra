/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense.data;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;


public record DefenseBotThreatDefStrategyData(
		BotID threatID,
		ILineSegment threatLine,
		ILineSegment protectionLine,
		IVector2 threatPos,
		IVector2 threatVel,
		IVector2 protectionPos,
		EDefenseBotThreatDefStrategy type
)
{
	public boolean isComplete()
	{
		return threatLine != null && protectionLine != null;
	}
}
