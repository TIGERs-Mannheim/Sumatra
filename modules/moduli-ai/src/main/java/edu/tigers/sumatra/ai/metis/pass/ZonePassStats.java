/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import lombok.Data;


@Data
public class ZonePassStats
{
	private int totalPasses = 0;
	private int successfulPasses = 0;

	public ZonePassStats()
	{

	}

	public ZonePassStats(int totalPasses, int successfulPasses)
	{
		this.totalPasses = totalPasses;
		this.successfulPasses = successfulPasses;
	}
}
