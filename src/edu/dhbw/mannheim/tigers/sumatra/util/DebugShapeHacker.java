/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 8, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.Agent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;


/**
 * Dirty Hacker-Util class
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DebugShapeHacker
{
	private static final Logger	log	= Logger.getLogger(DebugShapeHacker.class.getName());
	private static volatile Agent	agent	= null;
	
	
	/**
	 * Inject the shape into upcoming aiFrame.
	 * This is really dirty, please do not look at this :)
	 * 
	 * @param shape
	 */
	public static void addDebugShape(final IDrawableShape shape)
	{
		if (agent == null)
		{
			try
			{
				agent = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
			} catch (ModuleNotFoundException err)
			{
				log.error("Agent not found");
			}
		}
		if (agent != null)
		{
			agent.injectDrawableShape(shape);
		}
	}
}
