/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.motorlearner.collector;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class LocalVelWpDataCollector extends AWpDataCollector<IVector>
{
	private final BotID	botId;
	
	
	/**
	 * @param botId
	 */
	public LocalVelWpDataCollector(final BotID botId)
	{
		super(EDataCollector.LOCAL_VEL_WP);
		this.botId = botId;
	}
	
	
	@Override
	protected void onNewWorldFrameWrapper(final WorldFrameWrapper wfw)
	{
		ITrackedBot bot = wfw.getSimpleWorldFrame().getBot(botId);
		if (bot == null)
		{
			return;
		}
		IVector2 velXYglob = bot.getVel();
		double velW = bot.getaVel();
		double curAngle = bot.getAngle();
		IVector2 velXYloc = GeoMath.convertGlobalBotVector2Local(velXYglob, curAngle);
		addSample(new Vector3(velXYloc, velW));
	}
}
