/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 15, 2015
 * Author(s): geforce
 * *********************************************************
 */
package edu.tigers.sumatra.visualizer;

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotStatus
{
	private boolean	visible		= false;
	private boolean	connected	= false;
	private double		batRel		= 0;
	private double		kickerRel	= 0;
											
	private boolean	hideRcm		= false;
	private boolean	hideAi		= false;
											
											
	/**
	 * @return the visible
	 */
	public final boolean isVisible()
	{
		return visible;
	}
	
	
	/**
	 * @param visible the visible to set
	 */
	public final void setVisible(final boolean visible)
	{
		this.visible = visible;
	}
	
	
	/**
	 * @return the connected
	 */
	public final boolean isConnected()
	{
		return connected;
	}
	
	
	/**
	 * @param connected the connected to set
	 */
	public final void setConnected(final boolean connected)
	{
		this.connected = connected;
	}
	
	
	/**
	 * @return the batRel
	 */
	public final double getBatRel()
	{
		return batRel;
	}
	
	
	/**
	 * @param batRel the batRel to set
	 */
	public final void setBatRel(final double batRel)
	{
		this.batRel = batRel;
	}
	
	
	/**
	 * @return the kickerRel
	 */
	public final double getKickerRel()
	{
		return kickerRel;
	}
	
	
	/**
	 * @param kickerRel the kickerRel to set
	 */
	public final void setKickerRel(final double kickerRel)
	{
		this.kickerRel = kickerRel;
	}
	
	
	/**
	 * @return the hideRcm
	 */
	public final boolean isHideRcm()
	{
		return hideRcm;
	}
	
	
	/**
	 * @param hideRcm the hideRcm to set
	 */
	public final void setHideRcm(final boolean hideRcm)
	{
		this.hideRcm = hideRcm;
	}
	
	
	/**
	 * @return the hideAi
	 */
	public final boolean isHideAi()
	{
		return hideAi;
	}
	
	
	/**
	 * @param hideAi the hideAi to set
	 */
	public final void setHideAi(final boolean hideAi)
	{
		this.hideAi = hideAi;
	}
	
}
