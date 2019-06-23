/*
 * Copyright (c) 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.statistics.view;

import edu.tigers.sumatra.ai.metis.statistics.StatisticData;
import edu.tigers.sumatra.ids.BotID;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Marius Messerschmidt <marius.messerschmidt@dlr.de>
 */
public class StatisticsTable extends JTable
{
	/**
	 * The serial Version UID for this kind of table
	 */
	private static final long serialVersionUID = 1L;
	DefaultTableModel model = null;

	private int getBotColumn(BotID i)
	{
		String search = String.valueOf(i.getNumber());
		for(int x = 0; x < getColumnCount(); x++)
		{
			if( search.equals(getColumnName(x)))
				return x;
		}
		return -1;
	}

	public void setData(Map<String, StatisticData> data, Set<BotID> bots)
	{
		if (model == null || bots.size() != model.getColumnCount() - 2)
		{
			List<String> header = new ArrayList<>();

			for (BotID i : bots)
				header.add(String.valueOf(i.getNumber()));

			Collections.sort(header);
			header.add(0, "General");
			header.add(0, "Statistic");


			model = new DefaultTableModel(header.toArray(), header.size());
			setModel(model);
			getColumnModel().getColumn(0).setMinWidth(200);

			DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
			cr.setHorizontalAlignment(JLabel.CENTER);
			for(int x = 1; x < model.getColumnCount(); x++)
			{
				getColumnModel().getColumn(x).setCellRenderer(cr);
			}
		}

		if(model.getRowCount() != data.size())
			model.setRowCount(data.size());

        Object[] keys = data.keySet().toArray();
		for(int x = 0; x < keys.length; x++)
		{
			model.setValueAt(keys[x],x, 0);
			StatisticData d = data.get(String.valueOf(keys[x]));
			model.setValueAt(d.getTextualRepresenationOfGeneralStatistic(), x, 1);
			for(BotID i : bots)
			{
				int col = getBotColumn(i);
				if (col < 0)
					continue;
				String val = d.getTextualRepresentationOfBotStatistics().get(i);
				if (val == null)
					val = "0";

				model.setValueAt(val, x, col);
			}
		}
	}

}
