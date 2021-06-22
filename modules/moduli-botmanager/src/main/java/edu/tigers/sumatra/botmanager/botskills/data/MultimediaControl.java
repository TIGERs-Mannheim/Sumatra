/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager.botskills.data;


import lombok.NoArgsConstructor;


/**
 * Data structure for controlling the multimedia functions of our robots
 */
@NoArgsConstructor
public class MultimediaControl
{
	private ELedColor ledColor = ELedColor.OFF;
	private ESong song = ESong.NONE;


	public MultimediaControl(MultimediaControl multimediaControl)
	{
		this.ledColor = multimediaControl.ledColor;
		this.song = multimediaControl.song;
	}


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
