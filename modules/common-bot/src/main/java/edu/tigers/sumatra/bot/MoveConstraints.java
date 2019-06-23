/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 21, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.bot;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.sleepycat.persist.model.Persistent;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class MoveConstraints
{
	@Configurable(spezis = { "", "v2013" }, defValueSpezis = { "3.5", "3" })
	private double						velMax	= 3.5;
	@Configurable(spezis = { "", "v2013" }, defValueSpezis = { "10", "10" })
	private double						velMaxW	= 10;
	@Configurable(spezis = { "", "v2013" }, defValueSpezis = { "3", "3" })
	private double						accMax	= 3;
	@Configurable(spezis = { "", "v2013" }, defValueSpezis = { "25", "25" })
	private double						accMaxW	= 25;
	@Configurable(spezis = { "", "v2013" }, defValueSpezis = { "50", "50" })
	private double						jerkMax	= 50;
	@Configurable(spezis = { "", "v2013" }, defValueSpezis = { "500", "500" })
	private double						jerkMaxW	= 500;
	
	private final MoveConstraints	defConstraints;
	
	static
	{
		ConfigRegistration.registerClass("botmgr", MoveConstraints.class);
	}
	
	
	/**
	 * Default constraints
	 */
	public MoveConstraints()
	{
		defConstraints = null;
	}
	
	
	/**
	 * @param o
	 */
	public MoveConstraints(final MoveConstraints o)
	{
		velMax = o.velMax;
		velMaxW = o.velMaxW;
		accMax = o.accMax;
		accMaxW = o.accMaxW;
		jerkMax = o.jerkMax;
		jerkMaxW = o.jerkMaxW;
		defConstraints = o;
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
	 * @return the defConstraints
	 */
	public MoveConstraints getDefConstraints()
	{
		return defConstraints;
	}
	
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(velMax);
		builder.append(",");
		builder.append(velMaxW);
		builder.append("|");
		builder.append(accMax);
		builder.append(",");
		builder.append(accMaxW);
		builder.append("|");
		builder.append(jerkMax);
		builder.append(",");
		builder.append(jerkMaxW);
		return builder.toString();
	}
	
	
	/**
	 * 
	 */
	public void setDefaultVelLimit()
	{
		if (defConstraints != null)
		{
			velMax = defConstraints.velMax;
		}
	}
	
	
	/**
	 * 
	 */
	public void setDefaultAccLimit()
	{
		if (defConstraints != null)
		{
			accMax = defConstraints.accMax;
		}
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
}
