/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.skirmish;

import lombok.Getter;


public enum ESkirmishCategory
{
	NO_SKIRMISH(false),
	OPPONENT_HAS_CONTROL(false),
	CONTESTED_CONTROL(false),
	WE_HAVE_CONTROL(false),
	PENDING_OPPONENT_HAS_CONTROL(true),
	PENDING_CONTESTED_CONTROL(true),
	PENDING_WE_HAVE_CONTROL(true);

	@Getter
	private final boolean pending; // If true: the skirmish category is currently not happening, but it is likely to happen in the next seconds


	ESkirmishCategory(boolean isPending)
	{
		this.pending = isPending;
	}
}
