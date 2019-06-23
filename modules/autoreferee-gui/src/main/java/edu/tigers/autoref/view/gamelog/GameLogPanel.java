/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.view.gamelog;

import java.awt.*;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import edu.tigers.autoref.model.gamelog.GameLogRowFilter;
import edu.tigers.autoref.model.gamelog.GameLogTableModel;
import edu.tigers.autoreferee.engine.log.GameLog;
import edu.tigers.autoreferee.engine.log.GameLogEntry;
import edu.tigers.autoreferee.engine.log.GameLogEntry.ELogEntryType;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.components.EnumCheckBoxPanel;
import edu.tigers.sumatra.components.IEnumPanel;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.ARecordManager;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * Panel class which displays a {@link JTable} instance to display entries of a {@link GameLog}. Use it in conjunction
 * with the {@link GameLogTableModel} class.
 * 
 * @author "Lukas Magel"
 */
public class GameLogPanel extends JPanel implements IGameLogPanel, ISumatraView
{
	private static final Logger log = Logger.getLogger(GameLogPanel.class.getName());
	
	/**  */
	private static final long serialVersionUID = 3266769602344203080L;
	
	
	private EnumCheckBoxPanel<ELogEntryType> logTypePanel;
	private JTable entryTable = new JTable();
	private GameLogRowFilter filter = new GameLogRowFilter();
	private TableRowSorter<GameLogTableModel> sorter = new TableRowSorter<>();
	
	
	/**
	 * New panel
	 */
	public GameLogPanel()
	{
		logTypePanel = new EnumCheckBoxPanel<>(ELogEntryType.class, null, BoxLayout.LINE_AXIS);
		
		entryTable.setFillsViewportHeight(true);
		entryTable.setDefaultRenderer(GameLogEntry.class, new GameLogCellRenderer());
		
		sorter.setRowFilter(filter);
		entryTable.setRowSorter(sorter);
		
		setupGUI();
	}
	
	
	private void setupGUI()
	{
		/*
		 * Layout the component
		 */
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
	}
	
	
	private void viewRecording()
	{
		int selectedRow = entryTable.getSelectedRow();
		if (selectedRow < 0)
		{
			return;
		}
		int row = entryTable.convertRowIndexToModel(selectedRow);
		GameLogEntry value = (GameLogEntry) entryTable.getModel().getValueAt(row, 0);
		long startTime = value.getTimestamp() - TimeUnit.SECONDS.toNanos(3);
		
		try
		{
			ARecordManager recordManager = (ARecordManager) SumatraModel.getInstance().getModule(ARecordManager.MODULE_ID);
			recordManager.notifyViewReplay(startTime);
		} catch (ModuleNotFoundException e)
		{
			log.debug("There is no record manager. Wont't add observer", e);
		}
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
	@Override
	public void setTableModel(final GameLogTableModel model)
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
	@Override
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
	 * 
	 * @author "Lukas Magel"
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
	
	
	@Override
	public IEnumPanel<ELogEntryType> getLogTypePanel()
	{
		return logTypePanel;
	}
}
