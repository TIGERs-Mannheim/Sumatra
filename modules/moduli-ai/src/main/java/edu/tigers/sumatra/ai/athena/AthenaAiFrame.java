/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.TacticalField;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Delegate;


/**
 * Ai frame for athena data, based on {@link MetisAiFrame}
 */
@Value
@Builder
public class AthenaAiFrame
{
	@Delegate
	BaseAiFrame baseAiFrame;
	MetisAiFrame metisAiFrame;
	IPlayStrategy playStrategy;


	public TacticalField getTacticalField()
	{
		return metisAiFrame.getTacticalField();
	}
}
