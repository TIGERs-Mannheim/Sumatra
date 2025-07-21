/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.botcenter.view;

import edu.tigers.sumatra.gui.botcenter.view.bots.TigerBotSummaryPanel;
import edu.tigers.sumatra.ids.BotID;
import net.miginfocom.swing.MigLayout;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;


/**
 * Overview of all bots.
 *
 * @author AndreR
 */
public class BotCenterOverviewPanel extends JPanel
{
	private static final long serialVersionUID = -3183090653608159807L;
	private final Map<BotID, TigerBotSummaryPanel> botPanels = new ConcurrentSkipListMap<>(BotID.getComparator());
	private boolean active = false;


	public BotCenterOverviewPanel()
	{
		setLayout(new MigLayout("fill", "", ""));

		setActive(false);
	}


	/**
	 * @param active
	 */
	public void setActive(final boolean active)
	{
		this.active = active;

		updatePanels();
	}


	/**
	 * @param botID
	 * @param panel
	 */
	public void addBotPanel(final BotID botID, final TigerBotSummaryPanel panel)
	{
		botPanels.put(botID, panel);
		updatePanels();
	}


	/**
	 * @param botID
	 */
	public void removeBotPanel(final BotID botID)
	{
		TigerBotSummaryPanel panel = botPanels.get(botID);
		if (panel != null)
		{
			panel.setEnabled(false);
		}

		updatePanels();
	}


	/**
	 * @param botId
	 * @return
	 */
	public TigerBotSummaryPanel getBotPanel(final BotID botId)
	{
		return botPanels.get(botId);
	}


	private void updatePanels()
	{
		removeAll();

		if (active && !botPanels.isEmpty())
		{
			botPanels.values().stream()
					.filter(JPanel::isEnabled)
					.forEach(p -> add(p, "wrap, gapbottom 0"));
		} else
		{
			add(new JLabel("No bots connected."), "wrap");
		}

		add(Box.createGlue(), "push");
		SwingUtilities.updateComponentTreeUI(this);
	}
}
