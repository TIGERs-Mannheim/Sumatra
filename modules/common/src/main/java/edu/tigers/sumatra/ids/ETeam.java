/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.04.2011
 * Author(s):
 * FlorianS
 * *********************************************************
 */
package edu.tigers.sumatra.ids;

/**
 * Enum which describes the team
 */
public enum ETeam
{
	TIGERS,
	OPPONENTS,
	UNKNOWN,
	BOTH,
	;


	/**
	 * Asserts that the given team is one of {@link #TIGERS} or {@link #OPPONENTS}, throws an
	 * {@link IllegalArgumentException} otherwise!
	 *
	 * @param team
	 */
	public static void assertOneTeam(final ETeam team)
	{
		if ((team == TIGERS) || (team == OPPONENTS))
		{
			// okay!
			return;
		}
		final String teamStr = team == null ? "<null>" : team.name();
		throw new IllegalArgumentException(teamStr + " is not valid team identifier!!!");
	}


	public static ETeam[] both()
	{
		return new ETeam[] { TIGERS, OPPONENTS };
	}
}
