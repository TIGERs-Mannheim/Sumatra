/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.bot.params;

import com.sleepycat.persist.model.Persistent;


/**
 * Robot movement limitations.
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


	@Override
	public double getVelMax()
	{
		return velMax;
	}


	public void setVelMax(final double velMax)
	{
		this.velMax = velMax;
	}


	@Override
	public double getAccMax()
	{
		return accMax;
	}


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


	@Override
	public double getJerkMax()
	{
		return jerkMax;
	}


	public void setJerkMax(final double jerkMax)
	{
		this.jerkMax = jerkMax;
	}


	@Override
	public double getVelMaxW()
	{
		return velMaxW;
	}


	public void setVelMaxW(final double velMaxW)
	{
		this.velMaxW = velMaxW;
	}


	@Override
	public double getAccMaxW()
	{
		return accMaxW;
	}


	public void setAccMaxW(final double accMaxW)
	{
		this.accMaxW = accMaxW;
	}


	@Override
	public double getJerkMaxW()
	{
		return jerkMaxW;
	}


	public void setJerkMaxW(final double jerkMaxW)
	{
		this.jerkMaxW = jerkMaxW;
	}


	@Override
	public double getVelMaxFast()
	{
		return velMaxFast;
	}


	public void setVelMaxFast(final double velMaxFast)
	{
		this.velMaxFast = velMaxFast;
	}


	@Override
	public double getAccMaxFast()
	{
		return accMaxFast;
	}


	public void setAccMaxFast(final double accMaxFast)
	{
		this.accMaxFast = accMaxFast;
	}

}
