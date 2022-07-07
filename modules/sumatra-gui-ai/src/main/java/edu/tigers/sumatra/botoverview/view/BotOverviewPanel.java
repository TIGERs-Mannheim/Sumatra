/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botoverview.view;

import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.botoverview.BotOverviewTableModel;
import edu.tigers.sumatra.ids.BotID;
import org.jdesktop.swingx.JXTable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.io.Serial;
import java.util.Map;


/**
 * Main Panel for Bot Overview
 */
public class BotOverviewPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = -8536401073164037476L;
	private final BotOverviewTableModel model;
	private final JXTable table;


	public BotOverviewPanel()
	{
		setLayout(new BorderLayout());
		model = new BotOverviewTableModel();
		table = new JXTable(model);
		table.setColumnControlVisible(true);
		table.setHorizontalScrollEnabled(true);
		table.setSortable(false);
		table.updateUI();
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane, BorderLayout.CENTER);
	}


	public void update(final VisualizationFrame frame)
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
}
