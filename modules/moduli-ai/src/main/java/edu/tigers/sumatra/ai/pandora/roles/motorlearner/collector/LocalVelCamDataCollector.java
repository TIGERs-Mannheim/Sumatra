/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.motorlearner.collector;

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
public class LocalVelCamDataCollector extends ACamDataCollector<IVector>
{
	@SuppressWarnings("unused")
	private static final Logger	log			= Logger.getLogger(LocalVelCamDataCollector.class.getName());
	
	private final BotID				botId;
	private Optional<CamRobot>		preBot		= Optional.empty();
	private Optional<CamRobot>		prePreBot	= Optional.empty();
	private int							camId			= -1;
	private final double				minDt;
	
	
	/**
	 * @param botId
	 * @param minDt
	 */
	public LocalVelCamDataCollector(final BotID botId, final double minDt)
	{
		super(EDataCollector.LOCAL_VEL_CAM);
		this.botId = botId;
		this.minDt = minDt;
	}
	
	
	/**
	 * @param botId
	 */
	public LocalVelCamDataCollector(final BotID botId)
	{
		this(botId, 0);
	}
	
	
	@Override
	protected void onNewCamFrame(final ExtendedCamDetectionFrame frame)
	{
		final Optional<CamRobot> newBot = getCamRobot(frame, botId);
		if (!newBot.isPresent())
		{
			return;
		}
		
		if (camId == -1)
		{
			camId = newBot.get().getCameraId();
		} else if (camId != newBot.get().getCameraId())
		{
			return;
		}
		
		if (!preBot.isPresent())
		{
			preBot = newBot;
			return;
		}
		
		
		double dt = (newBot.get().getTimestamp() - preBot.get().getTimestamp()) / 1e9;
		if (dt > minDt)
		{
			if (!prePreBot.isPresent())
			{
				prePreBot = preBot;
				preBot = newBot;
				return;
			}
			
			IVector3 velLocal = getVel(prePreBot.get(), preBot.get(), newBot.get());
			addSample(velLocal);
			
			prePreBot = preBot;
			preBot = newBot;
		}
	}
	
	
	@Override
	public void start()
	{
		super.start();
		camId = -1;
		preBot = Optional.empty();
		prePreBot = Optional.empty();
	}
}
