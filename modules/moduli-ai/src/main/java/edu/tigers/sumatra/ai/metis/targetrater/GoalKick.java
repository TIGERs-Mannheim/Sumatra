/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import lombok.Value;
import lombok.experimental.Accessors;


@Value
public class GoalKick
{
	KickOrigin kickOrigin;
	IRatedTarget ratedTarget;
	Kick kick;
	@Accessors(fluent = true)
	boolean canBeRedirected;
}
