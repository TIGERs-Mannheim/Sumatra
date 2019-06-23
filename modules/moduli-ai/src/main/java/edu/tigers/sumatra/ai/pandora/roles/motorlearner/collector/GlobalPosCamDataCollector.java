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

import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IVector;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class GlobalPosCamDataCollector extends ACamDataCollector<IVector>
{
	private final BotID	botId;
	
	
	/**
	 * @param botId
	 */
	public GlobalPosCamDataCollector(final BotID botId)
	{
		super(EDataCollector.GLOBAL_POS_CAM);
		this.botId = botId;
	}
	
	
	@Override
	protected void onNewCamFrame(final ExtendedCamDetectionFrame frame)
	{
		final Optional<CamRobot> newBot = getCamRobot(frame, botId);
		if (newBot.isPresent())
		{
			addSample(new Vector3(newBot.get().getPos().multiplyNew(1e-3f), newBot.get().getOrientation()));
		}
	}
}
