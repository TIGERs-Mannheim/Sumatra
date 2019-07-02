/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.log;

import java.awt.Color;
import java.time.Instant;


public abstract class GameLogEntry
{
	/** frame timestamp in nanoseconds */
	private final long timestamp;
	/** time of the game this entry was created at */
	private final GameTime gameTime;
	/** The time instant this entry was created in */
	private final Instant instant;
	
	private final ELogEntryType type;
	
	
	protected GameLogEntry(final ELogEntryType type, final long timestamp, final GameTime gameTime)
	{
		this.timestamp = timestamp;
		this.gameTime = gameTime;
		this.instant = Instant.now();
		this.type = type;
	}
	
	
	public abstract String workGameLogEntry();
	
	
	public abstract String getToolTipText();
	
	
	public Color getForegroundColor()
	{
		return type.getColor();
	}
	
	
	public ELogEntryType getType()
	{
		return type;
	}
	
	
	/**
	 * The timestamp of the frame this entry was reported in
	 * 
	 * @return timestamp in nanoseconds
	 */
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * @return the gameTime until the end of the stage
	 */
	public GameTime getGameTime()
	{
		return gameTime;
	}
	
	
	/**
	 * @return the instant this instance was created in. The value is in UTC
	 */
	public Instant getInstant()
	{
		return instant;
	}
}
