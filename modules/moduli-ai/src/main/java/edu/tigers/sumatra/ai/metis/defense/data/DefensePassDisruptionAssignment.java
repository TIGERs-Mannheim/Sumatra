/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense.data;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.NonNull;
import lombok.Value;


@Value
public class DefensePassDisruptionAssignment implements Comparable<DefensePassDisruptionAssignment>
{
	@NonNull
	BotID threatId;
	@NonNull
	BotID defenderId;
	@NonNull
	IVector2 interceptionPoint;


	@Override
	public int compareTo(DefensePassDisruptionAssignment o)
	{
		var def = defenderId.compareTo(o.getDefenderId());
		if (def != 0)
		{
			return def;
		}
		return threatId.compareTo(o.getThreatId());
	}
}
