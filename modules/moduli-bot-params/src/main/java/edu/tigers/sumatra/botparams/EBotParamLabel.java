/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.botparams;

/**
 * For each label one bot parameter set can be selected and queried.
 *
 * @author AndreR <andre@ryll.cc>
 */
public enum EBotParamLabel
{
	/** Current 2016 version TIGER */
	TIGER_V2016("Parameters for TIGER bots (Revision 2016)"),
	/** Old 2013 version TIGER */
	TIGER_V2013("Parameters TIGER bots (Revision 2013)"),
	/** Opponent team */
	OPPONENT("Parameters for opponent team"),
	/** Yellow team in simulation */
	SIMULATION_YELLOW("Yellow team in the simulation"),
	/** Blue team in simulation */
	SIMULATION_BLUE("Blue team in the simulation"),
	/** Yellow team as observed live */
	YELLOW_LIVE("Yellow team for movement limit observer, should be set to opponent"),
	/** Blue team as observed live */
	BLUE_LIVE("Blue team for movement limit observer, should be set to opponent"),
	;

	private String label;

	EBotParamLabel(String label)
	{
		this.label = label;
	}

	public String getLabel()
	{
		return this.label;
	}
}
