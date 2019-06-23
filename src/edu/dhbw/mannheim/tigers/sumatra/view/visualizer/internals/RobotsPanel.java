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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IManualBotObserver;


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
	private static final int						SIGN_HEIGHT					= 15;
	private static final int						SIGN_MARGIN					= 5;
	private static final int						SIGN_STRIP_WIDTH			= 5;
	private static final int						Y_START_MARGIN				= 5;
	private static final int						PANEL_WIDTH					= 42;
	private int											signHeight					= SIGN_HEIGHT;
	
	// --- observer ---
	private final List<IRobotsPanelObserver>	observers					= new ArrayList<IRobotsPanelObserver>();
	private final List<IManualBotObserver>		manualBotObservers		= new ArrayList<IManualBotObserver>();
	
	// --- marker-position ---
	private static final int						NO_BOT_MARKED				= -1;
	private int											markerPosition				= NO_BOT_MARKED;
	
	// --- connection arrays ---
	private Map<BotID, ABot>						bots							= new HashMap<BotID, ABot>(0);
	private IBotIDMap<TrackedTigerBot>			tBots							= new BotIDMap<TrackedTigerBot>();
	
	// --- color ---
	private static final Color						SELECTED_COLOR				= Color.black;
	
	private static final Color						TRUE_COLOR					= Color.green;
	private static final Color						FALSE_COLOR					= Color.red;
	private static final Color						CONNECTING_COLOR			= Color.cyan;
	private static final Color						MANUAL_COLOR				= Color.red;
	
	private static final Color						YELLOW_BOT_COLOR			= Color.yellow;
	private static final Color						BLUE_BOT_COLOR				= Color.blue;
	
	private static final Color						YELLOW_CONTRAST_COLOR	= Color.black;
	private static final Color						BLUE_CONTRAST_COLOR		= Color.white;
	
	
	private final Set<BotID>						manualControlledTigers	= new HashSet<BotID>();
	
	private final List<Color>						colors						= new ArrayList<Color>();
	
	private long										flashLastTime				= System.nanoTime();
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
	 * @param o
	 */
	public void addObserver(final IManualBotObserver o)
	{
		manualBotObservers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(final IManualBotObserver o)
	{
		manualBotObservers.remove(o);
	}
	
	
	// --------------------------------------------------------------------------
	// --- select methods -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param botId
	 */
	public void selectRobot(final BotID botId)
	{
		int marker = 0;
		for (BotID id : bots.keySet())
		{
			if (id.equals(botId))
			{
				markerPosition = marker;
			}
			marker++;
		}
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
		float fixedHeight = Y_START_MARGIN + (bots.size() * SIGN_MARGIN) + SIGN_MARGIN;
		float availHeight = getHeight();
		if (bots.isEmpty())
		{
			signHeight = (int) (availHeight - fixedHeight);
		} else
		{
			signHeight = (int) (availHeight - fixedHeight) / bots.size();
		}
		
		// --- init work ---
		super.paintComponent(g1);
		final Graphics2D g = (Graphics2D) g1;
		
		if (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - flashLastTime) > 100)
		{
			flashLastTime = System.nanoTime();
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
				if (id >= bots.size())
				{
					return;
				}
				final BotID botId = bots.keySet().toArray(new BotID[bots.size()])[id];
				
				// --- check if click is within a rect + botId is between 1 and 12 ---
				if ((clickY >= ((signHeight + SIGN_MARGIN) * (id)))
						&& (clickY <= (((signHeight + SIGN_MARGIN) * (id)) + signHeight)))
				{
					if (e.getButton() == MouseEvent.BUTTON3)
					{
						final BotPopUpMenu botPopUpMenu = new BotPopUpMenu();
						if (manualControlledTigers.contains(botId))
						{
							botPopUpMenu.iManual.setSelected(true);
						}
						botPopUpMenu.iManual.addActionListener(new ActionListener()
						{
							
							@Override
							public void actionPerformed(final ActionEvent e)
							{
								// --- notify observer ---
								synchronized (manualBotObservers)
								{
									if (((JCheckBoxMenuItem) e.getSource()).isSelected())
									{
										manualControlledTigers.add(botId);
										for (final IManualBotObserver observer : manualBotObservers)
										{
											observer.onManualBotAdded(botId);
										}
									} else
									{
										manualControlledTigers.remove(botId);
										for (final IManualBotObserver observer : manualBotObservers)
										{
											observer.onManualBotRemoved(botId);
										}
									}
								}
							}
						});
						botPopUpMenu.show(e.getComponent(), e.getX(), e.getY());
					} else
					{
						// --- notify observer ---
						synchronized (observers)
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
		
		for (Map.Entry<BotID, ABot> entry : bots.entrySet())
		{
			final BotID botId = entry.getKey();
			final ABot bot = entry.getValue();
			ENetworkState networkState = bot.getNetworkState();
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
			
			// if manual controlled, reset color
			if (manualControlledTigers.contains(botId))
			{
				fontColor = MANUAL_COLOR;
			}
			
			// Draw bot-panel
			g.fillRect((PANEL_WIDTH - SIGN_WIDTH) / 2, ((i + offset) * (signHeight + SIGN_MARGIN)) + Y_START_MARGIN,
					SIGN_WIDTH, signHeight);
			
			// ???Border???
			g.setColor(Color.black);
			g.drawRect((PANEL_WIDTH - SIGN_WIDTH) / 2, ((i + offset) * (signHeight + SIGN_MARGIN)) + Y_START_MARGIN,
					SIGN_WIDTH, signHeight);
			
			// Detected by WP?
			setColor(tBots.containsKey(botId), g);
			g.fillRect(((PANEL_WIDTH - SIGN_WIDTH) / 2) + 1, ((i + offset) * (signHeight + SIGN_MARGIN)) + 1
					+ Y_START_MARGIN, SIGN_WIDTH / 6, signHeight - 1);
			
			// Connected?
			setConnectionColor(networkState, g);
			g.fillRect((((PANEL_WIDTH - SIGN_WIDTH) / 2) + SIGN_WIDTH) - SIGN_STRIP_WIDTH - 1,
					((i + offset) * (signHeight + SIGN_MARGIN)) + 1 + Y_START_MARGIN, SIGN_WIDTH / 6, signHeight - 1);
			
			if (networkState == ENetworkState.ONLINE)
			{
				float battery = bot.getBatteryLevel();
				float batMin = bot.getBatteryLevelMin();
				float batMax = bot.getBatteryLevelMax();
				
				if (bot.getType() == EBotType.TIGER_V2)
				{
					TigerBotV2 botV2 = (TigerBotV2) bot;
					if (botV2.getPowerLog().getU(1) <= 1e-6)
					{
						batMin = 10.5f;
						batMax = 12.6f;
					}
				}
				float kicker = bot.getKickerLevel();
				float batRel = Math.max(0, (battery - batMin) / (batMax - batMin));
				float kickerRel = (kicker / bot.getKickerLevelMax());
				
				int barWidth = SIGN_WIDTH - ((2 * SIGN_WIDTH) / 6);
				int barHeight = signHeight / 6;
				int barX = ((PANEL_WIDTH - SIGN_WIDTH) / 2) + 1 + (SIGN_WIDTH / 6);
				int barY = ((i + offset) * (signHeight + SIGN_MARGIN)) + 1 + Y_START_MARGIN;
				int barY2 = (barY + signHeight) - (signHeight / 6) - 1;
				
				// background
				g.setColor(Color.red);
				g.fillRect(barX, barY, barWidth, barHeight);
				g.fillRect(barX, barY2, barWidth, barHeight);
				
				// battery
				if (batRel < 0.2f)
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
			
			// Write id
			int fontSize = (int) (signHeight / 1.5f);
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
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter methods ------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 */
	public void clearView()
	{
		deselectRobots();
		bots.clear();
		tBots = new BotIDMap<TrackedTigerBot>();
	}
	
	
	/**
	 * @param bots the bots to set
	 */
	public void setBots(final Map<BotID, ABot> bots)
	{
		this.bots = bots;
	}
	
	
	/**
	 * @param tBots the tBots to set
	 */
	public void settBots(final IBotIDMap<TrackedTigerBot> tBots)
	{
		this.tBots = tBots;
	}
}
