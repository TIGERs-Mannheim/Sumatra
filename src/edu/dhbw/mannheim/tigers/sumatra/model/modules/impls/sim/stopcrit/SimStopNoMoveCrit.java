/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 1, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.sim.stopcrit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimStopNoMoveCrit extends ASimStopCriterion
{
	private static final Logger	log					= Logger.getLogger(SimStopNoMoveCrit.class.getName());
	
	@Configurable
	private static float				distTolerance		= 1;
	
	@Configurable
	private static float				timeBeforeCheck	= 1;
	
	
	@Override
	protected boolean checkStopSimulation()
	{
		if (getRuntime() < (timeBeforeCheck * 1e9))
		{
			return false;
		}
		for (TrackedTigerBot bot : getLatestFrame().getWorldFrame().getBots().values())
		{
			TrackedTigerBot preBot = getLatestFrame().getPrevFrame().getWorldFrame().getBot(bot.getId());
			float dist = GeoMath.distancePP(bot.getPos(), preBot.getPos());
			if (dist > distTolerance)
			{
				return false;
			}
		}
		log.debug("Stopped because no bot moved");
		return true;
	}
	
}
