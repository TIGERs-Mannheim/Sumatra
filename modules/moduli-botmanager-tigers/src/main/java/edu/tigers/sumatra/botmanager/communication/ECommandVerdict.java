/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.communication;

/**
 * A command verdict is used to indicate (in a chain of command procesing units) if a command
 * should be processed further (PASS) by the next unit or not (DROP).
 */
public enum ECommandVerdict
{
	PASS,
	DROP,
}
