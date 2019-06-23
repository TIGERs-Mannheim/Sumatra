/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 21, 2010
 * Author(s): bernhard
 * *********************************************************
 */
package edu.tigers.sumatra.visualizer.view;

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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.visualizer.BotStatus;
import net.miginfocom.swing.MigLayout;


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
	private BotID										selectedBot					= BotID.get();
																							
	// --- connection arrays ---
	private Map<BotID, BotStatus>					botStati						= new ConcurrentSkipListMap<>();
																							
	// --- color ---
	private static final Color						SELECTED_COLOR				= Color.black;
																							
	private static final Color						TRUE_COLOR					= Color.green;
	private static final Color						FALSE_COLOR					= Color.red;
																							
	private static final Color						YELLOW_BOT_COLOR			= Color.yellow;
	private static final Color						BLUE_BOT_COLOR				= Color.blue;
																							
	private static final Color						YELLOW_CONTRAST_COLOR	= Color.black;
	private static final Color						BLUE_CONTRAST_COLOR		= Color.white;
																							
																							
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
	 * @param botId
	 */
	public void selectRobot(final BotID botId)
	{
		selectedBot = botId;
		repaint();
	}
	
	
	/**
	 */
	public void deselectRobots()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- paint method ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void paintComponent(final Graphics g1)
	{
		double fixedHeight = Y_START_MARGIN + (botStati.size() * SIGN_MARGIN) + SIGN_MARGIN;
		double availHeight = getHeight();
		if (botStati.isEmpty())
		{
			signHeight = (int) (availHeight - fixedHeight);
		} else
		{
			signHeight = (int) (availHeight - fixedHeight) / botStati.size();
			signHeight = Math.min(signHeight, SIGN_HEIGHT);
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
			final int startcoordinateX = (PANEL_WIDTH - SIGN_WIDTH) / 2;
			
			// --- check x-coordinate ---
			if ((clickX <= (startcoordinateX + SIGN_WIDTH)) && (clickX >= startcoordinateX))
			{
				// --- determinate y-coordinate ---
				int id = clickY / (signHeight + SIGN_MARGIN);
				if (id >= botStati.size())
				{
					return;
				}
				List<BotID> ids = new ArrayList<>(botStati.keySet());
				final BotID botId = ids.get(id);
				
				// --- check if click is within a rect + botId is between 1 and 12 ---
				if ((clickY >= ((signHeight + SIGN_MARGIN) * (id)))
						&& (clickY <= (((signHeight + SIGN_MARGIN) * (id)) + signHeight)))
				{
					if (e.getButton() == MouseEvent.BUTTON3)
					{
						final BotPopUpMenu botPopUpMenu = new BotPopUpMenu(botId, botStati.get(botId));
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
		int marker = 0;
		for (BotID botId : botStati.keySet())
		{
			if (botId.equals(selectedBot))
			{
				break;
			}
			marker++;
		}
		
		if (marker < botStati.size())
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
		
		for (Map.Entry<BotID, BotStatus> entry : botStati.entrySet())
		{
			final BotID botId = entry.getKey();
			BotStatus status = entry.getValue();
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
			setColor(status.isVisible(), g);
			g.fillRect(((PANEL_WIDTH - SIGN_WIDTH) / 2) + 1, ((i + offset) * (signHeight + SIGN_MARGIN)) + 1
					+ Y_START_MARGIN, SIGN_WIDTH / 6, signHeight - 1);
					
			// Connected?
			g.setColor(status.isConnected() ? TRUE_COLOR : FALSE_COLOR);
			g.fillRect((((PANEL_WIDTH - SIGN_WIDTH) / 2) + SIGN_WIDTH) - SIGN_STRIP_WIDTH - 1,
					((i + offset) * (signHeight + SIGN_MARGIN)) + 1 + Y_START_MARGIN, SIGN_WIDTH / 6, signHeight - 1);
					
			if (status.isConnected())
			{
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
				if (status.getBatRel() < 0.1)
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
					g.setColor(getColor(status.getBatRel()));
					g.fillRect(barX, barY, (int) (barWidth * status.getBatRel()), barHeight);
				}
				
				// kicker
				if (status.getKickerRel() < .2)
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
					g.setColor(getColor(status.getKickerRel()));
					g.fillRect(barX, barY2, (int) (barWidth * status.getKickerRel()), barHeight);
				}
			}
			
			// Write id
			int fontSize = (signHeight);
			g.setFont(new Font("Courier", Font.BOLD, fontSize));
			FontMetrics fontMetrics = g.getFontMetrics();
			int textWidth = fontMetrics.stringWidth(String.valueOf(id));
			int textHeight = fontMetrics.getMaxAscent();
			g.setColor(fontColor);
			g.drawString(
					String.valueOf(id),
					((PANEL_WIDTH - textWidth) / 2),
					(Y_START_MARGIN
							+ ((((i + offset) * (signHeight + SIGN_MARGIN)) + signHeight) - ((signHeight - textHeight) / 2)))
							- 2);
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
	
	
	private boolean is(final Boolean on)
	{
		return (on != null) && on;
	}
	
	
	private Color getColor(final double relValue)
	{
		double step = 1.0 / colors.size();
		for (int i = 0; i < colors.size(); i++)
		{
			double val = (i + 1) * step;
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
		botStati.clear();
		repaint();
	}
	
	
	/**
	 * @param botId
	 * @return
	 */
	public synchronized BotStatus getBotStatus(final BotID botId)
	{
		BotStatus status = botStati.get(botId);
		if (status == null)
		{
			status = new BotStatus();
			botStati.put(botId, status);
		}
		return status;
	}
}
