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
	TIGER_V2016,
	/** Old 2013 version TIGER */
	TIGER_V2013,
	/** Opponent team */
	OPPONENT,
	/** Yellow team in simulation */
	SIMULATION_YELLOW,
	/** Blue team in simulation */
	SIMULATION_BLUE,
	/** Yellow team as observed live */
	YELLOW_LIVE,
	/** Blue team as observed live */
	BLUE_LIVE,
}
