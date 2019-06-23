/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Aug 21, 2010
 * Author(s): bernhard
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.AObjectID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IManualBotObserver;


/**
 * Visualizes all available robots.
 * 
 * @author bernhard
 * 
 */
public class RobotsPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long						serialVersionUID			= 6408342941543334436L;
	
	// --- constants ---
	private static final int						SIGN_WIDTH					= 40;
	private static final int						SIGN_HEIGHT					= 20;
	private static final int						SIGN_MARGIN					= 10;
	private static final int						SIGN_STRIP_WIDTH			= 5;
	private static final int						Y_START_MARGIN				= 25;
	private static final int						PANEL_WIDTH					= 65;
	
	// --- observer ---
	private final List<IRobotsPanelObserver>	observers					= new ArrayList<IRobotsPanelObserver>();
	private final List<IManualBotObserver>		manualBotObservers		= new ArrayList<IManualBotObserver>();
	
	// --- marker-position ---
	private static final int						NO_BOT_MARKED				= -1;
	private int											markerPosition				= NO_BOT_MARKED;
	
	// --- connection arrays ---
	
	/** Thread-safe guarded by <code>this</code> */
	private final Map<BotID, Boolean>			tigersWpDetected			= new HashMap<BotID, Boolean>();
	
	/** Thread-safe guarded by <code>this</code> */
	private final Map<BotID, Boolean>			foesWpDetected				= new HashMap<BotID, Boolean>();
	
	/** Thread-safe guarded by <code>this</code> */
	private final Map<BotID, ENetworkState>	botConnection				= new HashMap<BotID, ENetworkState>();
	
	// --- color ---
	private static final Color						UNKNOWN_TEAM				= Color.black;
	private static final Color						SELECTED_COLOR				= Color.black;
	
	private static final Color						TRUE_COLOR					= Color.green;
	private static final Color						FALSE_COLOR					= Color.red;
	private static final Color						CONNECTING_COLOR			= Color.cyan;
	private static final Color						MANUAL_COLOR				= Color.red;
	
	private Color										tigersColor					= UNKNOWN_TEAM;
	private Color										tigersContrastColor		= UNKNOWN_TEAM;
	
	private final Set<BotID>						manualControlledTigers	= new HashSet<BotID>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public RobotsPanel()
	{
		// --- configure panel ---
		setLayout(new MigLayout("fill", "[" + (PANEL_WIDTH - (2 * SIGN_MARGIN)) + "!]", "[top]"));
		setPreferredSize(new Dimension(70, ((SIGN_HEIGHT + SIGN_MARGIN + SIGN_STRIP_WIDTH) * 13)));
		
		// --- border ---
		final TitledBorder border = BorderFactory.createTitledBorder("robots");
		border.setTitleJustification(TitledBorder.CENTER);
		setBorder(border);
		
		// --- add listener ---
		final MouseEvents me = new MouseEvents();
		addMouseListener(me);
	}
	
	
	/**
	 * @param tigersAreYellow
	 */
	public void setTigersAreYellow(boolean tigersAreYellow)
	{
		if (tigersAreYellow)
		{
			tigersColor = Color.yellow;
			tigersContrastColor = Color.black;
		} else
		{
			tigersColor = Color.blue;
			tigersContrastColor = Color.white;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- observer -------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param o
	 */
	public void addObserver(IRobotsPanelObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(IRobotsPanelObserver o)
	{
		observers.remove(o);
	}
	
	
	/**
	 * @param o
	 */
	public void addObserver(IManualBotObserver o)
	{
		manualBotObservers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(IManualBotObserver o)
	{
		manualBotObservers.remove(o);
	}
	
	
	// --------------------------------------------------------------------------
	// --- select methods -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param i
	 */
	public synchronized void selectRobot(int i)
	{
		markerPosition = i;
	}
	
	
	/**
	 */
	public synchronized void deselectRobots()
	{
		markerPosition = NO_BOT_MARKED;
	}
	
	
	// --------------------------------------------------------------------------
	// --- paint method ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void paintComponent(Graphics g1)
	{
		// --- init work ---
		super.paintComponent(g1);
		final Graphics2D g = (Graphics2D) g1;
		
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
		public void mouseClicked(MouseEvent e)
		{
			final int clickX = (e.getX());
			final int clickY = (e.getY() - 25);
			
			// --- calculate BotId ---
			int botId = NO_BOT_MARKED;
			final int startcoordinateX = (PANEL_WIDTH - SIGN_WIDTH) / 2;
			
			// --- check x-coordinate ---
			if ((clickX <= (startcoordinateX + SIGN_WIDTH)) && (clickX >= startcoordinateX))
			{
				// --- determinate y-coordinate ---
				botId = clickY / (SIGN_HEIGHT + SIGN_MARGIN);
				// log.info("clicked botId: " + botId);
				
				// --- check if click is within a rect + botId is between 1 and 12 ---
				if ((botId >= AObjectID.BOT_ID_MIN) && (botId <= AObjectID.BOT_ID_MAX)
						&& (clickY >= ((SIGN_HEIGHT + SIGN_MARGIN) * (botId)))
						&& (clickY <= (((SIGN_HEIGHT + SIGN_MARGIN) * (botId)) + SIGN_HEIGHT)))
				{
					// log.debug("clicked: " + clickX + "," + clickY + " - " + botId);
					
					if (e.getButton() == MouseEvent.BUTTON3)
					{
						final int finalBotId = botId;
						final BotPopUpMenu botPopUpMenu = new BotPopUpMenu();
						if (manualControlledTigers.contains(new BotID(finalBotId)))
						{
							botPopUpMenu.iManual.setSelected(true);
						}
						botPopUpMenu.iManual.addActionListener(new ActionListener()
						{
							
							@Override
							public void actionPerformed(ActionEvent e)
							{
								// --- notify observer ---
								synchronized (manualBotObservers)
								{
									if (((JCheckBoxMenuItem) e.getSource()).isSelected())
									{
										manualControlledTigers.add(new BotID(finalBotId));
										for (final IManualBotObserver observer : manualBotObservers)
										{
											observer.onManualBotAdded(new BotID(finalBotId));
										}
									} else
									{
										manualControlledTigers.remove(new BotID(finalBotId));
										for (final IManualBotObserver observer : manualBotObservers)
										{
											observer.onManualBotRemoved(new BotID(finalBotId));
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
								observer.onRobotClick(new BotID(botId));
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
	
	private synchronized void drawMarker(Graphics2D g)
	{
		final int marker = markerPosition;
		if (marker != NO_BOT_MARKED)
		{
			g.setColor(SELECTED_COLOR);
			g.setStroke(new BasicStroke(3));
			g.drawRect((PANEL_WIDTH - SIGN_WIDTH) / 2, (marker * (SIGN_HEIGHT + SIGN_MARGIN)) + Y_START_MARGIN,
					SIGN_WIDTH + 1, SIGN_HEIGHT + 1);
			g.setStroke(new BasicStroke(1));
		}
	}
	
	
	private synchronized void drawRobots(Graphics2D g)
	{
		for (int i = AObjectID.BOT_ID_MIN; i <= AObjectID.BOT_ID_MAX; i++)
		{
			Color fontColor = null;
			
			final BotID botId = new BotID(i);
			
			// Bot-color
			if (is(tigersWpDetected.get(botId)))
			{
				g.setColor(tigersColor);
				fontColor = tigersContrastColor;
			} else
			{
				g.setColor(UNKNOWN_TEAM);
				fontColor = Color.white;
			}
			// if manual controlled, reset color
			if (manualControlledTigers.contains(botId))
			{
				fontColor = MANUAL_COLOR;
			}
			
			// Draw bot-panel
			g.fillRect((PANEL_WIDTH - SIGN_WIDTH) / 2, (i * (SIGN_HEIGHT + SIGN_MARGIN)) + Y_START_MARGIN, SIGN_WIDTH,
					SIGN_HEIGHT);
			
			// ???Border???
			g.setColor(Color.black);
			g.drawRect((PANEL_WIDTH - SIGN_WIDTH) / 2, (i * (SIGN_HEIGHT + SIGN_MARGIN)) + Y_START_MARGIN, SIGN_WIDTH,
					SIGN_HEIGHT);
			
			// Detected by WP?
			setColor(tigersWpDetected.get(botId), g);
			g.fillRect(((PANEL_WIDTH - SIGN_WIDTH) / 2) + 1, (i * (SIGN_HEIGHT + SIGN_MARGIN)) + 1 + Y_START_MARGIN,
					SIGN_WIDTH / 6, SIGN_HEIGHT - 1);
			
			// Connected?
			setConnectionColor(botConnection.get(botId), g);
			g.fillRect((((PANEL_WIDTH - SIGN_WIDTH) / 2) + SIGN_WIDTH) - SIGN_STRIP_WIDTH - 1,
					(i * (SIGN_HEIGHT + SIGN_MARGIN)) + 1 + Y_START_MARGIN, SIGN_WIDTH / 6, SIGN_HEIGHT - 1);
			
			// Write id
			g.setFont(new Font("Courier", Font.BOLD, 16));
			g.setColor(fontColor);
			if (String.valueOf(i).length() == 1)
			{
				g.drawString(String.valueOf(i), ((PANEL_WIDTH - SIGN_WIDTH) / 2) + 15, (i * (SIGN_HEIGHT + SIGN_MARGIN))
						+ 17 + Y_START_MARGIN);
			} else if (String.valueOf(i).length() == 2)
			{
				g.drawString(String.valueOf(i), ((PANEL_WIDTH - SIGN_WIDTH) / 2) + 10, (i * (SIGN_HEIGHT + SIGN_MARGIN))
						+ 17 + Y_START_MARGIN);
			}
		}
	}
	
	
	private void setColor(Boolean on, Graphics2D g)
	{
		if (is(on))
		{
			g.setColor(TRUE_COLOR);
		} else
		{
			g.setColor(FALSE_COLOR);
		}
	}
	
	
	private void setConnectionColor(ENetworkState state, Graphics2D g)
	{
		if (state == null)
		{
			g.setColor(FALSE_COLOR);
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
	
	
	private boolean is(Boolean on)
	{
		return (on != null) && on;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter methods ------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param id
	 * @param state
	 */
	public synchronized void setBotConnected(BotID id, ENetworkState state)
	{
		botConnection.put(id, state);
	}
	
	
	/**
	 * @param id
	 * @param detected
	 */
	public synchronized void setTigerDetected(BotID id, boolean detected)
	{
		tigersWpDetected.put(id, detected);
	}
	
	
	/**
	 * @param id
	 * @param detected
	 */
	public synchronized void setFoeDetected(BotID id, boolean detected)
	{
		foesWpDetected.put(id, detected);
	}
	
	
	/**
	 * @param id
	 * @return
	 */
	public synchronized boolean isFoeDetected(BotID id)
	{
		return is(foesWpDetected.get(id));
	}
	
	
	/**
	 * @param id
	 * @return
	 */
	public synchronized boolean isTigerDetected(BotID id)
	{
		return is(tigersWpDetected.get(id));
	}
	
	
	/**
	 */
	public synchronized void clearDetections()
	{
		tigersWpDetected.clear();
		foesWpDetected.clear();
	}
	
	
	/**
	 * @param id
	 * @return
	 */
	public synchronized boolean isBotConnected(BotID id)
	{
		return botConnection.get(id) == ENetworkState.ONLINE;
	}
	
	
	/**
	 */
	public synchronized void clearConnections()
	{
		botConnection.clear();
	}
	
	
	/**
	 */
	public synchronized void clearView()
	{
		clearConnections();
		clearDetections();
		deselectRobots();
	}
}
