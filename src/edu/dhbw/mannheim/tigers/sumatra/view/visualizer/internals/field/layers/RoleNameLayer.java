/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 4, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;


/**
 * Display role names for each bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class RoleNameLayer extends AFieldLayer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 */
	public RoleNameLayer()
	{
		super(EFieldLayer.ROLE_NAME);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void paintLayer(Graphics2D g)
	{
		g.setColor(Color.yellow);
		for (Map.Entry<BotID, ERole> entry : getAiFrame().getAssigendERoles().entrySet())
		{
			BotID botID = entry.getKey();
			ERole role = entry.getValue();
			TrackedTigerBot bot = getAiFrame().getRecordWfFrame().getTigerBotsVisible().getWithNull(botID);
			if (bot == null)
			{
				continue;
			}
			IVector2 pos = bot.getPos();
			IVector2 guiPos = FieldPanel.transformToGuiCoordinates(pos);
			g.drawString(role.name(), guiPos.x(), guiPos.y());
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
