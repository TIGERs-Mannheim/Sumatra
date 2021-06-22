/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.rcm;

import edu.tigers.sumatra.botmanager.bots.ABot;


public class ControllerState
{
	private final ABot bot;
	private boolean highSpeedMode = false;
	private double compassThreshold = 0;

	public ControllerState(final ABot bot)
	{
		this.bot = bot;
	}


	public ABot getBot()
	{
		return bot;
	}


	public boolean isHighSpeedMode()
	{
		return highSpeedMode;
	}


	public void setHighSpeedMode(final boolean highSpeedMode)
	{
		this.highSpeedMode = highSpeedMode;
	}


	public double getCompassThreshold()
	{
		return compassThreshold;
	}


	public void setCompassThreshold(final double compassThreshold)
	{
		this.compassThreshold = compassThreshold;
	}
}
