/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.view.gamelog;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import edu.tigers.autoref.model.gamelog.GameLogRowFilter;
import edu.tigers.autoref.model.gamelog.GameLogTableModel;
import edu.tigers.autoreferee.engine.log.ELogEntryType;
import edu.tigers.autoreferee.engine.log.GameLogEntry;
import edu.tigers.sumatra.components.EnumCheckBoxPanel;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.RecordManager;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * Show the game log of the autoRef.
 */
public class GameLogPanel extends JPanel implements ISumatraView
{
	private final EnumCheckBoxPanel<ELogEntryType> logTypePanel;
	private JTable entryTable = new JTable();
	private GameLogRowFilter filter = new GameLogRowFilter();
	private TableRowSorter<GameLogTableModel> sorter = new TableRowSorter<>();
	
	
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
		
		final JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem replayItem = new JMenuItem("Replay");
		replayItem.addActionListener(actionEvent -> viewRecording());
		popupMenu.add(replayItem);
		entryTable.setComponentPopupMenu(popupMenu);
		popupMenu.addPopupMenuListener(new RowPopupMenuListener(popupMenu));
		
		setTableModel(tableModel);
	}
	
	
	private void viewRecording()
	{
		int selectedRow = entryTable.getSelectedRow();
		long startTime;
		if (selectedRow < 0)
		{
			startTime = 0;
		} else
		{
			int row = entryTable.convertRowIndexToModel(selectedRow);
			GameLogEntry value = (GameLogEntry) entryTable.getModel().getValueAt(row, 0);
			startTime = value.getTimestamp() - TimeUnit.SECONDS.toNanos(3);
		}
		
		SumatraModel.getInstance().getModuleOpt(RecordManager.class).ifPresent(m -> m.notifyViewReplay(startTime));
	}
	
	private class RowPopupMenuListener implements PopupMenuListener
	{
		private final JPopupMenu popupMenu;
		
		
		RowPopupMenuListener(JPopupMenu popupMenu)
		{
			this.popupMenu = popupMenu;
		}
		
		
		@Override
		public void popupMenuWillBecomeVisible(final PopupMenuEvent popupMenuEvent)
		{
			SwingUtilities.invokeLater(() -> {
				int rowAtPoint = entryTable.rowAtPoint(SwingUtilities.convertPoint(popupMenu, new Point(0, 0), entryTable));
				if (rowAtPoint > -1)
				{
					entryTable.setRowSelectionInterval(rowAtPoint, rowAtPoint);
				}
			});
		}
		
		
		@Override
		public void popupMenuWillBecomeInvisible(final PopupMenuEvent popupMenuEvent)
		{
			// nothing to do
		}
		
		
		@Override
		public void popupMenuCanceled(final PopupMenuEvent popupMenuEvent)
		{
			// nothing to do
		}
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
				EventQueue.invokeLater(
						() -> entryTable.scrollRectToVisible(entryTable.getCellRect(entryTable.getRowCount() - 1, 0, true)));
			}
		}
	}
	
	
	public EnumCheckBoxPanel<ELogEntryType> getLogTypePanel()
	{
		return logTypePanel;
	}
}
