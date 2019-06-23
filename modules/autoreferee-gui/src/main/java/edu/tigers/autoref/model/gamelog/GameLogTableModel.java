/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.model.gamelog;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import edu.tigers.autoreferee.engine.log.GameLog;
import edu.tigers.autoreferee.engine.log.GameLog.IGameLogObserver;
import edu.tigers.autoreferee.engine.log.GameLogEntry;
import edu.tigers.autoreferee.engine.log.IGameLog;


/**
 * {@link TableModel} implementation which links a {@link JTable} with a {@link GameLog} instance. It currently returns
 * the log entry for every column of an arbitrary row. The {@link TableCellRenderer} is responsible for rendering the
 * entry for each column.
 * 
 * @author "Lukas Magel"
 */
public class GameLogTableModel extends AbstractTableModel implements IGameLogObserver
{
	
	/**  */
	private static final long serialVersionUID = -8160241136867692587L;
	private static final List<String> columns;
	
	private final IGameLog gameLog;
	
	
	static
	{
		columns = Collections.unmodifiableList(Arrays.asList("Time", "Game Time", "Type", "Event"));
	}
	
	
	/**
	 * @param log
	 */
	public GameLogTableModel(final IGameLog log)
	{
		gameLog = log;
		gameLog.addObserver(this);
	}
	
	
	@Override
	public int getRowCount()
	{
		return gameLog.getEntries().size();
	}
	
	
	@Override
	public int getColumnCount()
	{
		return columns.size();
	}
	
	
	@Override
	public String getColumnName(final int column)
	{
		return columns.get(column);
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
		return getEntry(rowIndex);
	}
	
	
	@Override
	public void onNewEntry(final int id, final GameLogEntry entry)
	{
		EventQueue.invokeLater(() -> fireTableRowsInserted(id, id));
	}
	
	
	@Override
	public void onClear()
	{
		EventQueue.invokeLater(this::fireTableDataChanged);
	}
	
	
	private GameLogEntry getEntry(final int rowIndex)
	{
		return gameLog.getEntries().get(rowIndex);
	}
	
}
