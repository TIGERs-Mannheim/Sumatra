/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.07.2016
 * Author(s): kisle
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands;

import edu.tigers.sumatra.botmanager.commands.other.ESong;


/**
 * @author MarkG
 */
public class MultimediaControl
{
	private boolean	leftGreen	= false;
	private boolean	leftRed		= false;
	private boolean	rightGreen	= false;
	private boolean	rightRed		= false;
	
	private ESong		song			= ESong.NONE;
	
	
	/**
	 * Constructor.
	 */
	public MultimediaControl()
	{
		// Default parameters
	}
	
	
	/**
	 * @param lg
	 * @param lr
	 * @param rg
	 * @param rr
	 */
	public MultimediaControl(final boolean lg, final boolean lr, final boolean rg, final boolean rr)
	{
		leftGreen = lg;
		leftRed = lr;
		rightGreen = rg;
		rightRed = rr;
	}
	
	
	/**
	 * @return the leftGreen
	 */
	public boolean isLeftGreen()
	{
		return leftGreen;
	}
	
	
	/**
	 * @return the leftRed
	 */
	public boolean isLeftRed()
	{
		return leftRed;
	}
	
	
	/**
	 * @return the rightGreen
	 */
	public boolean isRightGreen()
	{
		return rightGreen;
	}
	
	
	/**
	 * @return the rightRed
	 */
	public boolean isRightRed()
	{
		return rightRed;
	}
	
	
	/**
	 * @param leftGreen the leftGreen to set
	 */
	public void setLeftGreen(final boolean leftGreen)
	{
		this.leftGreen = leftGreen;
	}
	
	
	/**
	 * @param leftRed the leftRed to set
	 */
	public void setLeftRed(final boolean leftRed)
	{
		this.leftRed = leftRed;
	}
	
	
	/**
	 * @param rightGreen the rightGreen to set
	 */
	public void setRightGreen(final boolean rightGreen)
	{
		this.rightGreen = rightGreen;
	}
	
	
	/**
	 * @param rightRed the rightRed to set
	 */
	public void setRightRed(final boolean rightRed)
	{
		this.rightRed = rightRed;
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
	 */
	public final void setSong(final ESong song)
	{
		this.song = song;
	}
}
