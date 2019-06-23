/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.offensive.view;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveAnalysedBotFrame;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveBotFrame;
import edu.tigers.sumatra.ids.BotID;
import net.miginfocom.swing.MigLayout;


/**
 * Strategy panel for one team.
 */
public class TeamOffensiveStatisticsPanel extends JPanel
{
	private static final long serialVersionUID = 5289153581163080891L;
	
	private JTextField desiredNumberText;
	private Map<BotID, OffensiveStatisticsBotPanel> botPanels = new HashMap<>();
	private JPanel botPanel = new JPanel(new MigLayout());
	private boolean currentFrameOnly = false;
	
	
	/**
	 * Team Panel
	 */
	public TeamOffensiveStatisticsPanel()
	{
		setLayout(new MigLayout());
		
		final JPanel numberPanel = new JPanel(new MigLayout("fill", "", ""));
		final JLabel numberLabel = new JLabel("Number of bots:");
		
		final JLabel desiredNumberLabel = new JLabel("desired.");
		desiredNumberText = new JTextField("0");
		desiredNumberText.setEditable(false);
		
		numberPanel.add(numberLabel);
		numberPanel.add(desiredNumberLabel);
		numberPanel.add(desiredNumberText);
		
		add(numberPanel, "wrap,span");
		final JScrollPane jp = new JScrollPane(botPanel);
		add(jp);
	}
	
	
	/**
	 * @param desired
	 */
	public void setMaxMinDesiredAVG(double desired)
	{
		desiredNumberText.setText(String.valueOf(desired));
	}
	
	
	/**
	 * @param desired
	 */
	public void setMaxMinDesired(double desired)
	{
		desiredNumberText.setText(String.valueOf(desired));
	}
	
	
	/**
	 * @param id
	 * @param val
	 */
	public void setPrimaryPercentage(BotID id, double val)
	{
		botPanels.get(id).updatePercentage(val);
	}
	
	
	/**
	 * @param id
	 * @param frame
	 */
	public void setBotFrame(BotID id, OffensiveAnalysedBotFrame frame)
	{
		if (!botPanels.containsKey(id))
		{
			botPanels.put(id, new OffensiveStatisticsBotPanel(id));
			botPanel.add(botPanels.get(id));
		}
		
		// update values here
		botPanels.get(id).updateBotFrame(frame);
	}
	
	
	public void setCurrentFrameOnly(final boolean currentFrameOnly)
	{
		this.currentFrameOnly = currentFrameOnly;
	}
	
	
	public boolean isCurrentFrameOnly()
	{
		return currentFrameOnly;
	}
	
	
	public void setBotSFrame(final BotID id, final OffensiveBotFrame frame)
	{
		if (!botPanels.containsKey(id))
		{
			botPanels.put(id, new OffensiveStatisticsBotPanel(id));
			botPanel.add(botPanels.get(id));
		}
		
		// update values here
		botPanels.get(id).updateBotFrame(frame);
	}
}
