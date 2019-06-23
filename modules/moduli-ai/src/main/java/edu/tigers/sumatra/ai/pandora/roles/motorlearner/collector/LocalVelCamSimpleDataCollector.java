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
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class LocalVelCamSimpleDataCollector extends ACamDataCollector<IVector>
{
	@SuppressWarnings("unused")
	private static final Logger	log		= Logger.getLogger(LocalVelCamSimpleDataCollector.class.getName());
	
	private final BotID				botId;
	private Optional<CamRobot>		preBot	= Optional.empty();
	private int							camId		= -1;
	private final double				minDt;
	
	
	/**
	 * @param botId
	 * @param minDt
	 */
	public LocalVelCamSimpleDataCollector(final BotID botId, final double minDt)
	{
		super(EDataCollector.LOCAL_VEL_CAM_SIMPLE);
		this.botId = botId;
		this.minDt = minDt;
	}
	
	
	/**
	 * @param botId
	 */
	public LocalVelCamSimpleDataCollector(final BotID botId)
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
			IVector2 globVel = newBot.get().getPos().subtractNew(preBot.get().getPos()).multiply(1 / (1000 * dt));
			double meanAngle = AngleMath.normalizeAngle(preBot.get().getOrientation()
					+ (AngleMath.difference(newBot.get().getOrientation(), preBot.get().getOrientation()) / 2));
			double aVel = AngleMath.difference(newBot.get().getOrientation(), preBot.get().getOrientation()) / dt;
			IVector2 localVel = GeoMath.convertGlobalBotVector2Local(globVel, meanAngle);
			IVector3 velLocal = new Vector3(localVel, aVel);
			addSample(velLocal);
			
			preBot = newBot;
		}
	}
	
	
	@Override
	public void start()
	{
		super.start();
		camId = -1;
		preBot = Optional.empty();
	}
}
