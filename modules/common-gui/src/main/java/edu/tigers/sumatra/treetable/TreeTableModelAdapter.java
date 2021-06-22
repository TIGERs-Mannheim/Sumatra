/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.treetable;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;


/**
 * This is the {@link AbstractTableModel} implementation for the {@link javax.swing.JTable}-part of the
 * {@link JTreeTable}. It
 * therefore delegates the calls to the underlying {@link javax.swing.tree.TreeModel} (in {@link ITreeTableModel}).
 *
 * @author Gero
 * @see JTreeTable
 */
public class TreeTableModelAdapter extends AbstractTableModel
{
	/**
	 *
	 */
	private static final long serialVersionUID = -6298333095243382630L;

	private final JTree tree;
	private final ITreeTableModel treeTableModel;


	/**
	 * @param treeTableModel
	 * @param tree
	 */
	public TreeTableModelAdapter(final ITreeTableModel treeTableModel, final JTree tree)
	{
		this.tree = tree;
		this.treeTableModel = treeTableModel;

		tree.addTreeExpansionListener(new TreeExpansionListener()
		{
			// Don't use fireTableRowsInserted() here; the selection model
			// would get updated twice.
			@Override
			public void treeExpanded(final TreeExpansionEvent event)
			{
				fireTableDataChanged();
			}


			@Override
			public void treeCollapsed(final TreeExpansionEvent event)
			{
				fireTableDataChanged();
			}
		});

		// Install a TreeModelListener that can update the table when
		// tree changes. We use delayedFireTableDataChanged as we can
		// not be guaranteed the tree will have finished processing
		// the event before us.
		treeTableModel.addTreeModelListener(new TreeModelListenerImpl());
	}


	private class TreeModelListenerImpl implements TreeModelListener
	{
		@Override
		public void treeNodesChanged(final TreeModelEvent e)
		{
			// Only one element changed
			if (e.getChildIndices().length == 0)
			{
				final int row = TreeTableModelAdapter.this.tree.getRowForPath(e.getTreePath());
				delayedFireTableRowsUpdated(row, row);
			} else
			{
				delayedFireTableDataChanged();
			}
		}


		@Override
		public void treeNodesInserted(final TreeModelEvent e)
		{
			delayedFireTableDataChanged();
		}


		@Override
		public void treeNodesRemoved(final TreeModelEvent e)
		{
			delayedFireTableDataChanged();
		}


		@Override
		public void treeStructureChanged(final TreeModelEvent e)
		{
			delayedFireTableDataChanged();
		}


		/**
		 * Invokes fireTableDataChanged after all the pending events have been
		 * processed. SwingUtilities.invokeLater is used to handle this.
		 */
		private void delayedFireTableDataChanged()
		{
			SwingUtilities.invokeLater(TreeTableModelAdapter.this::fireTableDataChanged);
		}


		/**
		 * Invokes {@link #fireTableRowsUpdated(int, int)} after all the pending events have been
		 * processed. SwingUtilities.invokeLater is used to handle this.
		 */
		private void delayedFireTableRowsUpdated(final int firstRow, final int lastRow)
		{
			SwingUtilities.invokeLater(() -> fireTableRowsUpdated(firstRow, lastRow));
		}
	}


	@Override
	public int getColumnCount()
	{
		return treeTableModel.getColumnCount();
	}


	@Override
	public String getColumnName(final int column)
	{
		return treeTableModel.getColumnName(column);
	}


	@Override
	public Class<?> getColumnClass(final int column)
	{
		return treeTableModel.getColumnClass(column);
	}


	@Override
	public int getRowCount()
	{
		return tree.getRowCount();
	}


	/**
	 * Get the node of a specific row.
	 *
	 * @param row
	 * @return
	 */
	public Object getNodeForRow(final int row)
	{
		final TreePath treePath = tree.getPathForRow(row);
		return treePath.getLastPathComponent();
	}


	@Override
	public Object getValueAt(final int row, final int column)
	{
		return treeTableModel.getValueAt(getNodeForRow(row), column);
	}


	@Override
	public boolean isCellEditable(final int row, final int column)
	{
		return treeTableModel.isCellEditable(getNodeForRow(row), column);
	}


	@Override
	public void setValueAt(final Object value, final int row, final int column)
	{
		treeTableModel.setValueAt(value, getNodeForRow(row), column);
	}
}
