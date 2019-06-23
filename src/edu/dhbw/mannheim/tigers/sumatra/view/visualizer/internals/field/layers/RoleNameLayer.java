/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 4, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.BotAiInformation;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;


/**
 * Display role names for each bot
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
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
	protected void paintLayerAif(final Graphics2D g, final IRecordFrame frame)
	{
		g.setColor(Color.yellow);
		g.setFont(new Font("", Font.PLAIN, 9));
		for (Map.Entry<BotID, BotAiInformation> entry : frame.getTacticalField().getBotAiInformation().entrySet())
		{
			BotID botID = entry.getKey();
			BotAiInformation aiInfo = entry.getValue();
			
			TrackedTigerBot bot = frame.getWorldFrame().getTigerBotsVisible().getWithNull(botID);
			if (bot == null)
			{
				continue;
			}
			IVector2 pos = bot.getPos();
			IVector2 guiPos = getFieldPanel().transformToGuiCoordinates(pos, frame.getWorldFrame().isInverted());
			g.drawString(aiInfo.getRole(), guiPos.x() + 10, guiPos.y());
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
