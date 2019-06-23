/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 2, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;


/**
 * This layer shows status information about each bot (e.g. bars for akku, kicker,..)
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class BotStatusLayer extends AFieldLayer
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final int		BAR_WIDTH	= 18;
	private static final int		BAR_HEIGHT	= 2;
	private static final Stroke	STROKE		= new BasicStroke(1);
	
	private final List<Color>		colors		= new ArrayList<Color>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 */
	public BotStatusLayer()
	{
		super(EFieldLayer.BOT_STATUS);
		colors.add(new Color(0xE52F00));
		colors.add(new Color(0xDB9000));
		colors.add(new Color(0xBAD200));
		colors.add(new Color(0x58C800));
		colors.add(new Color(0x00BF02));
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void paintLayerSwf(Graphics2D g, SimpleWorldFrame frame)
	{
		g.setStroke(STROKE);
		for (TrackedTigerBot bot : frame.getBots().values())
		{
			ABot aBot = bot.getBot();
			
			if ((aBot != null) && (aBot.getNetworkState() == ENetworkState.ONLINE))
			{
				IVector2 pos = bot.getPos();
				IVector2 gPos = getFieldPanel().transformToGuiCoordinates(pos);
				float battery = aBot.getBatteryLevel();
				float batMin = aBot.getBatteryLevelMin();
				float batMax = aBot.getBatteryLevelMax();
				float kicker = aBot.getKickerLevel();
				
				drawBar(g, (int) gPos.x(), (int) gPos.y() - 10, Math.max(0, (battery - batMin) / (batMax - batMin)));
				drawBar(g, (int) gPos.x(), ((int) gPos.y() - 10) - BAR_HEIGHT, ((kicker / 200) < 0.5 ? -1 : 1));
			}
		}
	}
	
	
	/**
	 * Draw a bar
	 * 
	 * @param g
	 * @param xCenter center of the bar on x axis
	 * @param yTop top y-coordinate
	 * @param relValue
	 */
	private void drawBar(Graphics2D g, int xCenter, int yTop, float relValue)
	{
		int xLeft = xCenter - (BAR_WIDTH / 2);
		g.setColor(Color.black);
		g.fillRect(xLeft, yTop, BAR_WIDTH, BAR_HEIGHT);
		g.setColor(getColor(relValue));
		// g.fillRect(xLeft, yTop, (int) (BAR_WIDTH * Math.abs(relValue)), BAR_HEIGHT);
		g.fillRect(xLeft, yTop, BAR_WIDTH, BAR_HEIGHT);
	}
	
	
	private Color getColor(float relValue)
	{
		float step = 1f / colors.size();
		for (int i = 0; i < colors.size(); i++)
		{
			float val = (i + 1) * step;
			if (relValue <= val)
			{
				return colors.get(i);
			}
		}
		return Color.black;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
