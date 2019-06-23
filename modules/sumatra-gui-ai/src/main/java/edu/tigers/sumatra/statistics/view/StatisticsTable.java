/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 31, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.statistics.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import edu.tigers.sumatra.ai.data.statistics.calculators.StatisticData;
import edu.tigers.sumatra.ids.BotID;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class StatisticsTable extends JTable
{
	/** The serial Version UID for this kind of table */
	private static final long				serialVersionUID		= 1L;
	
	private DefaultTableModel				tableModel;
	
	/** This Map will contain the Entries for the specific table rows */
	private Map<String, StatisticData>	rowEntries;
	
	private Map<BotID, Integer>			columnOfBot				= new HashMap<>();
	
	private final int							countGenericColums	= 2;
	private final int							placeGeneralColumn	= 1;
	
	private Map<BotID, Integer>			hardwareIDs				= new HashMap<>();
	
	private boolean							isHardwareIDShown		= false;
	
	
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
		if (isHardwareIDShown)
		{
			return hardwareIDs.get(botToGetHeader);
		}
		return botToGetHeader.getNumberWithColorOffset();
	}
	
	
	private void updateTable()
	{
		if (rowEntries != null)
		{
			tableModel.setRowCount(rowEntries.size() + 1);
			
			
			// This two rows are going to fill the table with the header and then with the values
			int row = 1;
			
			for (String rowDescriptor : rowEntries.keySet())
			{
				tableModel.setValueAt(rowDescriptor, row, 0);
				
				String generalStatistic = rowEntries.get(rowDescriptor).getTextualRepresenationOfGeneralStatistic();
				
				tableModel.setValueAt(generalStatistic, row, placeGeneralColumn);
				
				Map<BotID, String> specificStatistics = rowEntries.get(rowDescriptor)
						.getTextualRepresentationOfBotStatistics();
				
				for (BotID availableBot : specificStatistics.keySet())
				{
					Integer columnToSet;
					
					columnToSet = columnOfBot.get(availableBot);
					
					if ((columnToSet != null) && (specificStatistics != null))
					{
						tableModel.setValueAt(specificStatistics.get(availableBot), row, columnToSet);
					}
				}
				
				row++;
			}
		}
	}
	
	
	/**
	 * @param hardwareIDs This are the actual hardwareIDs
	 */
	public void updateHardwareIDs(final Map<BotID, Integer> hardwareIDs)
	{
		for (BotID tempBotID : hardwareIDs.keySet())
		{
			this.hardwareIDs.put(tempBotID, hardwareIDs.get(tempBotID));
		}
	}
	
	
	/**
	 * @param isHardwareIDShown the isHardwareIDShown to set
	 */
	public void setHardwareIDShown(final boolean isHardwareIDShown)
	{
		this.isHardwareIDShown = isHardwareIDShown;
	}
	
	
}
