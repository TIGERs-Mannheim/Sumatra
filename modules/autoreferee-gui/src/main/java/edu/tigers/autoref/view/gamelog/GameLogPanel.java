/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.view.gamelog;

import edu.tigers.autoref.model.gamelog.GameLogRowFilter;
import edu.tigers.autoref.model.gamelog.GameLogTableModel;
import edu.tigers.autoreferee.engine.log.ELogEntryType;
import edu.tigers.autoreferee.engine.log.GameLogEntry;
import edu.tigers.sumatra.components.EnumCheckBoxPanel;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.util.Set;
import java.util.stream.IntStream;


/**
 * Show the game log of the autoRef.
 */
public class GameLogPanel extends JPanel
{
	private final EnumCheckBoxPanel<ELogEntryType> logTypePanel;
	private JTable entryTable = new JTable();
	private transient GameLogRowFilter filter = new GameLogRowFilter();
	private transient TableRowSorter<GameLogTableModel> sorter = new TableRowSorter<>();


	public GameLogPanel(final GameLogTableModel tableModel)
	{
		logTypePanel = new EnumCheckBoxPanel<>(ELogEntryType.class, null, BoxLayout.LINE_AXIS);

		entryTable.setFillsViewportHeight(true);
		entryTable.setDefaultRenderer(GameLogEntry.class, new GameLogCellRenderer());

		sorter.setRowFilter(filter);
		entryTable.setRowSorter(sorter);

		setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(entryTable);
		add(scrollPane, BorderLayout.CENTER);
		add(logTypePanel, BorderLayout.PAGE_END);

		setTableModel(tableModel);
	}


	/**
	 * Set the supplied model as source for table entries
	 *
	 * @param model
	 */
	private void setTableModel(final GameLogTableModel model)
	{
		sorter.setModel(model);
		entryTable.setModel(model);
		model.addTableModelListener(new ScrollDownModelListener());

		disableUserColumnSorting();
	}


	/**
	 * Only display entries of the specified types
	 *
	 * @param types
	 */
	public void setActiveLogTypes(final Set<ELogEntryType> types)
	{
		filter.setIncludedTypes(types);
		sorter.sort();
	}


	private void disableUserColumnSorting()
	{
		IntStream.range(0, entryTable.getColumnCount()).forEach(i -> sorter.setSortable(i, false));
	}


	/**
	 * This class tries to automatically scroll down to new entries in the table if the table is already scrolled all the
	 * way to the bottom
	 */
	private class ScrollDownModelListener implements TableModelListener
	{
		@Override
		public void tableChanged(final TableModelEvent e)
		{
			Rectangle bounds = entryTable.getBounds();
			Rectangle viewBounds = entryTable.getVisibleRect();

			boolean isViewPortAtBottom = (viewBounds.getY() + viewBounds.getHeight()) >= (bounds.getY()
					+ bounds.getHeight());

			if (isViewPortAtBottom)
			{
				entryTable.scrollRectToVisible(entryTable.getCellRect(entryTable.getRowCount() - 1, 0, true));
			}
		}
	}


	public EnumCheckBoxPanel<ELogEntryType> getLogTypePanel()
	{
		return logTypePanel;
	}
}
