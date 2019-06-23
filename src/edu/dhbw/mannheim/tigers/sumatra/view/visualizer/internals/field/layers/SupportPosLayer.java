/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 28, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.model.data.ValuedField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Visualize information about Support positions and stuff
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SupportPosLayer extends AFieldLayer implements IConfigObserver
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	@Configurable(comment = "show SupportPositionsLayer for BotID")
	private static int	botID_int	= 0;
	private BotID			botId			= null;
	
	private IRecordFrame	Rframe		= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public SupportPosLayer()
	{
		super(EFieldLayer.SUPPORT_POS, false);
		AIConfig.getLayerClient().addObserver(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void paintLayerAif(final Graphics2D g, final IRecordFrame frame)
	{
		setFrame(frame);
		
		g.setColor(Color.YELLOW);
		for (IVector2 pos : frame.getTacticalField().getSupportIntersections())
		{
			IVector2 guiPos = getFieldPanel().transformToGuiCoordinates(pos, frame.getWorldFrame().isInverted());
			g.fillOval((int) guiPos.x() - 1, (int) guiPos.y() - 1, 3, 3);
		}
		
		g.setColor(Color.BLUE);
		g.setStroke(new BasicStroke(1));
		for (Map.Entry<BotID, IVector2> entry : frame.getTacticalField().getSupportRedirectPositions().entrySet())
		{
			TrackedTigerBot bot = frame.getWorldFrame().getBot(entry.getKey());
			if (bot == null)
			{
				continue;
			}
			IVector2 botPos = bot.getPos();
			IVector2 target = entry.getValue();
			IVector2 guiBotPos = getFieldPanel().transformToGuiCoordinates(botPos, frame.getWorldFrame().isInverted());
			IVector2 guiTarget = getFieldPanel().transformToGuiCoordinates(target, frame.getWorldFrame().isInverted());
			g.drawLine((int) guiBotPos.x(), (int) guiBotPos.y(), (int) guiTarget.x(), (int) guiTarget.y());
		}
		
		if (frame.getWorldFrame().getTigerBotsAvailable().isEmpty())
		{
			return;
		}
		
		botId = checkBotID(frame, botID_int);
		
		TrackedTigerBot bot = frame.getWorldFrame().getBot(botId);
		if (bot == null)
		{
			return;
		}
		ValuedField vf = frame.getTacticalField().getSupportValues().get(bot.getId());
		IVector2 guiBotPos = getFieldPanel().transformToGuiCoordinates(bot.getPos(), frame.getWorldFrame().isInverted());
		int radiusX = getFieldPanel().scaleXLength(AIConfig.getGeometry().getBotRadius()) + 2;
		int radiusY = getFieldPanel().scaleYLength(AIConfig.getGeometry().getBotRadius()) + 2;
		
		g.setColor(Color.CYAN);
		g.drawOval((int) guiBotPos.x() - radiusX, (int) guiBotPos.y() - radiusY, radiusX * 2, radiusY * 2);
		if (vf != null)
		{
			vf.setDrawDebug(isDebugInformationVisible());
			vf.setDrawInverted(frame.getWorldFrame().isInverted());
			vf.paintShape(g, getFieldPanel(), frame.getWorldFrame().isInverted());
		}
	}
	
	
	@Override
	protected void paintDebugInformation(final Graphics2D g, final IRecordFrame frame)
	{
	}
	
	
	@Override
	public void onLoad(final HierarchicalConfiguration newConfig)
	{
		IRecordFrame frame = getFrame();
		botId = checkBotID(frame, botID_int);
	}
	
	
	@Override
	public void onReload(final HierarchicalConfiguration freshConfig)
	{
		IRecordFrame frame = getFrame();
		botId = checkBotID(frame, botID_int);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the frame
	 */
	public IRecordFrame getFrame()
	{
		return Rframe;
	}
	
	
	/**
	 * @param Rframe the frame to set
	 */
	public void setFrame(final IRecordFrame Rframe)
	{
		this.Rframe = Rframe;
	}
	
	
	private BotID checkBotID(final IRecordFrame frame, final int newBoitID)
	{
		BotID botID = null;
		
		List<BotID> botIds = new ArrayList<BotID>(frame.getWorldFrame().getTigerBotsAvailable().keySet());
		
		botID = BotID.createBotId(newBoitID, ETeamColor.YELLOW);
		
		if (botIds.contains(botID))
		{
			return botID;
		}
		
		Collections.sort(botIds);
		return botIds.get(0);
		
	}
}
