/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.botoverview;

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.swing.table.AbstractTableModel;

import edu.tigers.sumatra.botoverview.view.BotOverviewColumn;
import edu.tigers.sumatra.botoverview.view.BotOverviewPanel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * Table Model for {@link BotOverviewPanel}
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotOverviewTableModel extends AbstractTableModel
{
	/**  */
	private static final long									serialVersionUID	= 4712760313475190867L;
	private final SortedMap<BotID, BotOverviewColumn>	data					= new ConcurrentSkipListMap<BotID, BotOverviewColumn>(
																										BotID.getComparator());
	private BotID[]												sortedBots			= new BotID[0];
	
	
	/**
	 * @param botId
	 * @param columnData
	 */
	public void putBot(final BotID botId, final BotOverviewColumn columnData)
	{
		BotOverviewColumn col = data.get(botId);
		if (col == null)
		{
			data.put(botId, columnData);
			update();
			fireTableStructureChanged();
		} else if (!col.equals(columnData))
		{
			data.put(botId, columnData);
			fireTableDataChanged();
		}
	}
	
	
	/**
	 * @param botId
	 */
	public void removeBot(final BotID botId)
	{
		Object rem = data.remove(botId);
		if (rem != null)
		{
			update();
			fireTableStructureChanged();
		}
	}
	
	
	/**
	 * @param botId
	 * @return
	 */
	public BotOverviewColumn getBotOverviewColumn(final BotID botId)
	{
		return data.get(botId);
	}
	
	
	private void update()
	{
		sortedBots = data.keySet().toArray(new BotID[data.size()]);
	}
	
	
	/**
	 * @return
	 */
	public List<BotID> getBots()
	{
		return Arrays.asList(sortedBots);
	}
	
	
	@Override
	public int getColumnCount()
	{
		return data.size();
	}
	
	
	@Override
	public int getRowCount()
	{
		return BotOverviewColumn.ROWS;
	}
	
	
	@Override
	public String getColumnName(final int col)
	{
		return "Bot " + sortedBots[col].getNumber() + " "
				+ (sortedBots[col].getTeamColor() == ETeamColor.YELLOW ? "Y" : "B");
	}
	
	
	@Override
	public Object getValueAt(final int row, final int col)
	{
		return data.get(sortedBots[col]).getData().get(row);
	}
	
	
	@Override
	public Class<?> getColumnClass(final int c)
	{
		return getValueAt(0, c).getClass();
	}
}
