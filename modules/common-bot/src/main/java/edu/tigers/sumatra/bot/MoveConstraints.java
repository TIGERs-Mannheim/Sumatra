/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.bot.params.IBotMovementLimits;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class MoveConstraints implements IExportable
{
	private double velMax = 0;
	private double accMax = 0;
	private double brkMax = 0;
	private double jerkMax = 0;
	private double velMaxW = 0;
	private double accMaxW = 0;
	private double jerkMaxW = 0;
	private double velMaxFast = 0;
	private double accMaxFast = 0;
	private boolean fastMove = false;
	private IVector2 primaryDirection = Vector2f.ZERO_VECTOR;
	
	
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
		brkMax = moveLimits.getBrkMax();
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
		brkMax = o.brkMax;
		accMaxW = o.accMaxW;
		jerkMax = o.jerkMax;
		jerkMaxW = o.jerkMaxW;
		velMaxFast = o.velMaxFast;
		accMaxFast = o.accMaxFast;
		fastMove = o.fastMove;
		primaryDirection = o.primaryDirection;
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
		brkMax = mergeDouble(brkMax, o.brkMax);
		accMaxW = mergeDouble(accMaxW, o.accMaxW);
		jerkMax = mergeDouble(jerkMax, o.jerkMax);
		jerkMaxW = mergeDouble(jerkMaxW, o.jerkMaxW);
		velMaxFast = mergeDouble(velMaxFast, o.velMaxFast);
		accMaxFast = mergeDouble(accMaxFast, o.accMaxFast);
		fastMove = o.fastMove;
		primaryDirection = o.primaryDirection;
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
				", brkMax=" + brkMax +
				", jerkMax=" + jerkMax +
				", velMaxW=" + velMaxW +
				", accMaxW=" + accMaxW +
				", jerkMaxW=" + jerkMaxW +
				", velMaxFast=" + velMaxFast +
				", accMaxFast=" + accMaxFast +
				", fastMove=" + fastMove +
				", primaryDirection=" + primaryDirection +
				'}';
	}
	
	
	@Override
	public List<Number> getNumberList()
	{
		List<Number> nbrs = new ArrayList<>();
		nbrs.add(velMax);
		nbrs.add(accMax);
		nbrs.add(jerkMax);
		nbrs.add(velMaxW);
		nbrs.add(accMaxW);
		nbrs.add(jerkMaxW);
		nbrs.add(velMaxFast);
		nbrs.add(accMaxFast);
		nbrs.add(fastMove ? 1 : 0);
		nbrs.add(primaryDirection.x());
		nbrs.add(primaryDirection.y());
		return nbrs;
	}
	
	
	@Override
	public List<String> getHeaders()
	{
		return Arrays.asList("velMax", "accMax", "jerkMax", "velMaxW", "accMaxW", "jerkMaxW", "velMaxFast", "accMaxFast",
				"fastMove", "primaryDirectionX", "primaryDirectionY");
	}
	
	
	/**
	 * @return the velMax
	 */
	public double getVelMax()
	{
		if (fastMove)
		{
			return velMaxFast;
		}
		return velMax;
	}
	
	
	/**
	 * @param velMax the velMax to set
	 */
	public void setVelMax(final double velMax)
	{
		assert velMax >= 0 : "vel: " + velMax;
		this.velMax = velMax;
	}
	
	
	/**
	 * @param velMaxFast the velMaxFast to set
	 */
	public void setVelMaxFast(final double velMaxFast)
	{
		assert velMax >= 0 : "vel: " + velMaxFast;
		this.velMaxFast = velMaxFast;
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
		assert velMaxW >= 0;
		this.velMaxW = velMaxW;
	}
	
	
	/**
	 * @return the accMax
	 */
	public double getAccMax()
	{
		if (fastMove)
		{
			return accMaxFast;
		}
		return accMax;
	}
	
	
	/**
	 * @param accMax the accMax to set
	 */
	public void setAccMax(final double accMax)
	{
		assert accMax >= 0;
		this.accMax = accMax;
	}
	
	
	public double getBrkMax()
	{
		return brkMax;
	}
	
	
	public void setBrkMax(final double brkMax)
	{
		this.brkMax = brkMax;
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
		assert accMaxW >= 0;
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
		assert jerkMax >= 0;
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
		assert jerkMaxW >= 0;
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
	 * @param primaryDirection
	 */
	public void setPrimaryDirection(final IVector2 primaryDirection)
	{
		assert primaryDirection != null;
		this.primaryDirection = primaryDirection;
	}
	
	
	/**
	 * @return the primaryDirection
	 */
	public IVector2 getPrimaryDirection()
	{
		return primaryDirection;
	}
}
