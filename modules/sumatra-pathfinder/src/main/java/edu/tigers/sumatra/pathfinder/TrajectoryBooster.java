/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.pathfinder;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2DLUT;


/**
 * The booster pre-generates LUTs for trajectories which are regularly requested.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class TrajectoryBooster
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(TrajectoryBooster.class.getName());
	
	private final ExecutorService lutGenExecutor;
	private final Map<Integer, Integer> bins = new ConcurrentHashMap<>();
	private final Map<Integer, BangBangTrajectory2DLUT> luts = new ConcurrentHashMap<>();
	private final double maxLUTPos;
	private final double maxLUTVel;
	private final int generationTriggerLimit;
	
	
	/**
	 * Constructor.
	 * 
	 * @param maxLUTPosition
	 * @param maxLUTVelocity
	 * @param generationTriggerLimit
	 */
	public TrajectoryBooster(final double maxLUTPosition, final double maxLUTVelocity, final int generationTriggerLimit)
	{
		maxLUTPos = maxLUTPosition;
		maxLUTVel = maxLUTVelocity;
		this.generationTriggerLimit = generationTriggerLimit;
		
		lutGenExecutor = Executors
				.newSingleThreadScheduledExecutor(new NamedThreadFactory("Traj LUT Generator", Thread.MIN_PRIORITY));
	}
	
	
	/**
	 * Query booster of trajectory is cached.
	 * 
	 * @param moveConstraints
	 * @param curPos
	 * @param curVel
	 * @param dest
	 * @return
	 */
	public Optional<BangBangTrajectory2D> query(final MoveConstraints moveConstraints,
			final IVector2 curPos,
			final IVector2 curVel,
			final IVector2 dest)
	{
		final int bin = (int) (moveConstraints.getVelMax() * 1000) + (int) (moveConstraints.getAccMax() * 1000 * 100000);
		
		if (luts.containsKey(bin))
		{
			return luts.get(bin).getTrajectory(curPos, dest, curVel);
		}
		
		bins.merge(bin, 0, (v1, v2) -> v1 + 1);
		
		if (bins.get(bin) == generationTriggerLimit)
		{
			lutGenExecutor.submit(() -> generateLUT(bin, moveConstraints.getVelMax(), moveConstraints.getAccMax()));
		}
		
		return Optional.empty();
	}
	
	
	private void generateLUT(final int bin, final double trajVelMax, final double trajAccMax)
	{
		BangBangTrajectory2DLUT lut = new BangBangTrajectory2DLUT(maxLUTPos, maxLUTVel, trajVelMax, trajAccMax);
		luts.put(bin, lut);
		
		log.info("Generated trajectory LUT for vMax: " + trajVelMax + ", aMax: " + trajAccMax);
	}
}
