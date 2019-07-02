/*
 * Copyright (c) 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.statistics.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import edu.tigers.sumatra.ai.metis.statistics.StatisticData;


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
	
	
	private int getBotColumn(Integer i)
	{
		String search = String.valueOf(i);
		for (int x = 0; x < getColumnCount(); x++)
		{
			if (search.equals(getColumnName(x)))
				return x;
		}
		return -1;
	}
	
	
	public void setData(Map<String, StatisticData> data, Set<Integer> bots)
	{
		if (model == null || bots.size() != model.getColumnCount() - 2)
		{
			List<String> header = new ArrayList<>();
			
			for (Integer i : bots)
			{
				header.add(String.valueOf(i));
			}
			
			Collections.sort(header);
			header.add(0, "General");
			header.add(0, "Statistic");
			
			
			model = new DefaultTableModel(header.toArray(), header.size());
			setModel(model);
			getColumnModel().getColumn(0).setMinWidth(200);
			
			DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
			cr.setHorizontalAlignment(JLabel.CENTER);
			for (int x = 1; x < model.getColumnCount(); x++)
			{
				getColumnModel().getColumn(x).setCellRenderer(cr);
			}
		}
		
		if (model.getRowCount() != data.size())
		{
			model.setRowCount(data.size());
		}
		
		Object[] keys = data.keySet().toArray();
		for (int x = 0; x < keys.length; x++)
		{
			model.setValueAt(keys[x], x, 0);
			StatisticData d = data.get(String.valueOf(keys[x]));
			model.setValueAt(d.formattedGeneralStatistic(), x, 1);
			for (Integer i : bots)
			{
				int col = getBotColumn(i);
				if (col < 0)
					continue;
				String val = d.formattedBotStatistics().get(i);
				if (val == null)
					val = "0";
				
				model.setValueAt(val, x, col);
			}
		}
	}
}
