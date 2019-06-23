/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botoverview.view;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXTable;

import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.botoverview.BotOverviewTableModel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * Main Panel for Bot Overview
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotOverviewPanel extends JPanel implements ISumatraView
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long serialVersionUID = -8536401073164037476L;
	private final BotOverviewTableModel model;
	private final JXTable table;
	
	
	/**
	 * Default
	 */
	public BotOverviewPanel()
	{
		setLayout(new BorderLayout());
		model = new BotOverviewTableModel();
		table = new JXTable(model);
		table.setColumnControlVisible(true);
		table.setHorizontalScrollEnabled(true);
		table.updateUI();
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);
	}
	
	
	/**
	 * @param frame
	 */
	public void update(final VisualizationFrame frame)
	{
		SwingUtilities.invokeLater(() -> doUpdate(frame));
	}
	
	
	private void doUpdate(final VisualizationFrame frame)
	{
		// remove vanished bots
		for (BotID botId : model.getBots())
		{
			if ((botId.getTeamColor() == frame.getTeamColor()) && !frame.getAiInfos().containsKey(botId))
			{
				model.removeBot(botId);
			}
		}
		
		// add ai info for all known bots
		for (Map.Entry<BotID, BotAiInformation> entry : frame.getAiInfos().entrySet())
		{
			BotID botId = entry.getKey();
			BotAiInformation aiInfo = entry.getValue();
			if (aiInfo != null)
			{
				BotOverviewColumn column = new BotOverviewColumn(aiInfo);
				BotOverviewColumn prevColumn = model.putBot(botId, column);
				if (prevColumn == null)
				{
					updateColumnSize();
				}
			}
		}
	}
	
	
	private void updateColumnSize()
	{
		table.getColumnModel().getColumn(0).setMinWidth(155);
		for (int i = 1; i < model.getColumnCount(); i++)
		{
			table.getColumnModel().getColumn(i).setMinWidth(65);
		}
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		return new ArrayList<>();
	}
	
	
	@Override
	public void onShown()
	{
		// nothing to do
	}
	
	
	@Override
	public void onHidden()
	{
		// nothing to do
	}
	
	
	@Override
	public void onFocused()
	{
		// nothing to do
	}
	
	
	@Override
	public void onFocusLost()
	{
		// nothing to do
	}
}
