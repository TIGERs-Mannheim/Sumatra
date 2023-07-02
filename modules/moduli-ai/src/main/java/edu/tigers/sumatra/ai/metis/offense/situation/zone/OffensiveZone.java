/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.situation.zone;

import edu.tigers.sumatra.math.rectangle.IRectangle;
import lombok.Value;


@Value
public class OffensiveZone
{
	EOffensiveZone zoneName;
	IRectangle rect;
}
