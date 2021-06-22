/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.bot.params.IBotMovementLimits;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Constraints on the movement of robots.
 */
@Persistent
@Data
@Accessors(chain = true)
public class MoveConstraints implements IExportable, IMoveConstraints
{
	private double velMax;
	private double accMax;
	private double brkMax;
	private double jerkMax;
	private double velMaxW;
	private double accMaxW;
	private double jerkMaxW;
	private double velMaxFast;
	private double accMaxFast;

	private boolean fastMove;
	@NonNull // Primary direction must not be null. Set it to a Zero-Vector to disable it.
	private IVector2 primaryDirection = Vector2f.ZERO_VECTOR;


	/**
	 * Create a dummy instance
	 */
	public MoveConstraints()
	{
	}


	public MoveConstraints(MoveConstraints mc)
	{
		velMax = mc.velMax;
		accMax = mc.accMax;
		brkMax = mc.brkMax;
		jerkMax = mc.jerkMax;
		velMaxW = mc.velMaxW;
		accMaxW = mc.accMaxW;
		jerkMaxW = mc.jerkMaxW;
		velMaxFast = mc.velMaxFast;
		accMaxFast = mc.accMaxFast;
		fastMove = mc.fastMove;
		primaryDirection = mc.primaryDirection;
	}


	/**
	 * Create move constraints from bot individual movement limits.
	 *
	 * @param moveLimits
	 */
	public MoveConstraints(final IBotMovementLimits moveLimits)
	{
		resetLimits(moveLimits);
	}


	public void resetLimits(final IBotMovementLimits moveLimits)
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


	public MoveConstraints limit(final IBotMovementLimits movementLimits)
	{
		velMax = Math.min(velMax, movementLimits.getVelMax());
		accMax = Math.min(accMax, movementLimits.getAccMax());
		brkMax = Math.min(brkMax, movementLimits.getBrkMax());
		jerkMax = Math.min(jerkMax, movementLimits.getJerkMax());
		velMaxW = Math.min(velMaxW, movementLimits.getVelMaxW());
		accMaxW = Math.min(accMaxW, movementLimits.getAccMaxW());
		jerkMaxW = Math.min(jerkMaxW, movementLimits.getJerkMaxW());
		velMaxFast = Math.min(velMaxFast, movementLimits.getVelMaxFast());
		accMaxFast = Math.min(accMaxFast, movementLimits.getAccMaxFast());
		return this;
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


	@Override
	public double getVelMax()
	{
		if (fastMove)
		{
			return velMaxFast;
		}
		return velMax;
	}


	public void setVelMax(final double velMax)
	{
		assert velMax >= 0 : "vel: " + velMax;
		this.velMax = velMax;
	}


	public void setVelMaxW(final double velMaxW)
	{
		assert velMaxW >= 0;
		this.velMaxW = velMaxW;
	}


	@Override
	public double getAccMax()
	{
		return accMax;
	}


	public MoveConstraints setAccMax(final double accMax)
	{
		assert accMax >= 0;
		this.accMax = accMax;
		return this;
	}


	public MoveConstraints setAccMaxW(final double accMaxW)
	{
		assert accMaxW >= 0;
		this.accMaxW = accMaxW;
		return this;
	}


	public MoveConstraints setJerkMax(final double jerkMax)
	{
		assert jerkMax >= 0;
		this.jerkMax = jerkMax;
		return this;
	}


	public MoveConstraints setJerkMaxW(final double jerkMaxW)
	{
		assert jerkMaxW >= 0;
		this.jerkMaxW = jerkMaxW;
		return this;
	}
}
