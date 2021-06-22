/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.Color;


@Getter
@AllArgsConstructor
public enum ESkillState
{
	IN_PROGRESS(Color.cyan),
	SUCCESS(Color.green),
	FAILURE(Color.red),

	;
	private final Color color;
}
