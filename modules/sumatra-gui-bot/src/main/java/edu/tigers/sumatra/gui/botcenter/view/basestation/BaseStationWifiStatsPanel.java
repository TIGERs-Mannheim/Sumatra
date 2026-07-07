package edu.tigers.sumatra.gui.botcenter.view.basestation;

import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStatsV2;
import edu.tigers.sumatra.botmanager.commands.basestation.BaseStationWifiStatsV2.BotStats;
import edu.tigers.sumatra.ids.BotID;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


/**
 * Show Base Station Wifi stats
 *
 * @author AndreR
 */
public class BaseStationWifiStatsPanel extends JPanel
{

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = -9048719448114258641L;

	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Map<BotID, WifiStatsPanel> wifiPanels = new TreeMap<>(BotID.getComparator());
	private final WifiStatsHeaderPanel headerPanel = new WifiStatsHeaderPanel();
	private final JTextField updateRate = new JTextField();
	private final JLabel updateRateText = new JLabel("Update Rate:");


	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------


	/**
	 * Default constructor.
	 */
	public BaseStationWifiStatsPanel()
	{
		setLayout(new MigLayout("wrap 1"));

		setBorder(BorderFactory.createTitledBorder("Wireless"));

		updatePanels();
	}


	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	private void updatePanels()
	{
		final JPanel thisPanel = this;

		removeAll();

		add(updateRateText, "split 2");
		add(updateRate, "width 100");

		if (!wifiPanels.isEmpty())
		{
			add(headerPanel);

			for (final JPanel panel : wifiPanels.values())
			{
				add(panel, "wrap, gapbottom 0");
			}
		} else
		{
			add(new JLabel("No bots connected."), "wrap");
		}

		add(Box.createGlue(), "push");

		SwingUtilities.updateComponentTreeUI(thisPanel);
	}


	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------


	/**
	 * @param stats
	 */
	public void setStats(final BaseStationWifiStatsV2 stats)
	{
		boolean panelsModified = false;

		updateRate.setText(String.format("%.1fHz", stats.getUpdateRate()));

		for (BotID id : new ArrayList<>(wifiPanels.keySet()))
		{
			if (!stats.isBotConnected(id))
			{
				wifiPanels.remove(id);
				panelsModified = true;
			}
		}

		for (int i = 0; i < BaseStationWifiStatsV2.NUM_BOTS; i++)
		{
			BotStats botStats = stats.getBotStats()[i];
			if (botStats.getBotId().isUninitializedID())
			{
				continue;
			}

			if (!wifiPanels.containsKey(botStats.getBotId()))
			{
				wifiPanels.put(botStats.getBotId(), new WifiStatsPanel());
				panelsModified = true;
			}

			wifiPanels.get(botStats.getBotId()).setStats(botStats, stats.getModuleStats());
		}

		if (panelsModified)
		{
			updatePanels();
		}
	}


	private static class WifiStatsHeaderPanel extends JPanel
	{
		/**
		 *
		 */
		@Serial
		private static final long serialVersionUID = -3542692054124461579L;


		public WifiStatsHeaderPanel()
		{
			setLayout(new MigLayout("wrap 6", "[20,fill]5[20]20[80,fill]5[80,fill]20[80,fill]5[80,fill]"));

			add(new JLabel("--- To Bot ---", SwingConstants.CENTER), "skip 2, span 2");
			add(new JLabel("--- From Bot ---", SwingConstants.CENTER), "span 2");
			add(new JLabel("Pkt. / Bytes", SwingConstants.CENTER), "skip 2");
			add(new JLabel("Loss", SwingConstants.CENTER));
			add(new JLabel("Pkt. / Bytes", SwingConstants.CENTER));
			add(new JLabel("Loss", SwingConstants.CENTER));
		}
	}

	private static class WifiStatsPanel extends JPanel
	{
		/**
		 *
		 */
		@Serial
		private static final long serialVersionUID = 9205204622066410833L;

		private final JProgressBar[] rfStats = new JProgressBar[4];
		private final JLabel[] queueStats = new JLabel[2];
		private final JProgressBar linkQuality;

		private final JLabel id;


		public WifiStatsPanel()
		{
			setLayout(new MigLayout("wrap 6", "[20,fill]5[20]20[80,fill]5[80,fill]20[80,fill]5[80,fill]"));

			linkQuality = new JProgressBar(SwingConstants.VERTICAL, 0, 1000);
			linkQuality.setStringPainted(true);
			linkQuality.setSize(20, 30);

			id = new JLabel("None", SwingConstants.CENTER);
			id.setFont(id.getFont().deriveFont(20.0f));

			add(id, "span 1 2");
			add(linkQuality, "span 1 2, h 40px");

			for (int i = 0; i < 4; i++)
			{
				rfStats[i] = new JProgressBar(0, 1000);
				rfStats[i].setStringPainted(true);
				add(rfStats[i]);
			}

			for (int i = 0; i < 2; i++)
			{
				queueStats[i] = new JLabel("", SwingConstants.CENTER);
			}

			add(queueStats[0], "span 2 1");
			add(queueStats[1], "span 2 1");
		}


		public void setStats(BotStats stats, BaseStationWifiStatsV2.ModuleStats moduleStats)
		{
			doSetStats(stats, moduleStats);
		}


		@SuppressWarnings("squid:S1192")
		private void doSetStats(BotStats stats, BaseStationWifiStatsV2.ModuleStats moduleStats)
		{
			rfStats[0].setValue((int) ((stats.buf.txBytesTotal / moduleStats.getBytesPerSecondPerRobotSlot()) * 1000));
			rfStats[1].setValue((int) (stats.getTxLoss() * 1000));
			rfStats[2].setValue((int) ((stats.buf.rxBytesTotal / moduleStats.getBytesPerSecondPerRobotSlot()) * 1000));
			rfStats[3].setValue((int) (stats.getRxLoss() * 1000));

			rfStats[0].setString(String.format("%5d / %5d", stats.buf.txPacketsTotal, stats.buf.txBytesTotal));
			rfStats[1].setString(String.format(Locale.ENGLISH, "%5.2f%%", stats.getTxLoss() * 100));
			rfStats[2].setString(String.format("%5d / %5d", stats.buf.rxPacketsTotal, stats.buf.rxBytesTotal));
			rfStats[3].setString(String.format(Locale.ENGLISH, "%5.2f%%", stats.getRxLoss() * 100));

			long rxErrors = stats.buf.rxBufferOverruns + stats.buf.rxBufferDataOverflows + stats.buf.rxCrcErrors
					+ stats.buf.rxCobsDecodingErrors;

			queueStats[0].setText(String.format("TX Underrun: %5d", stats.buf.txBufferUnderrun));
			queueStats[1].setText(String.format("RX Buf Error: %5d", rxErrors));

			linkQuality.setValue((int) (stats.getLinkQuality() * 1000));
			linkQuality.setString(String.format(Locale.ENGLISH, "%3.0f%%", stats.getLinkQuality() * 100));

			if (stats.getBotId().isBot())
			{
				id.setForeground(stats.getBotId().getTeamColor().getColor());
				id.setText(Integer.toString(stats.getBotId().getNumber()));
			} else
			{
				setVisible(false);
			}
		}
	}
}
