/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s):
 * Bernhard
 * Gunther
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import java.io.Serializable;


/**
 * Simple data holder for {@link ERefereeCommand}s
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 */
public class RefereeMsg implements Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long		serialVersionUID	= 3539987723856079252L;
	
	/**  */
	public final int					id;
	
	/**  */
	public final ERefereeCommand	cmd;
	
	/**  */
	public final int					goalsTigers;
	
	/**  */
	public final int					goalsEnemies;
	
	/**  */
	public final short				timeRemaining;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param id
	 * @param command
	 * @param goalsTigers
	 * @param goalsEnemies
	 * @param timeRemaining
	 */
	public RefereeMsg(int id, ERefereeCommand command, int goalsTigers, int goalsEnemies, short timeRemaining)
	{
		this.id = id;
		this.cmd = command;
		this.goalsTigers = goalsTigers;
		this.goalsEnemies = goalsEnemies;
		this.timeRemaining = timeRemaining;
	}
	

	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder();
		b.append("[RefereeMsg|");
		b.append("id=" + id + "|");
		b.append("cmd=" + cmd + "|");
		b.append("goalsTigers=" + goalsTigers + "|");
		b.append("goalsEnemies=" + goalsEnemies + "|");
		b.append("timeRemaining=" + timeRemaining + "]");
		return b.toString();
	}
}
