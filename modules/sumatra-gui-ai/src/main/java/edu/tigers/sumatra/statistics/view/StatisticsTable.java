/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.statistics.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import edu.tigers.sumatra.ai.metis.statistics.StatisticData;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class StatisticsTable extends JTable
{
	/** The serial Version UID for this kind of table */
	private static final long serialVersionUID = 1L;
	
	private DefaultTableModel tableModel;
	
	/** This Map will contain the Entries for the specific table rows */
	private Map<String, StatisticData> rowEntries;
	
	private Map<BotID, Integer> columnOfBot = new HashMap<>();
	
	
	/**
	 * This is the default constructor, that constructs a 1 x 2 Table, it will be filled with information
	 * when the KI is running
	 */
	public StatisticsTable()
	{
		super(0, 2);
		tableModel = (DefaultTableModel) getModel();
		
		tableModel.addRow(new String[] { "Statistic", "General" });
	}
	
	
	/**
	 * This will update the table entries.
	 * If there is no data it will keep the last calculated data for information purposes
	 * 
	 * @param updatedEntries A list with updated entries
	 * @param availableBots A list with the available Bots
	 */
	public void updateTableEntries(final Map<String, StatisticData> updatedEntries,
			final Set<BotID> availableBots)
	{
		rowEntries = updatedEntries;
		
		createBotHeaders(availableBots);
		
		// This would mean that there is no update or that the statistics is halted
		if (updatedEntries.size() == 0)
		{
			return;
		}
		
		updateTable();
	}
	
	
	private void createBotHeaders(final Set<BotID> availableBots)
	{
		final int countGenericColums = 2;
		tableModel.setColumnCount(columnOfBot.size() + countGenericColums);
		
		for (BotID tempBotID : availableBots)
		{
			if (!columnOfBot.containsKey(tempBotID))
			{
				columnOfBot.put(tempBotID, countGenericColums + columnOfBot.size());
				
				tableModel.setColumnCount(columnOfBot.size() + countGenericColums);
			}
			
			Integer valueToDisplay = getBotHeaderEntry(tempBotID);
			
			tableModel.setValueAt(valueToDisplay, 0, columnOfBot.get(tempBotID));
		}
	}
	
	
	private int getBotHeaderEntry(final BotID botToGetHeader)
	{
		return botToGetHeader.getNumber();
	}
	
	
	private void updateTable()
	{
		if (rowEntries == null)
		{
			return;
		}
		
		tableModel.setRowCount(rowEntries.size() + 1);
		
		
		// This two rows are going to fill the table with the header and then with the values
		int row = 1;
		
		for (Map.Entry<String, StatisticData> rowEntry : rowEntries.entrySet())
		{
			tableModel.setValueAt(rowEntry, row, 0);
			
			String generalStatistic = rowEntry.getValue().getTextualRepresenationOfGeneralStatistic();
			
			final int placeGeneralColumn = 1;
			tableModel.setValueAt(generalStatistic, row, placeGeneralColumn);
			
			Map<BotID, String> specificStatistics = rowEntry.getValue().getTextualRepresentationOfBotStatistics();
			
			for (Map.Entry<BotID, String> statisticEntry : specificStatistics.entrySet())
			{
				Integer columnToSet = columnOfBot.get(statisticEntry.getKey());
				
				if (columnToSet != null)
				{
					tableModel.setValueAt(statisticEntry.getValue(), row, columnToSet);
				}
			}
			
			row++;
		}
	}
}
