/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

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


/**
 * Aggregate multiple calculators to collect all WP-related shapes
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class WorldFrameVisualization implements IRefereeObserver
{
	@SuppressWarnings("unused")
	private static final Logger	log	= Logger.getLogger(WorldFrameVisualization.class.getName());
	
	private final List<IWpCalc>	calcs	= new ArrayList<>();
	
	
	/**
	 * Create new instance
	 */
	public WorldFrameVisualization()
	{
		calcs.add(new BallVisCalc());
		calcs.add(new BorderVisCalc());
		calcs.add(new BotVisCalc());
		calcs.add(new BufferVisCalc());
		calcs.add(new RefereeVisCalc());
		calcs.add(new VelocityVisCalc());
	}
	
	
	/**
	 * Reset any internal states
	 */
	public void reset()
	{
		for (IWpCalc calc : calcs)
		{
			calc.reset();
		}
	}
	
	
	/**
	 * Add shapes to {@link WorldFrameWrapper}
	 *
	 * @param wfw world frame
	 * @param shapeMap
	 */
	public void process(final WorldFrameWrapper wfw, final ShapeMap shapeMap)
	{
		for (IWpCalc calc : calcs)
		{
			calc.process(wfw, shapeMap);
		}
	}
}
