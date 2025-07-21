/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botparams;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * For each label one bot parameter set can be selected and queried.
 */
@RequiredArgsConstructor
@Getter
public enum EBotParamLabel
{
	/**
	 * 2020 version TIGER
	 */
	TIGER_V2020("Parameters TIGER bots (Revision 2020)"),
	/**
	 * Opponent team
	 */
	OPPONENT("Parameters for opponent team"),
	/**
	 * Yellow team in simulation
	 */
	SIMULATION_YELLOW("Yellow team in the simulation"),
	/**
	 * Blue team in simulation
	 */
	SIMULATION_BLUE("Blue team in the simulation"),
	/**
	 * Yellow team as observed live
	 */
	YELLOW_LIVE("Yellow team for movement limit observer, should be set to opponent"),
	/**
	 * Blue team as observed live
	 */
	BLUE_LIVE("Blue team for movement limit observer, should be set to opponent"),

	;

	private final String label;
}
