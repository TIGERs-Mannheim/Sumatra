/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.11.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.commons.treetable;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;


/**
 * This is the main class of the treetable-component. This is based on the ideas from this tutorials:
 * <ul>
 * <li><a href="http://java.sun.com/products/jfc/tsc/articles/treetable1/">TreeTable 1</a> and</li>
 * <li><a href="http://java.sun.com/products/jfc/tsc/articles/treetable2/">TreeTable 2</a></li>
 * </ul>
 * 
 * Swing doesn't provide any combination of the a {@link JTree} and {@link JTable} of the shelve, although it would've
 * been very convenient... So this combines the two to provide a solution!<br/>
 * Basically the first column of a {@link JTable} is rendered by an underlying {@link JTree}. The model is an
 * implementation of a special interface {@link ITreeTableModel} / {@link ATreeTableModel}. The {@link JTable} accesses
 * the models data through the {@link TreeTableModelAdapter}. The {@link JTreeTable} also features {@link ACellEditor}.<br/>
 * <br/>
 * Although the whole thing is a little more complex then usual it works quite well! ^^
 * 
 * @author Gero
 * 
 */
public class JTreeTable extends JTable
{
	/**  */
	private static final long			serialVersionUID	= -3052468144632521282L;
	
	/** A subclass of JTree. */
	protected TreeTableCellRenderer	tree;
	private final ITreeTableModel		treeTableModel;
	
	
	/**
	 * @param treeTableModel
	 */
	public JTreeTable(ITreeTableModel treeTableModel)
	{
		super();
		setTreeTableModel(treeTableModel);
		this.treeTableModel = treeTableModel;
		
		// setCellEditor(anEditor) // # Potentally check for validity...?
	}
	
	
	/**
	 * Sets the {@link ITreeTableModel} to be shown by this {@link JTreeTable}
	 * 
	 * @param treeTableModel
	 */
	public void setTreeTableModel(ITreeTableModel treeTableModel)
	{
		// Create the tree. It will be used as a renderer and editor.
		tree = new TreeTableCellRenderer(treeTableModel);
		final TreeRenderer treeRenderer = new TreeRenderer(treeTableModel);
		// Cares for the rendering of the first column by the JTree
		tree.setCellRenderer(treeRenderer);
		
		// Install a tableModel representing the visible rows in the tree.
		super.setModel(new TreeTableModelAdapter(treeTableModel, tree));
		
		// Force the JTable and JTree to share their row selection models.
		final ListToTreeSelectionModelWrapper selectionWrapper = new ListToTreeSelectionModelWrapper();
		tree.setSelectionModel(selectionWrapper);
		setSelectionModel(selectionWrapper.getListSelectionModel());
		
		// Install the tree editor renderer and editor.
		setDefaultRenderer(ITreeTableModel.class, tree);
		setDefaultEditor(ITreeTableModel.class, new TreeTableCellEditor());
		
		// No grid.
		setShowGrid(false);
		
		// No intercell spacing
		setIntercellSpacing(new Dimension(0, 0));
		
		// And update the height of the trees row to match that of
		// the table.
		if (tree.getRowHeight() < 1)
		{
			// Metal looks better like this.
			setRowHeight(18);
		}
	}
	
	
	@Override
	public String getToolTipText(MouseEvent event)
	{
		return treeTableModel.getToolTipText(event);
	}
	
	
	/**
	 * Overridden to message super and forward the method to the tree.
	 * Since the tree is not actually in the component hierarchy it will
	 * never receive this unless we forward it in this manner.
	 */
	@Override
	public void updateUI()
	{
		super.updateUI();
		if (tree != null)
		{
			tree.updateUI();
		}
		// Use the tree's default foreground and background colors in the
		// table.
		LookAndFeel.installColorsAndFont(this, "Tree.background", "Tree.foreground", "Tree.font");
	}
	
	
	/*
	 * Workaround for BasicTableUI anomaly. Make sure the UI never tries to
	 * paint the editor. The UI currently uses different techniques to
	 * paint the renderers and editors and overriding setBounds() below
	 * is not the right thing to do for an editor. Returning -1 for the
	 * editing row in this case, ensures the editor is never painted.
	 */
	@Override
	public int getEditingRow()
	{
		return (getColumnClass(editingColumn) == ITreeTableModel.class) ? -1 : editingRow;
	}
	
	
	/**
	 * Overridden to pass the new rowHeight to the tree.
	 */
	@Override
	public void setRowHeight(int rowHeight)
	{
		super.setRowHeight(rowHeight);
		if ((tree != null) && (tree.getRowHeight() != rowHeight))
		{
			tree.setRowHeight(getRowHeight());
		}
	}
	
	
	/**
	 * A TreeCellRenderer that displays a JTree.
	 */
	public class TreeTableCellRenderer extends JTree implements TableCellRenderer
	{
		/**  */
		private static final long	serialVersionUID	= 6816892617917678961L;
		
