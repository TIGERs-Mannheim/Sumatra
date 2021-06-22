/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public enum EFontSize
{
	SMALL(0.8),
	MEDIUM(1.0),
	LARGE(1.2),

	;

	private final double scaleFactor;
}
