/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import net.miginfocom.swing.MigLayout;


/**
 * Visualizes all available robots.
 * 
 * @author bernhard
 */
public class RobotsPanel extends JPanel
{
	private static final long						serialVersionUID			= 6408342941543334436L;
	
	// --- constants ---
	private static final int						SIGN_WIDTH					= 40;
	private static final int						SIGN_HEIGHT					= 30;
	private static final int						SIGN_MARGIN					= 5;
	private static final int						SIGN_STRIP_WIDTH			= 5;
	private static final int						Y_START_MARGIN				= 1;
	private static final int						PANEL_WIDTH					= 50;
	private static final int						BAT_SIGN_WIDTH				= 8;
	
	// --- color ---
	private static final Color						SELECTED_COLOR				= Color.black;
	private static final Color						TRUE_COLOR					= Color.green;
	private static final Color						FALSE_COLOR					= Color.red;
	private static final Color						YELLOW_BOT_COLOR			= Color.yellow;
	private static final Color						BLUE_BOT_COLOR				= Color.blue;
	private static final Color						YELLOW_CONTRAST_COLOR	= Color.black;
	private static final Color						BLUE_CONTRAST_COLOR		= Color.white;
	
	// --- observer ---
	private final List<IRobotsPanelObserver>	observers					= new CopyOnWriteArrayList<>();
	private final List<Color>						colors						= new ArrayList<>();
	private int											signHeight					= SIGN_HEIGHT;
	
	// --- marker-position ---
	private BotID										selectedBot					= BotID.noBot();
	
	// --- connection arrays ---
	private Map<BotID, BotStatus>					botStati						= new ConcurrentSkipListMap<>();
	private Map<BotID, BotStatus>					oldBotStati					= new ConcurrentSkipListMap<>();
	private Map<BotID, List<String>>				brokenFeatureMap			= new ConcurrentSkipListMap<>();
	private Map<BotID, BotPanel>					robotButtonMap				= new ConcurrentSkipListMap<>();
	private boolean									flashState					= false;
	private boolean									flashState2					= false;
	private boolean									necessaryToRemoveAll		= false;
	
	// --- bot features ---
	private static final Set<EFeature>			botFeaturesToCheck		= new HashSet<>();
	
