/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import edu.tigers.sumatra.wp.vis.BallVisCalc;
import edu.tigers.sumatra.wp.vis.BorderVisCalc;
import edu.tigers.sumatra.wp.vis.BotVisCalc;
import edu.tigers.sumatra.wp.vis.BufferVisCalc;
import edu.tigers.sumatra.wp.vis.IWpCalc;
import edu.tigers.sumatra.wp.vis.RefereeVisCalc;
import edu.tigers.sumatra.wp.vis.VelocityVisCalc;
import lombok.extern.log4j.Log4j2;

import java.util.List;


/**
 * Aggregate multiple calculators to collect all WP-related shapes
 */
@Log4j2
public class WorldFrameVisualization implements IRefereeObserver
{
	private final List<IWpCalc> calcs = List.of(
			new BallVisCalc(),
			new BorderVisCalc(),
			new BotVisCalc(),
			new BufferVisCalc(),
			new RefereeVisCalc(),
			new VelocityVisCalc()
	);


	/**
	 * Reset any internal states
	 */
	public synchronized void reset()
	{
		for (IWpCalc calc : calcs)
		{
			calc.reset();
		}
	}


	/**
	 * Add shapes to {@link WorldFrameWrapper}
	 *
	 * @param wfw      world frame
	 * @param shapeMap
	 */
	public synchronized void process(final WorldFrameWrapper wfw, final ShapeMap shapeMap)
	{
		for (IWpCalc calc : calcs)
		{
			calc.process(wfw, shapeMap);
		}
	}
}
