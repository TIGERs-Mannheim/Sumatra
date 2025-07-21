/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * Specifies the bots position
 * Take care the order of this Enum, it may be used for sorting.
 */
@RequiredArgsConstructor
@Getter
public enum CoverMode implements Comparable<CoverMode>
{
	RIGHT_2_5(-2.5),
	RIGHT_2(-2),
	RIGHT_1_5(-1.5),
	RIGHT_1(-1),
	RIGHT_0_5(-0.5), // Halfway between RIGHT_1 and CENTER
	CENTER(0),
	LEFT_0_5(0.5), // Halfway between LEFT_1 and CENTER
	LEFT_1(1),
	LEFT_1_5(1.5),
	LEFT_2(2),
	LEFT_2_5(2.5),

	;

	private final double distanceFactor;
}