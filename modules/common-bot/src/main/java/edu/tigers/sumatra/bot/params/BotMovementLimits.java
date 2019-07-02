/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.bot.params;

import com.sleepycat.persist.model.Persistent;


/**
 * Robot movement limitations.
 * 
 * @author AndreR <andre@ryll.cc>
 */
@Persistent
public class BotMovementLimits implements IBotMovementLimits
{
	private double velMax = 2;
	private double accMax = 3;
	private double brkMax = 6;
	private double jerkMax = 20;
	
	private double velMaxW = 20;
	private double accMaxW = 50;
	private double jerkMaxW = 1000;
	
	private double velMaxFast = 3;
	private double accMaxFast = 4;
	
	
	/**
	 * Default constructor.
	 */
	public BotMovementLimits()
	{
	}
	
	
	/**
	 * Copy constructor.
	 * 
	 * @param limits
	 */
	public BotMovementLimits(final IBotMovementLimits limits)
	{
		velMax = limits.getVelMax();
		accMax = limits.getAccMax();
		brkMax = limits.getBrkMax();
		jerkMax = limits.getJerkMax();
		velMaxW = limits.getVelMaxW();
		accMaxW = limits.getAccMaxW();
		jerkMaxW = limits.getJerkMaxW();
		velMaxFast = limits.getVelMaxFast();
		accMaxFast = limits.getAccMaxFast();
	}
	
	
	/**
	 * @return the velMax
	 */
	@Override
	public double getVelMax()
	{
		return velMax;
	}
	
	
	/**
	 * @param velMax the velMax to set
	 */
	public void setVelMax(final double velMax)
	{
		this.velMax = velMax;
	}
	
	
	/**
	 * @return the accMax
	 */
	@Override
	public double getAccMax()
	{
		return accMax;
	}
	
	
	/**
	 * @param accMax the accMax to set
	 */
	public void setAccMax(final double accMax)
	{
		this.accMax = accMax;
	}
	
	
	@Override
	public double getBrkMax()
	{
		return brkMax;
	}
	
	
	public void setBrkMax(final double brkMax)
	{
		this.brkMax = brkMax;
	}
	
	
	/**
	 * @return the jerkMax
	 */
	@Override
	public double getJerkMax()
	{
		return jerkMax;
	}
	
	
	/**
	 * @param jerkMax the jerkMax to set
	 */
	public void setJerkMax(final double jerkMax)
	{
		this.jerkMax = jerkMax;
	}
	
	
	/**
	 * @return the velMaxW
	 */
	@Override
	public double getVelMaxW()
	{
		return velMaxW;
	}
	
	
	/**
	 * @param velMaxW the velMaxW to set
	 */
	public void setVelMaxW(final double velMaxW)
	{
		this.velMaxW = velMaxW;
	}
	
	
	/**
	 * @return the accMaxW
	 */
	@Override
	public double getAccMaxW()
	{
		return accMaxW;
	}
	
	
	/**
	 * @param accMaxW the accMaxW to set
	 */
	public void setAccMaxW(final double accMaxW)
	{
		this.accMaxW = accMaxW;
	}
	
	
	/**
	 * @return the jerkMaxW
	 */
	@Override
	public double getJerkMaxW()
	{
		return jerkMaxW;
	}
	
	
	/**
	 * @param jerkMaxW the jerkMaxW to set
	 */
	public void setJerkMaxW(final double jerkMaxW)
	{
		this.jerkMaxW = jerkMaxW;
	}
	
	
	/**
	 * @return the velMaxFast
	 */
	@Override
	public double getVelMaxFast()
	{
		return velMaxFast;
	}
	
	
	/**
	 * @param velMaxFast the velMaxFast to set
	 */
	public void setVelMaxFast(final double velMaxFast)
	{
		this.velMaxFast = velMaxFast;
	}
	
	
	/**
	 * @return the accMaxFast
	 */
	@Override
	public double getAccMaxFast()
	{
		return accMaxFast;
	}
	
	
	/**
	 * @param accMaxFast the accMaxFast to set
	 */
	public void setAccMaxFast(final double accMaxFast)
	{
		this.accMaxFast = accMaxFast;
	}
	
}
