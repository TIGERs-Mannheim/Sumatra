/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 28, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.event;

/**
 * This enumeration will track the available game events
 * 
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public enum EGameEvent
{
	/** Event for Ball possession of bots */
	BALL_POSSESSION,
	/** This event tracks tackles */
	TACKLE,
	/** Opponent bots are marking one of ours (is not related to MarkG in any way) */
	MARKING;
}
