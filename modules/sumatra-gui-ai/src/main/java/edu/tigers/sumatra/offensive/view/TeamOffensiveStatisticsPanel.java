/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.offensive.view;

import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAnalysedBotFrame;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveBotFrame;
import edu.tigers.sumatra.ids.BotID;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;


/**
 * Strategy panel for one team.
 */
public class TeamOffensiveStatisticsPanel extends JPanel
{
	/**  */
	private static final long								serialVersionUID	= 5289153581163080891L;
	
	private JTextField										minNumberText		= null;
	private JTextField										desiredNumberText	= null;
	private JTextField										maxNumberText		= null;
	private Map<BotID, OffensiveStatisticsBotPanel>	botPanels			= new HashMap<>();
	private JPanel												botPanel				= new JPanel(new MigLayout());
	private boolean											currentFrameOnly	= false;
	
	
	/**
	 * Team Panel
	 */
	public TeamOffensiveStatisticsPanel()
	{
		setLayout(new MigLayout());
		
		final JPanel numberPanel = new JPanel(new MigLayout("fill", "", ""));
		final JLabel numberLabel = new JLabel("Number of bots:");
		
		final JLabel minNumberLabel = new JLabel("min.");
		minNumberText = new JTextField("0");
		minNumberText.setEditable(false);
		
		final JLabel desiredNumberLabel = new JLabel("desired.");
		desiredNumberText = new JTextField("0");
		desiredNumberText.setEditable(false);
		
		final JLabel maxNumberLabel = new JLabel("max.");
		maxNumberText = new JTextField("0");
		maxNumberText.setEditable(false);
		
		numberPanel.add(numberLabel);
		numberPanel.add(minNumberLabel);
		numberPanel.add(minNumberText);
		numberPanel.add(maxNumberLabel);
		numberPanel.add(maxNumberText);
		numberPanel.add(desiredNumberLabel);
		numberPanel.add(desiredNumberText);
		
		add(numberPanel, "wrap,span");
		final JScrollPane jp = new JScrollPane(botPanel);
		add(jp);
	}
	
	
	/**
	 * @param min
	 * @param max
	 * @param desired
	 */
	public void setMaxMinDesiredAVG(double min, double max, double desired)
	{
		minNumberText.setText(String.valueOf(min));
		maxNumberText.setText(String.valueOf(max));
		desiredNumberText.setText(String.valueOf(desired));
	}
	
	
	/**
	 * @param min
	 * @param max
	 * @param desired
	 */
	public void setMaxMinDesired(double min, double max, double desired)
	{
		minNumberText.setText(String.valueOf(min));
		maxNumberText.setText(String.valueOf(max));
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