	static
	{
		botFeaturesToCheck.add(EFeature.BARRIER);
		botFeaturesToCheck.add(EFeature.CHARGE_CAPS);
		botFeaturesToCheck.add(EFeature.CHIP_KICKER);
		botFeaturesToCheck.add(EFeature.DRIBBLER);
		botFeaturesToCheck.add(EFeature.MOVE);
		botFeaturesToCheck.add(EFeature.STRAIGHT_KICKER);
	}
	
	
	/**
	 * sets color list and layout
	 */
	public RobotsPanel()
	{
		colors.add(new Color(0xE52F00));
		colors.add(new Color(0xDB9000));
		colors.add(new Color(0xBAD200));
		colors.add(new Color(0x58C800));
		colors.add(new Color(0x00BF02));
		
		// --- configure panel ---
		setLayout(new MigLayout("ins 1 0 1 0, gapy 2, wrap", "", "[top]"));
		setMinimumSize(new Dimension(PANEL_WIDTH + 5, signHeight));
		setPreferredSize(new Dimension(PANEL_WIDTH + 5, 2000));
		
		// --- swing timer for flashing ---
		Timer flashFast = new Timer(200, e -> {
			flashState = !flashState;
			for (BotPanel bp : robotButtonMap.values())
			{
				bp.flashStateChanged();
			}
		});
		flashFast.start();
		Timer flashSlow = new Timer(500, e -> {
			flashState2 = !flashState2;
			for (BotPanel bp : robotButtonMap.values())
			{
				bp.btn.flashStateChanged();
			}
		});
		flashSlow.start();
	}
	
	
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
	}
	
	
	/**
	 * deselects bot
	 */
	public void deselectRobots()
	{
		selectedBot = BotID.noBot();
	}
	
	
	/**
	 * compare last and new botStati and repaint if not equal
	 */
	public void updateBotStati()
	{
		if (!botStati.equals(oldBotStati))
		{
			repaint();
			saveOldBotStati();
		}
	}
	
	
	private void saveOldBotStati()
	{
		for (Map.Entry<BotID, BotStatus> entry : botStati.entrySet())
		{
			oldBotStati.computeIfAbsent(entry.getKey(), k -> new BotStatus());
			BotStatus old = oldBotStati.get(entry.getKey());
			BotStatus status = entry.getValue();
			
			old.setBatRel(status.getBatRel());
			old.setBotFeatures(status.getBotFeatures());
			old.setConnected(status.isConnected());
			old.setHideAi(status.isHideAi());
			old.setHideRcm(status.isHideRcm());
			old.setKickerRel(status.getKickerRel());
			old.setVisible(status.isVisible());
		}
	}
	
	
	@Override
	protected void paintComponent(final Graphics g1)
	{
		int fixedHeight = Y_START_MARGIN + (botStati.size() * SIGN_MARGIN) + SIGN_MARGIN;
		int availHeight = getHeight();
		if (botStati.isEmpty())
		{
			signHeight = availHeight - fixedHeight;
		} else
		{
			signHeight = (availHeight - fixedHeight) / botStati.size();
			signHeight = Math.min(signHeight, SIGN_HEIGHT);
		}
		
		// --- init work ---
		super.paintComponent(g1);
		
		// --- drawRobots ---
		addBots();
		
	}
	
	
	/**
	 * Generates List of buttons to draw existing bot panels on
	 * also adds labels of broken features underneath its respective button
	 */
	private void addBots()
	{
		
		for (Map.Entry<BotID, BotStatus> entry : botStati.entrySet())
		{
			if (!robotButtonMap.containsKey(entry.getKey()))
			{
				robotButtonMap.put(entry.getKey(), new BotPanel(entry.getKey(), entry.getValue()));
				necessaryToRemoveAll = true;
			}
			
			robotButtonMap.get(entry.getKey()).btn.updateBrokenFeatures();
			if (!entry.getValue().getBrokenFeatures().equals(brokenFeatureMap.get(entry.getKey())))
			{
				necessaryToRemoveAll = true;
				brokenFeatureMap.put(entry.getKey(), entry.getValue().getBrokenFeatures());
			}
		}
		if (necessaryToRemoveAll)
		{
			removeAll();
			ButtonGroup group = new ButtonGroup();
			for (Map.Entry<BotID, BotStatus> entry : botStati.entrySet())
			{
				add(robotButtonMap.get(entry.getKey()));
				group.add(robotButtonMap.get(entry.getKey()).btn);
				List<String> brokenFeatures = entry.getValue().getBrokenFeatures();
				for (String feat : brokenFeatures)
				{
					add(new ErrorMsg(feat));
				}
				brokenFeatureMap.put(entry.getKey(), entry.getValue().getBrokenFeatures());
			}
			validate();
			necessaryToRemoveAll = false;
		}
	}
	
	
	/**
	 * remove all robot panels
	 */
	public void clearView()
	{
		deselectRobots();
		botStati.clear();
		oldBotStati.clear();
		robotButtonMap.clear();
		removeAll();
		repaint();
	}
	
	
	/**
	 * @param botId
	 * @return existing BotStatus, or new one if not in Map
	 */
	public synchronized BotStatus getBotStatus(final BotID botId)
	{
		return botStati.computeIfAbsent(botId, k -> new BotStatus());
	}
	
	
	/**
	 * @return map of existing BotStati
	 */
	public synchronized Map<BotID, BotStatus> getBotStati()
	{
		return botStati;
	}
	
	/**
	 * RobotButton is used as placeholder for drawing the panel for each respective bot
	 * also contains mouseEvent listener for click events
	 */
	private class RobotButton extends JRadioButton
	{
		
		private BotID		botID	= BotID.noBot();
		private BotStatus	botStatus;
		
		
		public RobotButton(BotID id, BotStatus status)
		{
			botID = id;
			botStatus = status;
			setMargin(new Insets(0, 0, 0, 0));
			setMinimumSize(new Dimension(SIGN_WIDTH + 1, signHeight + 1));
			setPreferredSize(new Dimension(SIGN_WIDTH + 1, signHeight + 1));
			setContentAreaFilled(false);
			setBorder(new EmptyBorder(-1, -1, -1, -1));
			addActionListener(actionEvent -> {
				for (final IRobotsPanelObserver observer : observers)
				{
					observer.onRobotClick(botID);
				}
			});
			addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(final MouseEvent e)
				{
					selectMouseAction(e);
				}
			});
		}
		
		
		private void selectMouseAction(final MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON3)
			{
				final BotPopUpMenu botPopUpMenu = new BotPopUpMenu(botID, botStati.get(botID));
				for (IRobotsPanelObserver o : observers)
				{
					botPopUpMenu.addObserver(o);
				}
				botPopUpMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
		
		
		protected void flashStateChanged()
		{
			updateBrokenFeatures();
			if (!botStatus.getBrokenFeatures().isEmpty())
			{
				repaint();
			}
		}
		
		
		@Override
		protected void paintComponent(final Graphics g)
		{
			super.paintComponent(g);
			final Graphics2D g2 = (Graphics2D) g;
			drawRobot(g2);
		}
		
		
		/**
		 * draws fat border around chosen bot
		 *
		 * @param g
		 */
		private void drawMarker(final Graphics2D g)
		{
			if (this.isSelected() && selectedBot.equals(botID))
			{
				g.setColor(SELECTED_COLOR);
				g.setStroke(new BasicStroke(3));
				g.drawRect(0, 0, SIGN_WIDTH, signHeight);
				g.setStroke(new BasicStroke(1));
			}
		}
		
		
		/**
		 * used to set color of left column which shows if bot is visible
		 *
		 * @param on
		 * @param g
		 */
		private void setColorBoolean(final Boolean on, final Graphics2D g)
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
		
		
		private void setColorForId(final Graphics2D g, Color fontColor)
		{
			if (flashState2)
			{
				g.setColor(Color.red);
			} else
			{
				g.setColor(fontColor);
			}
		}
		
		
		private void drawRobot(final Graphics2D g)
		{
			ETeamColor color = botID.getTeamColor();
			int id = botID.getNumber();
			
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
			g.fillRect(0, 0, SIGN_WIDTH, signHeight);
			
			// Border
			g.setColor(Color.black);
			g.drawRect(0, 0, SIGN_WIDTH, signHeight);
			
			// Detected by WP? --> left strip
			setColorBoolean(botStatus.isVisible(), g);
			g.fillRect(1, 1, SIGN_WIDTH / 6, signHeight - 1);
			
			// Connected? --> right strip
			g.setColor(botStatus.isConnected() ? TRUE_COLOR : FALSE_COLOR);
			g.fillRect(SIGN_WIDTH - SIGN_STRIP_WIDTH - 1, 1, SIGN_WIDTH / 6, signHeight - 1);
			
			updateBrokenFeatures();
			
			// Write id
			int fontSize = signHeight;
			g.setFont(new Font("Courier", Font.BOLD, fontSize));
			FontMetrics fontMetrics = g.getFontMetrics();
			int textWidth = fontMetrics.stringWidth(String.valueOf(id));
			int textHeight = fontMetrics.getMaxAscent();
			g.setColor(fontColor);
			if (!botStatus.getBrokenFeatures().isEmpty())
			{
				setColorForId(g, fontColor);
			}
			g.drawString(
					String.valueOf(id),
					(PANEL_WIDTH - textWidth - BAT_SIGN_WIDTH) / 2,
					signHeight - ((signHeight - textHeight) / 2) - 2);
			
			// grey out bot panel if bot is invisible
			if (botStatus.getRobotMode() == ERobotMode.IDLE)
			{
				g.setColor(new Color(150, 150, 150, 150));
				g.fillRect(0, 0, SIGN_WIDTH, signHeight);
			}
			drawMarker(g);
		}
		
		
		protected void updateBrokenFeatures()
		{
			List<String> brokenFeatures = new ArrayList<>();
			if (botStatus.isConnected())
			{
				Map<EFeature, EFeatureState> botFeatures = botStatus.getBotFeatures();
				for (EFeature feat : botFeaturesToCheck)
				{
					EFeatureState state = botFeatures.getOrDefault(feat, EFeatureState.UNKNOWN);
					if (state != EFeatureState.WORKING)
					{
						brokenFeatures.add(feat.getName());
					}
				}
			}
			botStatus.setBrokenFeatures(brokenFeatures);
			String tip = "";
			if (!brokenFeatures.isEmpty())
			{
				tip = "Broken: " + String.join(", ", brokenFeatures);
			}
			setToolTipText(tip);
		}
		
	}
	
	private class ErrorMsg extends JPanel
	{
		private String text;
		
		
		public ErrorMsg(String s)
		{
			this.text = s;
			setMinimumSize(new Dimension(PANEL_WIDTH + 5, 11));
		}
		
		
		@Override
		protected void paintComponent(final Graphics g)
		{
			super.paintComponent(g);
			final Graphics2D g2 = (Graphics2D) g;
			g2.setFont(new Font("Courier", Font.BOLD, 9));
			g2.setColor(Color.black);
			g2.drawString(text, 0, 9);
		}
	}
	
	private class BotPanel extends JPanel
	{
		protected RobotButton	btn;
		private BotStatus			botStatus;
		
		
		public BotPanel(BotID id, BotStatus status)
		{
			botStatus = status;
			btn = new RobotButton(id, status);
			setLayout(new MigLayout("ins 0, gapy 0, gapx 0", "", ""));
			setMinimumSize(new Dimension(PANEL_WIDTH + 5, signHeight));
			
			add(btn);
		}
		
		
		protected void flashStateChanged()
		{
			if (botStatus.getKickerRel() < 0.2 || botStatus.getBatRel() < 0.1)
			{
				repaint();
			}
		}
		
		
		@Override
		protected void paintComponent(final Graphics g)
		{
			super.paintComponent(g);
			final Graphics2D g1 = (Graphics2D) g;
			drawSymbols(g1);
		}
		
		
		/**
		 * draws battery symbol and kicker symbol for given Bot
		 *
		 * @param g
		 */
		private void drawSymbols(final Graphics2D g)
		{
			int barHeight = signHeight / 2 - 3;
			int barX = PANEL_WIDTH - BAT_SIGN_WIDTH + 3;
			int barY = 2;
			int barY2 = (barY + signHeight) - (barHeight) - 1;
			
			if (botStatus.isConnected())
			{
				// battery
				if (botStatus.getBatRel() < 0.1)
				{
					if (flashState)
					{
						g.setColor(Color.red);
					} else
					{
						g.setColor(Color.black);
					}
					g.fillRect(barX, barY, BAT_SIGN_WIDTH, barHeight);
				} else
				{
					g.setColor(getChargeColor(botStatus.getBatRel()));
					g.fillRect(barX, barY + barHeight - (int) (barHeight * botStatus.getBatRel()),
							BAT_SIGN_WIDTH, (int) (barHeight * botStatus.getBatRel()));
				}
				
				// kicker
				if (botStatus.getKickerRel() < .2)
				{
					if (flashState)
					{
						g.setColor(Color.red);
					} else
					{
						g.setColor(Color.black);
					}
					g.fillArc(barX, barY2, BAT_SIGN_WIDTH + 1, BAT_SIGN_WIDTH + 1,
							90, 360);
				} else
				{
					g.setColor(getChargeColor(botStatus.getKickerRel()));
					g.fillArc(barX, barY2, BAT_SIGN_WIDTH + 1, BAT_SIGN_WIDTH + 1, 90,
							(int) (360 * botStatus.getKickerRel()));
				}
				g.setColor(Color.black);
				g.drawArc(barX, barY2, BAT_SIGN_WIDTH + 1, BAT_SIGN_WIDTH + 1, 90, 360);
			}
			
			// battery symbol:
			g.setColor(Color.black);
			g.drawRect(barX, barY, BAT_SIGN_WIDTH, barHeight);
			g.drawRect(barX + BAT_SIGN_WIDTH / 4, barY - 2, BAT_SIGN_WIDTH / 2, 2);
			
		}
		
		
		/**
		 * returns color respective to charge of battery or kicker, for drawing symbols
		 *
		 * @param relValue
		 * @return Color out of List
		 */
		private Color getChargeColor(final double relValue)
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
	}
}
