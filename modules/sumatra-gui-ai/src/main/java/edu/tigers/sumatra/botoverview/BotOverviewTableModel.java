/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botoverview;

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.swing.table.AbstractTableModel;

import edu.tigers.sumatra.ai.data.EBotInformation;
import edu.tigers.sumatra.botoverview.view.BotOverviewColumn;
import edu.tigers.sumatra.botoverview.view.BotOverviewPanel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * Table Model for {@link BotOverviewPanel}
 */
public class BotOverviewTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 4712760313475190867L;
	private final SortedMap<BotID, BotOverviewColumn> data = new ConcurrentSkipListMap<>(BotID.getComparator());
	private BotID[] sortedBots = new BotID[0];


	/**
	 * @param botId
	 * @param columnData
	 * @return the previous item, if present
	 */
	public BotOverviewColumn putBot(final BotID botId, final BotOverviewColumn columnData)
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
		return col;
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


	private void update()
	{
		sortedBots = data.keySet().toArray(new BotID[0]);
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
		return data.size() + 1;
	}


	@Override
	public int getRowCount()
	{
		return EBotInformation.values().length;
	}


	@Override
	public String getColumnName(final int col)
	{
		if (col == 0)
		{
			return "";
		}
		return "Bot " + sortedBots[col - 1].getNumber() + " "
				+ (sortedBots[col - 1].getTeamColor() == ETeamColor.YELLOW ? "Y" : "B");
	}


	@Override
	public Object getValueAt(final int row, final int col)
	{
		if (col == 0)
		{
			return EBotInformation.values()[row].getLabel();
		}
		return data.get(sortedBots[col - 1]).getData().get(row);
	}


	@Override
	public Class<?> getColumnClass(final int c)
	{
		return getValueAt(0, c).getClass();
	}
}
