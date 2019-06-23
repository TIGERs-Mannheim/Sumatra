/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.params.IBotMovementLimits;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class MoveConstraints
{
	private double		velMax		= 0;
	private double		accMax		= 0;
	private double		jerkMax		= 0;
	private double		velMaxW		= 0;
	private double		accMaxW		= 0;
	private double		jerkMaxW		= 0;
	private double		velMaxFast	= 0;
	private double		accMaxFast	= 0;
	private boolean	fastMove		= false;
	
	
	/**
	 * Create a dummy instance
	 */
	public MoveConstraints()
	{
	}
	
	
	/**
	 * Create move constraints from bot individual movement limits.
	 * 
	 * @param moveLimits
	 */
	public MoveConstraints(final IBotMovementLimits moveLimits)
	{
		velMax = moveLimits.getVelMax();
		accMax = moveLimits.getAccMax();
		jerkMax = moveLimits.getJerkMax();
		velMaxW = moveLimits.getVelMaxW();
		accMaxW = moveLimits.getAccMaxW();
		jerkMaxW = moveLimits.getJerkMaxW();
		velMaxFast = moveLimits.getVelMaxFast();
		accMaxFast = moveLimits.getAccMaxFast();
	}
	
	
	/**
	 * Create a copy
	 * 
	 * @param o instance to copy
	 */
	public MoveConstraints(final MoveConstraints o)
	{
		velMax = o.velMax;
		velMaxW = o.velMaxW;
		accMax = o.accMax;
		accMaxW = o.accMaxW;
		jerkMax = o.jerkMax;
		jerkMaxW = o.jerkMaxW;
		velMaxFast = o.velMaxFast;
		accMaxFast = o.accMaxFast;
		fastMove = o.fastMove;
	}
	
	
	/**
	 * Apply all non-zero limits to this instance.
	 * 
	 * @param o a (partial) set of move constraints
	 */
	public void mergeWith(final MoveConstraints o)
	{
		velMax = mergeDouble(velMax, o.velMax);
		velMaxW = mergeDouble(velMaxW, o.velMaxW);
		accMax = mergeDouble(accMax, o.accMax);
		accMaxW = mergeDouble(accMaxW, o.accMaxW);
		jerkMax = mergeDouble(jerkMax, o.jerkMax);
		jerkMaxW = mergeDouble(jerkMaxW, o.jerkMaxW);
		accMaxFast = mergeDouble(accMaxFast, o.accMaxFast);
		fastMove = o.fastMove;
	}
	
	
	private double mergeDouble(final double current, final double newValue)
	{
		if (newValue > 0)
		{
			return newValue;
		}
		return current;
	}
	
	
	@Override
	public String toString()
	{
		return "MoveConstraints{" +
				"velMax=" + velMax +
				", accMax=" + accMax +
				", jerkMax=" + jerkMax +
				", velMaxW=" + velMaxW +
				", accMaxW=" + accMaxW +
				", jerkMaxW=" + jerkMaxW +
				", accMaxFast=" + accMaxFast +
				", fastMove=" + fastMove +
				'}';
	}
	
	
	/**
	 * @return the velMax
	 */
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
	 * @return the velMaxW
	 */
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
	 * @return the accMax
	 */
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
	
	
	/**
	 * @return the accMaxW
	 */
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
	 * @return the jerkMax
	 */
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
	 * @return the jerkMaxW
	 */
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
	 * Activate the fastPos move skill.<br>
	 *
	 * @param fastMove activate fast move skill
	 */
	public void setFastMove(final boolean fastMove)
	{
		this.fastMove = fastMove;
	}
	
	
	public boolean isFastMove()
	{
		return fastMove;
	}
	
	
	/**
	 * @return the velMaxFast
	 */
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
