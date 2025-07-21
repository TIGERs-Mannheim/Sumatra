/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.bot.params;

/**
 * Robot dimensions.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class BotDimensions implements IBotDimensions
{
	private double	center2DribblerDist	= 75.0;
	private double	diameter					= 180.0;
	private double	height					= 150.0;
	
	
	/**
	 * @return the center2DribblerDist
	 */
	@Override
	public double getCenter2DribblerDist()
	{
		return center2DribblerDist;
	}
	
	
	/**
	 * @param center2DribblerDist the center2DribblerDist to set
	 */
	public void setCenter2DribblerDist(final double center2DribblerDist)
	{
		this.center2DribblerDist = center2DribblerDist;
	}
	
	
	/**
	 * @return the diameter
	 */
	@Override
	public double getDiameter()
	{
		return diameter;
	}
	
	
	/**
	 * @param diameter the diameter to set
	 */
	public void setDiameter(final double diameter)
	{
		this.diameter = diameter;
	}
	
	
	/**
	 * @return the height
	 */
	@Override
	public double getHeight()
	{
		return height;
	}
	
	
	/**
	 * @param height the height to set
	 */
	public void setHeight(final double height)
	{
		this.height = height;
	}
}
