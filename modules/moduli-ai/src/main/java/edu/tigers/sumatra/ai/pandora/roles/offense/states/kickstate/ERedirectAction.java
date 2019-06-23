/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.states.kickstate;

/**
 * Stores redirect aciton which defines what the redirectKickState should do
 */
public enum ERedirectAction
{
	/**
	 * Redirector should peform a catch
	 */
	CATCH,
	/**
	 * Redirector should shot on goal
	 */
	REDIRECT_GOAL,
	/**
	 * Redirector should do a double pass
	 */
	REDIRECT_PASS,
	/**
	 * Opponent tries to catch ball before us. Dont let him !
	 */
	INTERCEPT
}
