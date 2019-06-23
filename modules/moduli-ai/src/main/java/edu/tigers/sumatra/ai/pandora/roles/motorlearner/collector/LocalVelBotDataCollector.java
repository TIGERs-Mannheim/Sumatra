/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.motorlearner.collector;

import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector3;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class LocalVelBotDataCollector extends ABotDataCollector<IVector>
{
	private final BotID	botId;
	
	
	/**
	 * @param botId
	 */
	public LocalVelBotDataCollector(final BotID botId)
	{
		super(EDataCollector.LOCAL_VEL_BOT);
		this.botId = botId;
	}
	
	
	@Override
	public void onNewMatchFeedback(final BotID botId, final TigerSystemMatchFeedback feedback)
	{
		if (!this.botId.equals(botId))
		{
			return;
		}
		IVector2 velXYglob = feedback.getVelocity();
		double velW = feedback.getAngularVelocity();
		double curAngle = feedback.getOrientation();
		IVector2 velXYloc = GeoMath.convertGlobalBotVector2Local(velXYglob, curAngle);
		addSample(new Vector3(velXYloc, velW));
	}
}