		/** Last table/tree row asked to renderer. */
		protected int					visibleRow;
		
		
		/**
		 * @param treeTableModel
		 */
		public TreeTableCellRenderer(ITreeTableModel treeTableModel)
		{
			super(treeTableModel);
		}
		
		
		/**
		 * updateUI is overridden to set the colors of the Tree's renderer
		 * to match that of the table.
		 */
		@Override
		public void updateUI()
		{
			super.updateUI();
			// Make the tree's cell renderer use the table's cell selection
			// colors.
			final TreeCellRenderer tcr = getCellRenderer();
			if (tcr instanceof DefaultTreeCellRenderer)
			{
				final DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer) tcr);
				// For 1.1 uncomment this, 1.2 has a bug that will cause an
				// exception to be thrown if the border selection color is
				// null.
				dtcr.setTextSelectionColor(UIManager.getColor("Table.selectionForeground"));
				dtcr.setBackgroundSelectionColor(UIManager.getColor("Table.selectionBackground"));
			}
		}
		
		
		/**
		 * Sets the row height of the tree, and forwards the row height to
		 * the table.
		 */
		@Override
		public void setRowHeight(int rowHeight)
		{
			if (rowHeight > 0)
			{
				super.setRowHeight(rowHeight);
				if ((JTreeTable.this != null) && (JTreeTable.this.getRowHeight() != rowHeight))
				{
					JTreeTable.this.setRowHeight(getRowHeight());
				}
			}
		}
		
		
		/**
		 * This is overridden to set the height to match that of the JTable.
		 */
		@Override
		public void setBounds(int x, int y, int w, int h)
		{
			super.setBounds(x, 0, w, JTreeTable.this.getHeight());
		}
		
		
		/**
		 * Sublcassed to translate the graphics such that the last visible
		 * row will be drawn at 0,0.
		 */
		@Override
		public void paint(Graphics g)
		{
			g.translate(0, -visibleRow * getRowHeight());
			super.paint(g);
		}
		
		
		/**
		 * TreeCellRenderer method. Overridden to update the visible row.
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column)
		{
			if (isSelected)
			{
				setBackground(table.getSelectionBackground());
			} else
			{
				setBackground(table.getBackground());
			}
			
			visibleRow = row;
			return this;
		}
	}
	
	
	/**
	 * TreeTableCellEditor implementation. Component returned is the
	 * JTree.
	 */
	public class TreeTableCellEditor extends AbstractCellEditor implements TableCellEditor
	{
		/**  */
		private static final long	serialVersionUID	= -2591875536212318768L;
		
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int r, int c)
		{
			return tree;
		}
		
		
		/**
		 * Overridden to return false, and if the event is a mouse event
		 * it is forwarded to the tree.
		 * <p>
		 * The behavior for this is debatable, and should really be offered as a property. By returning false, all
		 * keyboard actions are implemented in terms of the table. By returning true, the tree would get a chance to do
		 * something with the keyboard events. For the most part this is ok. But for certain keys, such as left/right, the
		 * tree will expand/collapse where as the table focus should really move to a different column. Page up/down
		 * should also be implemented in terms of the table. By returning false this also has the added benefit that
		 * clicking outside of the bounds of the tree node, but still in the tree column will select the row, whereas if
		 * this returned true that wouldn't be the case.
		 * <p>
		 * By returning false we are also enforcing the policy that the tree will never be editable (at least by a key
		 * sequence).
		 */
		@Override
		public boolean isCellEditable(EventObject e)
		{
			if (e instanceof MouseEvent)
			{
				for (int counter = getColumnCount() - 1; counter >= 0; counter--)
				{
					if (getColumnClass(counter) == ITreeTableModel.class)
					{
						final MouseEvent me = (MouseEvent) e;
						final MouseEvent newME = new MouseEvent(tree, me.getID(), me.getWhen(), me.getModifiers(), me.getX()
								- getCellRect(0, counter, true).x, me.getY(), me.getClickCount(), me.isPopupTrigger());
						tree.dispatchEvent(newME);
						break;
					}
				}
			}
			
			return false;
		}
		
		
		@Override
		public Object getCellEditorValue()
		{
			// Don't know what this is for! O.o
			return null;
		}
	}
	
	
	/**
	 * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
	 * to listen for changes in the ListSelectionModel it maintains. Once
	 * a change in the ListSelectionModel happens, the paths are updated
	 * in the DefaultTreeSelectionModel.
	 */
	class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
	{
		/**  */
		private static final long	serialVersionUID	= -5909393885929909085L;
		
		/** Set to true when we are updating the ListSelectionModel. */
		protected boolean				updatingListSelectionModel;
		
		
		/**
		 */
		public ListToTreeSelectionModelWrapper()
		{
			super();
			getListSelectionModel().addListSelectionListener(createListSelectionListener());
		}
		
		
		/**
		 * Returns the list selection model. ListToTreeSelectionModelWrapper
		 * listens for changes to this model and updates the selected paths
		 * accordingly.
		 */
		ListSelectionModel getListSelectionModel()
		{
			return listSelectionModel;
		}
		
		
		/**
		 * This is overridden to set <code>updatingListSelectionModel</code> and message super. This is the only place
		 * DefaultTreeSelectionModel
		 * alters the ListSelectionModel.
		 */
		@Override
		public void resetRowSelection()
		{
			if (!updatingListSelectionModel)
			{
				updatingListSelectionModel = true;
				try
				{
					super.resetRowSelection();
				} finally
				{
					updatingListSelectionModel = false;
				}
			}
			// Notice how we don't message super if
			// updatingListSelectionModel is true. If
			// updatingListSelectionModel is true, it implies the
			// ListSelectionModel has already been updated and the
			// paths are the only thing that needs to be updated.
		}
		
		
		/**
		 * Creates and returns an instance of ListSelectionHandler.
		 */
		protected ListSelectionListener createListSelectionListener()
		{
			return new ListSelectionHandler();
		}
		
		
		/**
		 * If <code>updatingListSelectionModel</code> is false, this will
		 * reset the selected paths from the selected rows in the list
		 * selection model.
		 */
		protected void updateSelectedPathsFromSelectedRows()
		{
			if (!updatingListSelectionModel)
			{
				updatingListSelectionModel = true;
				try
				{
					// This is way expensive, ListSelectionModel needs an
					// enumerator for iterating.
					final int min = listSelectionModel.getMinSelectionIndex();
					final int max = listSelectionModel.getMaxSelectionIndex();
					
					clearSelection();
					if ((min != -1) && (max != -1))
					{
						for (int counter = min; counter <= max; counter++)
						{
							if (listSelectionModel.isSelectedIndex(counter))
							{
								final TreePath selPath = tree.getPathForRow(counter);
								
								if (selPath != null)
								{
									addSelectionPath(selPath);
								}
							}
						}
					}
				} finally
				{
					updatingListSelectionModel = false;
				}
			}
		}
		
		/**
		 * Class responsible for calling updateSelectedPathsFromSelectedRows
		 * when the selection of the list changse.
		 */
		class ListSelectionHandler implements ListSelectionListener
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				updateSelectedPathsFromSelectedRows();
			}
		}
	}
	
	
	/**
	 * Forwards the possibility to influence the rendering of the first column to the {@link ITreeTableModel}, and acts
	 * as {@link ILookAndFeelStateObserver} for Sumatra.
	 * 
	 * @author Gero
	 * 
	 */
	private static class TreeRenderer extends DefaultTreeCellRenderer implements ILookAndFeelStateObserver
	{
		/**  */
		private static final long		serialVersionUID	= 7785387299072802203L;
		
		private final ITreeTableModel	treeTableModel;
		
		
		/**
		 * @param treeTableModel
		 */
		public TreeRenderer(ITreeTableModel treeTableModel)
		{
			super();
			this.treeTableModel = treeTableModel;
			LookAndFeelStateAdapter.getInstance().addObserver(this);
		}
		
		
		@Override
		public void onLookAndFeelChanged()
		{
			setUI(UIManager.getUI(this));
			
			setOpenIcon(UIManager.getIcon("Tree.openIcon"));
			setLeafIcon(UIManager.getIcon("Tree.leafIcon"));
			setClosedIcon(UIManager.getIcon("Tree.closedIcon"));
		}
		
		
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
				boolean isLeaf, int row, boolean hasFocus)
		{
			final Component comp = super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row,
					hasFocus);
			
			// Let the model change whatever it wants
			final JLabel label = (JLabel) comp;
			// DefaultTreeCellRenderer uses a JLabel!
			treeTableModel.renderTreeCellComponent(label, value);
			
			return comp;
		}
	}
}