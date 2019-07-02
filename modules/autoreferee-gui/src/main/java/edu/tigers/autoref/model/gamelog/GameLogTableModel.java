/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.model.gamelog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import edu.tigers.autoreferee.engine.log.GameLogEntry;


/**
 * {@link TableModel} implementation which links a {@link JTable} with a list of {@link GameLogEntry} instances.
 * It returns the log entry for every column of an arbitrary row. The {@link TableCellRenderer} is responsible for
 * rendering the entry for each column.
 */
public class GameLogTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = -8160241136867692587L;
	private static final List<String> COLUMNS = Collections
			.unmodifiableList(Arrays.asList("Time", "Game Time", "Type", "Event"));
	
	private final List<GameLogEntry> entries = new ArrayList<>();
	
	
	public void add(GameLogEntry entry)
	{
		SwingUtilities.invokeLater(() -> {
			entries.add(entry);
			int id = entries.size() - 1;
			fireTableRowsInserted(id, id);
		});
	}
	
	
	public void onClear()
	{
		SwingUtilities.invokeLater(() -> {
			entries.clear();
			fireTableDataChanged();
		});
	}
	
	
	@Override
	public int getRowCount()
	{
		return entries.size();
	}
	
	
	@Override
	public int getColumnCount()
	{
		return COLUMNS.size();
	}
	
	
	@Override
	public String getColumnName(final int column)
	{
		return COLUMNS.get(column);
	}
	
	
	@Override
	public Class<?> getColumnClass(final int columnIndex)
	{
		return GameLogEntry.class;
	}
	
	
	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex)
	{
		/*
		 * The model returns the log entry for every column since the cell renderer needs the entire log entry to properly
		 * format each cell.
		 * It is responsible for displaying the correct data in each column.
		 */
		return entries.get(rowIndex);
	}
	
	
	/**
	 * Remove entries that have a larger timestamp that the given one.
	 * This will clear events when going backwards in time in a replay window.
	 * 
	 * @param timestamp
	 */
	public void removeTooRecentEntries(final long timestamp)
	{
		SwingUtilities.invokeLater(() -> {
			boolean removed = entries.removeIf(e -> e.getTimestamp() > timestamp);
			if (removed)
			{
				fireTableDataChanged();
			}
		});
	}
}
