/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 21, 2010
 * Author(s): bernhard
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * Visualizes all available robots.
 * 
 * @author bernhard
 */
public class RobotsPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long						serialVersionUID			= 6408342941543334436L;
	
	// --- constants ---
	private static final int						SIGN_WIDTH					= 40;
	private static final int						SIGN_HEIGHT					= 30;
	private static final int						SIGN_MARGIN					= 5;
	private static final int						SIGN_STRIP_WIDTH			= 5;
	private static final int						Y_START_MARGIN				= 1;
	private static final int						PANEL_WIDTH					= 42;
	private int											signHeight					= SIGN_HEIGHT;
	
	// --- observer ---
	private final List<IRobotsPanelObserver>	observers					= new CopyOnWriteArrayList<IRobotsPanelObserver>();
	
	// --- marker-position ---
	private static final int						NO_BOT_MARKED				= -1;
	private int											markerPosition				= NO_BOT_MARKED;
	
	// --- connection arrays ---
	private List<TrackedTigerBot>					tBots							= new ArrayList<TrackedTigerBot>(0);
	
	// --- color ---
	private static final Color						SELECTED_COLOR				= Color.black;
	
	private static final Color						TRUE_COLOR					= Color.green;
	private static final Color						FALSE_COLOR					= Color.red;
	private static final Color						CONNECTING_COLOR			= Color.cyan;
	
	private static final Color						YELLOW_BOT_COLOR			= Color.yellow;
	private static final Color						BLUE_BOT_COLOR				= Color.blue;
	
	private static final Color						YELLOW_CONTRAST_COLOR	= Color.black;
	private static final Color						BLUE_CONTRAST_COLOR		= Color.white;
	
	
	private final List<Color>						colors						= new ArrayList<Color>();
	
	private long										flashLastTime				= SumatraClock.nanoTime();
	private boolean									flashState					= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public RobotsPanel()
	{
		colors.add(new Color(0xE52F00));
		colors.add(new Color(0xDB9000));
		colors.add(new Color(0xBAD200));
		colors.add(new Color(0x58C800));
		colors.add(new Color(0x00BF02));
		
		// --- configure panel ---
		setLayout(new MigLayout("fill", "[" + (PANEL_WIDTH - (2 * SIGN_MARGIN)) + "!]", "[top]"));
		setPreferredSize(new Dimension(PANEL_WIDTH, 2000));
		
		// --- add listener ---
		final MouseEvents me = new MouseEvents();
		addMouseListener(me);
	}
	
	
	// --------------------------------------------------------------------------
	// --- observer -------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param o
	 */
	public void addObserver(final IRobotsPanelObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(final IRobotsPanelObserver o)
	{
		observers.remove(o);
	}
	
	
	/**
	 * @param botId
	 */
	public void selectRobot(final BotID botId)
	{
		int marker = 0;
		for (TrackedTigerBot tBot : tBots)
		{
			if (tBot.getId().equals(botId))
			{
				markerPosition = marker;
				break;
			}
			marker++;
		}
		repaint();
	}
	
	
	/**
	 */
	public void deselectRobots()
	{
		markerPosition = NO_BOT_MARKED;
	}
	
	
	// --------------------------------------------------------------------------
	// --- paint method ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void paintComponent(final Graphics g1)
	{
		float fixedHeight = Y_START_MARGIN + (tBots.size() * SIGN_MARGIN) + SIGN_MARGIN;
		float availHeight = getHeight();
		if (tBots.isEmpty())
		{
			signHeight = (int) (availHeight - fixedHeight);
		} else
		{
			signHeight = (int) (availHeight - fixedHeight) / tBots.size();
			signHeight = Math.min(signHeight, SIGN_HEIGHT);
		}
		
		// --- init work ---
		super.paintComponent(g1);
		final Graphics2D g = (Graphics2D) g1;
		
		if (TimeUnit.NANOSECONDS.toMillis(SumatraClock.nanoTime() - flashLastTime) > 100)
		{
			flashLastTime = SumatraClock.nanoTime();
			flashState = !flashState;
		}
		
		// --- drawRobots ---
		drawRobots(g);
		
		// --- drawMarker ---
		drawMarker(g);
	}
	
	
	// --------------------------------------------------------------
	// --- action listener ------------------------------------------
	// --------------------------------------------------------------
	protected class MouseEvents extends MouseAdapter
	{
		@Override
		public void mouseClicked(final MouseEvent e)
		{
			final int clickX = (e.getX());
			final int clickY = (e.getY() - Y_START_MARGIN);
			
			// --- calculate BotId ---
			int id = NO_BOT_MARKED;
			final int startcoordinateX = (PANEL_WIDTH - SIGN_WIDTH) / 2;
			
			// --- check x-coordinate ---
			if ((clickX <= (startcoordinateX + SIGN_WIDTH)) && (clickX >= startcoordinateX))
			{
				// --- determinate y-coordinate ---
				id = clickY / (signHeight + SIGN_MARGIN);
				if (id >= tBots.size())
				{
					return;
				}
				TrackedTigerBot tBot = tBots.get(id);
				final BotID botId = tBot.getId();
				
				// --- check if click is within a rect + botId is between 1 and 12 ---
				if ((clickY >= ((signHeight + SIGN_MARGIN) * (id)))
						&& (clickY <= (((signHeight + SIGN_MARGIN) * (id)) + signHeight)))
				{
					if (e.getButton() == MouseEvent.BUTTON3)
					{
						final BotPopUpMenu botPopUpMenu = new BotPopUpMenu(tBot);
						for (IRobotsPanelObserver o : observers)
						{
							botPopUpMenu.addObserver(o);
						}
						botPopUpMenu.show(e.getComponent(), e.getX(), e.getY());
					} else
					{
						for (final IRobotsPanelObserver observer : observers)
						{
							observer.onRobotClick(botId);
						}
					}
				}
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- draw methods ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private void drawMarker(final Graphics2D g)
	{
		final int marker = markerPosition;
		if (marker != NO_BOT_MARKED)
		{
			g.setColor(SELECTED_COLOR);
			g.setStroke(new BasicStroke(3));
			g.drawRect((PANEL_WIDTH - SIGN_WIDTH) / 2, (marker * (signHeight + SIGN_MARGIN)) + Y_START_MARGIN,
					SIGN_WIDTH + 1, signHeight + 1);
			g.setStroke(new BasicStroke(1));
		}
	}
	
	
	private void drawRobots(final Graphics2D g)
	{
		int offset = 0;
		int i = 0;
		
		for (TrackedTigerBot tBot : tBots)
		{
			final BotID botId = tBot.getId();
			ETeamColor color = botId.getTeamColor();
			int id = botId.getNumber();
			
			Color fontColor;
			if (color == ETeamColor.YELLOW)
			{
				g.setColor(YELLOW_BOT_COLOR);
				fontColor = YELLOW_CONTRAST_COLOR;
			} else
			{
				g.setColor(BLUE_BOT_COLOR);
				fontColor = BLUE_CONTRAST_COLOR;
			}
			
			// Draw bot-panel
			g.fillRect((PANEL_WIDTH - SIGN_WIDTH) / 2, ((i + offset) * (signHeight + SIGN_MARGIN)) + Y_START_MARGIN,
					SIGN_WIDTH, signHeight);
			
			// ???Border???
			g.setColor(Color.black);
			g.drawRect((PANEL_WIDTH - SIGN_WIDTH) / 2, ((i + offset) * (signHeight + SIGN_MARGIN)) + Y_START_MARGIN,
					SIGN_WIDTH, signHeight);
			
			// Detected by WP?
			setColor(tBot.isVisible(), g);
			g.fillRect(((PANEL_WIDTH - SIGN_WIDTH) / 2) + 1, ((i + offset) * (signHeight + SIGN_MARGIN)) + 1
					+ Y_START_MARGIN, SIGN_WIDTH / 6, signHeight - 1);
			
			final ABot bot = tBot.getBot();
			if (bot != null)
			{
				ENetworkState networkState = bot.getNetworkState();
				// Connected?
				setConnectionColor(networkState, g);
				g.fillRect((((PANEL_WIDTH - SIGN_WIDTH) / 2) + SIGN_WIDTH) - SIGN_STRIP_WIDTH - 1,
						((i + offset) * (signHeight + SIGN_MARGIN)) + 1 + Y_START_MARGIN, SIGN_WIDTH / 6, signHeight - 1);
				
				if (networkState == ENetworkState.ONLINE)
				{
					float kicker = bot.getKickerLevel();
					float batRel = bot.getBatteryRelative();
					float kickerRel = (kicker / bot.getKickerLevelMax());
					
					int barWidth = SIGN_WIDTH - ((2 * SIGN_WIDTH) / 6);
					int barHeight = signHeight / 4;
					int barX = ((PANEL_WIDTH - SIGN_WIDTH) / 2) + 1 + (SIGN_WIDTH / 6);
					int barY = ((i + offset) * (signHeight + SIGN_MARGIN)) + 1 + Y_START_MARGIN;
					int barY2 = (barY + signHeight) - (barHeight) - 1;
					
					// background
					g.setColor(Color.red);
					g.fillRect(barX, barY, barWidth, barHeight);
					g.fillRect(barX, barY2, barWidth, barHeight);
					
					// battery
					if (batRel < 0.1f)
					{
						if (flashState)
						{
							g.setColor(Color.red);
						} else
						{
							g.setColor(Color.black);
						}
						g.fillRect(barX, barY, (barWidth), barHeight);
					} else
					{
						g.setColor(getColor(batRel));
						g.fillRect(barX, barY, (int) (barWidth * batRel), barHeight);
					}
					
					// kicker
					if (kickerRel < .2f)
					{
						if (flashState)
						{
							g.setColor(Color.red);
						} else
						{
							g.setColor(Color.black);
						}
						g.fillRect(barX, barY2, (barWidth), barHeight);
					} else
					{
						g.setColor(getColor(kickerRel));
						g.fillRect(barX, barY2, (int) (barWidth * kickerRel), barHeight);
					}
				}
			}
			
			// Write id
			int fontSize = (int) (signHeight / 1f);
			g.setFont(new Font("Courier", Font.BOLD, fontSize));
			FontMetrics fontMetrics = g.getFontMetrics();
			int textWidth = fontMetrics.stringWidth(String.valueOf(id));
			int textHeight = fontMetrics.getMaxAscent();
			g.setColor(fontColor);
			g.drawString(
					String.valueOf(id),
					((PANEL_WIDTH - textWidth) / 2),
					(Y_START_MARGIN + ((((i + offset) * (signHeight + SIGN_MARGIN)) + signHeight) - ((signHeight - textHeight) / 2))) - 2);
			i++;
		}
	}
	
	
	private void setColor(final Boolean on, final Graphics2D g)
	{
		if (is(on))
		{
			g.setColor(TRUE_COLOR);
		} else
		{
			g.setColor(FALSE_COLOR);
		}
	}
	
	
	private void setConnectionColor(final ENetworkState state, final Graphics2D g)
	{
		if (state == null)
		{
			g.setColor(Color.black);
			return;
		}
		
		switch (state)
		{
			case ONLINE:
				g.setColor(TRUE_COLOR);
				break;
			
			case CONNECTING:
				g.setColor(CONNECTING_COLOR);
				break;
			
			case OFFLINE:
				g.setColor(FALSE_COLOR);
				break;
		}
	}
	
	
	private boolean is(final Boolean on)
	{
		return (on != null) && on;
	}
	
	
	private Color getColor(final float relValue)
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
		return Color.magenta;
	}
	
	
	/**
	 */
	public void clearView()
	{
		deselectRobots();
		tBots = new ArrayList<>(0);
		repaint();
	}
	
	
	/**
	 * @param tBotsMap the tBots to set
	 */
	public void settBots(final IBotIDMap<TrackedTigerBot> tBotsMap)
	{
		tBots = new CopyOnWriteArrayList<>(tBotsMap.values());
		Collections.sort(tBots, new ATrackedObject.TrackedObjectComparator());
		repaint();
	}
}
