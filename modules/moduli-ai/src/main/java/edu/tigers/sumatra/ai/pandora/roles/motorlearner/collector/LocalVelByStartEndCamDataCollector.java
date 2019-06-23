/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 11, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.motorlearner.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IVector;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class LocalVelByStartEndCamDataCollector extends ACamDataCollector<IVector>
{
	@SuppressWarnings("unused")
	private static final Logger	log		= Logger.getLogger(LocalVelByStartEndCamDataCollector.class.getName());
	
	private final BotID				botId;
	private final double				minDt;
	private final List<CamRobot>	samples	= new ArrayList<>();
	
	
	/**
	 * @param botId
	 * @param minDt
	 */
	public LocalVelByStartEndCamDataCollector(final BotID botId, final double minDt)
	{
		super(EDataCollector.LOCAL_VEL_BY_START_END_CAM);
		this.botId = botId;
		this.minDt = minDt;
	}
	
	
	@Override
	protected void onNewCamFrame(final ExtendedCamDetectionFrame frame)
	{
		final Optional<CamRobot> newBot = getCamRobot(frame, botId);
		if (!newBot.isPresent())
		{
			return;
		}
		
		samples.add(newBot.get());
		
		if (samples.size() < 3)
		{
			return;
		}
		double dt = (samples.get(samples.size() - 1).getTimestamp() - samples.get(0).getTimestamp()) / 1e9;
		if (dt <= minDt)
		{
			return;
		}
		
		IVector3 vel = getVel(samples);
		addSample(vel);
	}
	
	
	@Override
	public void start()
	{
		samples.clear();
		super.start();
	}
	
	
	@Override
	public void stop()
	{
		super.stop();
		// CSVExporter exp = new CSVExporter("logs/startEndCollector/" + System.currentTimeMillis(), false);
		// for (CamRobot bot : samples)
		// {
		// RawBot rb = new RawBot(bot);
		// exp.addValues(rb.getNumberList());
		// }
		// exp.close();
	}
}
