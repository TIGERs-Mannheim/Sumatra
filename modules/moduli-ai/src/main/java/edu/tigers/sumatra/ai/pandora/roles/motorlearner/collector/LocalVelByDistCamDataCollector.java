/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 11, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.motorlearner.collector;

import java.util.Optional;

import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AVector3;
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
public class LocalVelByDistCamDataCollector extends ACamDataCollector<IVector>
{
	private final BotID	botId;
								
	private IVector3		startPos	= null,
										endPos = null;
	private long			tStart	= -1,
										tEnd = -1;
										
										
	/**
	 * @param botId
	 */
	public LocalVelByDistCamDataCollector(final BotID botId)
	{
		super(EDataCollector.LOCAL_VEL_BY_DIST_CAM);
		this.botId = botId;
	}
	
	
	@Override
	protected void onNewCamFrame(final ExtendedCamDetectionFrame frame)
	{
		final Optional<CamRobot> newBot = getCamRobot(frame, botId);
		if (!newBot.isPresent())
		{
			return;
		}
		
		endPos = new Vector3(newBot.get().getPos(), newBot.get().getOrientation());
		tEnd = newBot.get().getTimestamp();
		if (startPos == null)
		{
			startPos = endPos;
			tStart = tEnd;
		} else
		{
			addSample(getVel());
		}
	}
	
	
	private IVector3 getVel()
	{
		if (tEnd == tStart)
		{
			return AVector3.ZERO_VECTOR;
		}
		double sampleTime = (tEnd - tStart) / 1e9;
		IVector2 posDiff = endPos.getXYVector().subtractNew(startPos.getXYVector()).multiply(1e-3f);
		posDiff = GeoMath.convertGlobalBotVector2Local(posDiff, startPos.z());
		IVector2 vel = posDiff.multiplyNew(1.0 / sampleTime);
		double angleDiff = AngleMath.difference(endPos.z(), startPos.z());
		double aVel = angleDiff / sampleTime;
		return new Vector3(vel, aVel);
	}
	
	
	@Override
	public void start()
	{
		startPos = null;
		endPos = null;
		tStart = -1;
		tEnd = -1;
		super.start();
	}
}
