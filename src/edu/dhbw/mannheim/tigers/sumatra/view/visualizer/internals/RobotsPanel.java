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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;


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
	private static final long				serialVersionUID		= 6408342941543334436L;
	
	private final Logger						log						= Logger.getLogger(getClass());
	
	// --- constants ---
	private final int							SIGN_WIDTH				= 40;
	private final int							SIGN_HEIGHT				= 20;
	private final int							SIGN_MARGIN				= 15;
	private final int							SIGN_STRIP_WIDTH		= 5;
	private final int							Y_START_MARGIN			= 25;
	private final int							PANEL_WIDTH				= 70;
	
	// --- observer ---
	private List<IRobotsPanelObserver>	observers				= new ArrayList<IRobotsPanelObserver>();
	
	// --- log ---
	// private Logger log = Logger.getLogger(getClass());
	
	// --- marker-position ---
	private static final int				NO_BOT_MARKED			= -1;
	private int									markerPosition			= NO_BOT_MARKED;
	
	// --- connection arrays ---
	private static final int				BOT_ID_MIN				= 0;
	private static final int				BOT_ID_MAX				= 11;
	
	/** Thread-safe guarded by <code>this</code> */
	private final Map<Integer, Boolean>	tigersWpDetected		= new HashMap<Integer, Boolean>();
	
	/** Thread-safe guarded by <code>this</code> */
	private final Map<Integer, Boolean>	foesWpDetected			= new HashMap<Integer, Boolean>();
	
	/** Thread-safe guarded by <code>this</code> */
	private final Map<Integer, ENetworkState>	botConnection	= new HashMap<Integer, ENetworkState>();
	
	// --- color ---
	private static final Color				UNKNOWN_TEAM			= Color.black;
	private static final Color				SELECTED_COLOR			= Color.black;
	
	private static final Color				TRUE_COLOR				= Color.green;
	private static final Color				FALSE_COLOR				= Color.red;
	private static final Color				CONNECTING_COLOR		= Color.cyan;
	
	private Color								tigersColor				= UNKNOWN_TEAM;
	private Color								tigersContrastColor	= UNKNOWN_TEAM;
	
	private Color								foeColor					= UNKNOWN_TEAM;
	private Color								foeContrastColor		= UNKNOWN_TEAM;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public RobotsPanel()
	{
		// --- configure panel ---
		setLayout(new MigLayout("fill", "", ""));
		
		// --- border ---
		TitledBorder border = BorderFactory.createTitledBorder("robots");
		border.setTitleJustification(TitledBorder.CENTER);
		setBorder(border);
		
		// --- add listener ---
		MouseEvents me = new MouseEvents();
		addMouseListener(me);
	}
	

	public void setTigersAreYellow(boolean tigersAreYellow)
	{
		if (tigersAreYellow)
		{
			tigersColor = Color.yellow;
			tigersContrastColor = Color.black;
			
			foeColor = Color.blue;
			foeContrastColor = Color.white;
		} else
		{
			tigersColor = Color.blue;
			tigersContrastColor = Color.white;
			
			foeColor = Color.yellow;
			foeContrastColor = Color.black;
		}
	}
	

	// --------------------------------------------------------------------------
	// --- observer -------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	public void addObserver(IRobotsPanelObserver o)
	{
		observers.add(o);
	}
	

	public void removeObserver(IRobotsPanelObserver o)
	{
		observers.remove(o);
	}
	

	// --------------------------------------------------------------------------
	// --- select methods -------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public synchronized void selectRobot(int i)
	{
		markerPosition = i;
	}
	

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
		Graphics2D g = (Graphics2D) g1;
		
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
			
			int clickX = (e.getX());
			int clickY = (e.getY() - 25);
			// log.info("y: " + e.getY());
			
			// --- TODO: calculate BotId ---
			int botId = NO_BOT_MARKED;
			int startcoordinateX = (PANEL_WIDTH - SIGN_WIDTH) / 2;
			

			// --- check x-coordinate ---
			if (clickX <= startcoordinateX + SIGN_WIDTH && clickX >= startcoordinateX)
			{
				// --- determinate y-coordinate ---
				botId = clickY / (SIGN_HEIGHT + SIGN_MARGIN);
				// log.info("clicked botId: " + botId);
				
				// --- check if click is within a rect + botId is between 1 and 12 ---
				if (botId >= BOT_ID_MIN && botId <= BOT_ID_MAX && clickY >= (SIGN_HEIGHT + SIGN_MARGIN) * (botId)
						&& clickY <= (SIGN_HEIGHT + SIGN_MARGIN) * (botId) + SIGN_HEIGHT)
				{
					// log.debug("clicked: " + clickX + "," + clickY + " - " + botId);
					
					// --- notify observer ---
					synchronized (observers)
					{
						for (IRobotsPanelObserver observer : observers)
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
	
	private synchronized void drawMarker(Graphics2D g)
	{
		final int marker = markerPosition;
		if (marker != NO_BOT_MARKED)
		{
			g.setColor(SELECTED_COLOR);
			g.setStroke(new BasicStroke(3));
			g.drawRect((PANEL_WIDTH - SIGN_WIDTH) / 2, marker * (SIGN_HEIGHT + SIGN_MARGIN) + Y_START_MARGIN,
					SIGN_WIDTH + 1, SIGN_HEIGHT + 1);
			g.setStroke(new BasicStroke(1));
		}
	}
	

	private synchronized void drawRobots(Graphics2D g)
	{
		for (int i = BOT_ID_MIN; i <= BOT_ID_MAX; i++)
		{
			Color fontColor = null;
			
			// Bot-color
			if (is(tigersWpDetected.get(i)))
			{
				g.setColor(tigersColor);
				fontColor = tigersContrastColor;
			} else if (is(foesWpDetected.get(i)))
			{
				g.setColor(foeColor);
				fontColor = foeContrastColor;
			} else
			{
				g.setColor(UNKNOWN_TEAM);
				fontColor = Color.white;
			}
			
			// Draw bot-panel
			g.fillRect((PANEL_WIDTH - SIGN_WIDTH) / 2, i * (SIGN_HEIGHT + SIGN_MARGIN) + Y_START_MARGIN, SIGN_WIDTH,
					SIGN_HEIGHT);
			
			// ???Border???
			g.setColor(Color.black);
			g.drawRect((PANEL_WIDTH - SIGN_WIDTH) / 2, i * (SIGN_HEIGHT + SIGN_MARGIN) + Y_START_MARGIN, SIGN_WIDTH,
					SIGN_HEIGHT);
			
			// Detected by WP?
			setColor(tigersWpDetected.get(i), g);
			g.fillRect((PANEL_WIDTH - SIGN_WIDTH) / 2 + 1, i * (SIGN_HEIGHT + SIGN_MARGIN) + 1 + Y_START_MARGIN,
					SIGN_WIDTH / 6, SIGN_HEIGHT - 1);
			
			// Connected?
			setConnectionColor(botConnection.get(i), g);
			g.fillRect((PANEL_WIDTH - SIGN_WIDTH) / 2 + SIGN_WIDTH - SIGN_STRIP_WIDTH - 1, i * (SIGN_HEIGHT + SIGN_MARGIN)
					+ 1 + Y_START_MARGIN, SIGN_WIDTH / 6, SIGN_HEIGHT - 1);
			
			// Write id
			g.setFont(new Font("Courier", Font.BOLD, 16));
			g.setColor(fontColor);
			if (String.valueOf(i).length() == 1)
			{
				g.drawString(String.valueOf(i), (PANEL_WIDTH - SIGN_WIDTH) / 2 + 15, i * (SIGN_HEIGHT + SIGN_MARGIN) + 17
						+ Y_START_MARGIN);
			} else if (String.valueOf(i).length() == 2)
			{
				g.drawString(String.valueOf(i), (PANEL_WIDTH - SIGN_WIDTH) / 2 + 10, i * (SIGN_HEIGHT + SIGN_MARGIN) + 17
						+ Y_START_MARGIN);
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
		return on != null && on;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter methods ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public synchronized void setBotConnected(int id, ENetworkState state)
	{
		if (id < BOT_ID_MIN || id > BOT_ID_MAX)
		{
			log.warn("BotId '" + id + "' is out of allowed range!");
			return;
		}
		botConnection.put(id, state);
	}
	

	public synchronized void setTigerDetected(int id, boolean detected)
	{
		if (id < BOT_ID_MIN || id > BOT_ID_MAX)
		{
			log.warn("Tiger with botId '" + id + "' is out of allowed range!");
			return;
		}
		tigersWpDetected.put(id, detected);
	}
	

	public synchronized void setFoeDetected(int id, boolean detected)
	{
		if (id < BOT_ID_MIN || id > BOT_ID_MAX)
		{
			// log.warn("Foe with botId '" + id + "' is out of allowed range!");
			return;
		}
		foesWpDetected.put(id, detected);
	}
	

	public synchronized boolean isFoeDetected(int id)
	{
		return is(foesWpDetected.get(id));
	}
	

	public synchronized boolean isTigerDetected(int id)
	{
		return is(tigersWpDetected.get(id));
	}
	

	public synchronized void clearDetections()
	{
		tigersWpDetected.clear();
		foesWpDetected.clear();
	}
	

	public synchronized boolean isBotConnected(int id)
	{
		return botConnection.get(id) == ENetworkState.ONLINE;
	}
	

	public synchronized void clearConnections()
	{
		botConnection.clear();
	}
	

	public synchronized void clearView()
	{
		clearConnections();
		clearDetections();
		deselectRobots();
	}
}
