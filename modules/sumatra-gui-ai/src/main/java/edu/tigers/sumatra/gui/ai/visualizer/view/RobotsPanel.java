/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.ai.visualizer.view;

import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.gui.visualizer.view.field.ISelectedRobotsChanged;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.Arc2D;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Visualizes all available robots.
 */
@Log4j2
public class RobotsPanel extends JPanel implements ISelectedRobotsChanged
{
	@Serial
	private static final long serialVersionUID = 6408342941543334436L;

	// --- constants ---
	private static final int SIGN_DIAMETER = 28;
	private static final int SIGN_WIDTH = 38;
	private static final int PANEL_WIDTH = 53;
	private static final int BAT_SIGN_WIDTH = 8;
	private static final int ERROR_MSG_HEIGHT = 11;

	// --- color ---
	private static final Color SELECTED_COLOR = Color.black;
	private static final Color YELLOW_BOT_COLOR = Color.yellow;
	private static final Color BLUE_BOT_COLOR = Color.blue;
	private static final Color YELLOW_CONTRAST_COLOR = Color.black;
	private static final Color BLUE_CONTRAST_COLOR = Color.white;

	// --- bot features ---
	private static final Set<EFeature> botFeaturesToCheck = new HashSet<>();

	static
	{
		botFeaturesToCheck.add(EFeature.BARRIER);
		botFeaturesToCheck.add(EFeature.CHARGE_CAPS);
		botFeaturesToCheck.add(EFeature.CHIP_KICKER);
		botFeaturesToCheck.add(EFeature.DRIBBLER);
		botFeaturesToCheck.add(EFeature.MOVE);
		botFeaturesToCheck.add(EFeature.STRAIGHT_KICKER);
		botFeaturesToCheck.add(EFeature.ENERGETIC);
	}

	@Setter
	@Accessors(fluent = true)
	private transient RobotSelectedCallback onRobotClicked = (a, b) -> {
	};
	private final List<Color> colors = new ArrayList<>();
	// --- marker-position ---
	@Getter
	private transient List<BotID> selectedBots = new CopyOnWriteArrayList<>();
	// --- connection arrays ---
	private transient Map<BotID, BotStatus> botStati = new ConcurrentSkipListMap<>();
	private transient Map<BotID, BotStatus> oldBotStati = new ConcurrentSkipListMap<>();
	private transient Map<BotID, List<String>> brokenFeatureMap = new ConcurrentSkipListMap<>();
	private transient Map<BotID, BotPanel> robotButtonMap = new ConcurrentSkipListMap<>();
	private boolean flashState = false;
	private boolean flashState2 = false;
	private boolean necessaryToRemoveAll = false;

	private transient Image flash;
	private transient Image visibleRed;
	private transient Image visibleGreen;
	private transient Image signalRed;
	private transient Image signalGreen;


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
		try
		{
			flash = ImageIO.read(getClass().getResourceAsStream("/flash.png"));
			signalRed = ImageIO.read(getClass().getResourceAsStream("/signal_red.png"));
			signalGreen = ImageIO.read(getClass().getResourceAsStream("/signal_green.png"));
			visibleRed = ImageIO.read(getClass().getResourceAsStream("/location_red.png"));
			visibleGreen = ImageIO.read(getClass().getResourceAsStream("/location_green.png"));
		} catch (IOException e)
		{
			log.trace(e);
		}
		// --- configure panel ---

		setLayout(new MigLayout("ins 1 0 1 0, gapy 2, wrap", "", "[top]"));
		setMinimumSize(new Dimension(PANEL_WIDTH, SIGN_DIAMETER));
		setPreferredSize(new Dimension(PANEL_WIDTH, 1200));

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
	 * @param botId
	 */
	public void selectRobot(final BotID botId)
	{
		if (!selectedBots.contains(botId))
		{
			selectedBots.add(botId);
		}
	}


