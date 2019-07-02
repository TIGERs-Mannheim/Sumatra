/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.07.2016
 * Author(s): kisle
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.botskills.data;


/**
 * Data structure for controlling the multimedia functions of our robots
 */
public class MultimediaControl
{
	private ELedColor ledColor = ELedColor.OFF;
	private ESong song = ESong.NONE;
	
	
	public ELedColor getLedColor()
	{
		return ledColor;
	}
	
	
	public MultimediaControl setLedColor(final ELedColor ledColor)
	{
		this.ledColor = ledColor;
		return this;
	}
	
	
	/**
	 * @return the song
	 */
	public final ESong getSong()
	{
		return song;
	}
	
	
	/**
	 * @param song the song to set
	 * @return this
	 */
	public final MultimediaControl setSong(final ESong song)
	{
		this.song = song;
		return this;
	}
}
