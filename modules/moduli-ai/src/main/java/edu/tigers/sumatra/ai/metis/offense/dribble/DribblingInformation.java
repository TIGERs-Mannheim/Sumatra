/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.dribble;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.AllArgsConstructor;
import lombok.Value;


@Value
@AllArgsConstructor
public class DribblingInformation
{
	IVector2 startPos;
	boolean dribblingInProgress;
	BotID dribblingBot;
	ICircle dribblingCircle;
	IVector2 intersectionPoint;
	boolean violationImminent;


	public static DribblingInformation update(DribblingInformation info, IVector2 intersection, boolean violationImminent)
	{
		return new DribblingInformation(info.startPos, info.dribblingInProgress, info.dribblingBot, info.dribblingCircle,
				intersection, violationImminent);
	}
}