	/**
	 * deselects bot
	 */
	public void deselectRobot(BotID botID)
	{
		selectedBots.remove(botID);
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
			old.setKickerRel(status.getKickerRel());
			old.setVisible(status.isVisible());
		}
		oldBotStati.entrySet().removeIf(e -> !botStati.containsKey(e.getKey()));
	}


	@Override
	protected void paintComponent(final Graphics g1)
	{
		// --- init work ---
		super.paintComponent(g1);

		// --- drawRobots ---
		addBots();
	}


	/**
	 * Generates List of buttons to draw existing bot panels on
	 * also adds labels of broken features underneath its respective button
	 */
	private synchronized void addBots()
	{
		updateButtonMap();
		if (necessaryToRemoveAll)
		{
			refreshButtonGroup();
			necessaryToRemoveAll = false;
		}
	}


	private void refreshButtonGroup()
	{
		removeAll();
		ButtonGroup group = new ButtonGroup();
		int numLines = 0;
		for (Map.Entry<BotID, BotStatus> entry : botStati.entrySet())
		{
			add(robotButtonMap.get(entry.getKey()));
			group.add(robotButtonMap.get(entry.getKey()).btn);
			if (entry.getValue().getRobotMode() != ERobotMode.IDLE)
			{
				List<String> brokenFeatures = entry.getValue().getBrokenFeatures();
				for (String feat : brokenFeatures)
				{
					add(new ErrorMsg(feat));
				}
				numLines += brokenFeatures.size() * ERROR_MSG_HEIGHT;
			}
			brokenFeatureMap.put(entry.getKey(), entry.getValue().getBrokenFeatures());
		}
		setPreferredSize(new Dimension(PANEL_WIDTH, robotButtonMap.size() * (SIGN_DIAMETER + 2) + numLines));
		validate();
	}


	private void updateButtonMap()
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
		int before = robotButtonMap.size();
		robotButtonMap.entrySet().removeIf(e -> !botStati.containsKey(e.getKey()));
		necessaryToRemoveAll = robotButtonMap.size() != before || necessaryToRemoveAll;
	}


	/**
	 * remove all robot panels
	 */
	public synchronized void clearView()
	{
		selectedBots.forEach(this::deselectRobot);
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
	public Map<BotID, BotStatus> getBotStati()
	{
		return botStati;
	}


	@Override
	public void selectedRobotsChanged(List<BotID> selectedBots)
	{
		this.selectedBots = selectedBots;
		repaint();
	}


	/**
	 * RobotButton is used as placeholder for drawing the panel for each respective bot
	 * also contains mouseEvent listener for click events
	 */
	private class RobotButton extends JRadioButton
	{

		private transient BotID botID;
		private transient BotStatus botStatus;


		public RobotButton(BotID id, BotStatus status)
		{
			botID = id;
			botStatus = status;
			setMargin(new Insets(0, 0, 0, 0));
			setMinimumSize(new Dimension(SIGN_WIDTH + 1, SIGN_DIAMETER + 1));
			setPreferredSize(new Dimension(SIGN_WIDTH + 1, SIGN_DIAMETER + 1));
			setContentAreaFilled(false);
			setBorder(new EmptyBorder(-1, -1, -1, -1));
			addActionListener(actionEvent -> onRobotClicked.robotSelected(actionEvent, botID));
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
		private void drawMarker(final Graphics2D g, Shape shape)
		{
			if (selectedBots.contains(botID))
			{
				g.setColor(SELECTED_COLOR);
				g.setStroke(new BasicStroke(3));
				g.draw(shape);
				g.setStroke(new BasicStroke(1));
			}
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
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

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

			Shape botShape = new Arc2D.Double(0, 0, SIGN_DIAMETER, SIGN_DIAMETER, 45,
					270, Arc2D.CHORD);
			// Draw bot-panel
			g.fill(botShape);

			// Border
			g.setColor(Color.black);
			g.draw(botShape);

			int size = SIGN_DIAMETER / 2;
			// Detected by filtered vision? --> location icon
			Image vis = botStatus.isVisible() ?
					visibleGreen.getScaledInstance(size, size, Image.SCALE_DEFAULT) :
					visibleRed.getScaledInstance(size, size, Image.SCALE_DEFAULT);
			g.drawImage(vis, SIGN_DIAMETER - 2, 0, null);

			// Connected? --> signal icon
			Image conn = botStatus.isConnected() ?
					signalGreen.getScaledInstance(size, size, Image.SCALE_DEFAULT) :
					signalRed.getScaledInstance(size, size, Image.SCALE_DEFAULT);
			g.drawImage(conn, SIGN_DIAMETER - 2, size, null);

			updateBrokenFeatures();

			// Write id
			int fontSize = size + 1;
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
					(SIGN_DIAMETER - textWidth) / 2,
					SIGN_DIAMETER - ((SIGN_DIAMETER - textHeight) / 2) - 2);

			// grey out bot panel if bot is invisible
			if (botStatus.getRobotMode() == ERobotMode.IDLE)
			{
				g.setColor(new Color(150, 150, 150, 150));
				g.fill(botShape);
			}
			drawMarker(g, botShape);
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
					if (state == EFeatureState.KAPUT)
					{
						brokenFeatures.add(feat.getName());
					}
				}
			}
			botStatus.setBrokenFeatures(brokenFeatures);
			String tip = null;
			if (!brokenFeatures.isEmpty())
			{
				tip = "Broken: " + String.join(", ", brokenFeatures);
			}
			setToolTipText(tip);
		}
	}

	private static class ErrorMsg extends JPanel
	{
		private String text;


		public ErrorMsg(String s)
		{
			this.text = s;
			setMinimumSize(new Dimension(PANEL_WIDTH, ERROR_MSG_HEIGHT));
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
		private RobotButton btn;
		private transient BotStatus botStatus;


		public BotPanel(BotID id, BotStatus status)
		{
			botStatus = status;
			btn = new RobotButton(id, status);
			setLayout(new MigLayout("ins 0, gapy 0, gapx 0", "", ""));
			setMinimumSize(new Dimension(PANEL_WIDTH, SIGN_DIAMETER));

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
			int barHeight = SIGN_DIAMETER / 2 - 3;
			int barX = PANEL_WIDTH - BAT_SIGN_WIDTH - 3;
			int barY = 2;
			int barX2 = barX - 1;
			int barY2 = SIGN_DIAMETER - barHeight - 1;

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
					g.fillArc(barX2, barY2, BAT_SIGN_WIDTH + 2, BAT_SIGN_WIDTH + 2,
							90, 360);
				} else
				{
					g.setColor(getChargeColor(botStatus.getKickerRel()));
					g.fillArc(barX2, barY2, BAT_SIGN_WIDTH + 2, BAT_SIGN_WIDTH + 2, 90,
							(int) (360 * botStatus.getKickerRel()));
				}
				g.setColor(Color.black);
				g.drawArc(barX2, barY2, BAT_SIGN_WIDTH + 2, BAT_SIGN_WIDTH + 2, 90, 360);
			}
			g.drawImage(flash.getScaledInstance(BAT_SIGN_WIDTH + 4, BAT_SIGN_WIDTH + 4, Image.SCALE_DEFAULT),
					barX2 - 1, barY2, null);

			// battery symbol:
			g.setColor(Color.black);
			g.drawRect(barX, barY, BAT_SIGN_WIDTH, barHeight);
			g.drawRect(barX + BAT_SIGN_WIDTH / 4, 0, BAT_SIGN_WIDTH / 2, 2);
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

	public interface RobotSelectedCallback
	{
		void robotSelected(ActionEvent actionEvent, BotID botID);
	}
}
