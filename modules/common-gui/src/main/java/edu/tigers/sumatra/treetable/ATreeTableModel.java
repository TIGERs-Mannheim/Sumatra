/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.treetable;


import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;


/**
 * Base implementation of {@link ITreeTableModel}. Provides an implementation of the necessary change-propagation.
 * 
 * @author Gero
 * @see JTreeTable
 */
public abstract class ATreeTableModel implements ITreeTableModel
{
	protected Object					root;
	protected EventListenerList	listenerList	= new EventListenerList();
	
	private boolean					editable			= true;
	
	
	/**
	 * @param root
	 */
	public ATreeTableModel(final Object root)
	{
		this.root = root;
	}
	
	
	@Override
	public Object getRoot()
	{
		return root;
	}
	
	
	@Override
	public boolean isLeaf(final Object obj)
	{
		return getChildCount(obj) == 0;
	}
	
	
	@Override
	public void valueForPathChanged(final TreePath paramTreePath, final Object paramObject)
	{
		
	}
	
	
	/**
	 * By default, make the column with the Tree in it the only editable one.
	 * Making this column editable causes the JTable to forward mouse
	 * and keyboard events in the Tree column to the underlying JTree.
	 */
	@Override
	public boolean isCellEditable(final Object node, final int column)
	{
		return getColumnClass(column) == ITreeTableModel.class;
	}
	
	
	@Override
	public void setEditable(final boolean editable)
	{
		this.editable = editable;
	}
	
	
	@Override
	public boolean isEditable()
	{
		return editable;
	}
	
	
	@Override
	public void addTreeModelListener(final TreeModelListener l)
	{
		listenerList.add(TreeModelListener.class, l);
	}
	
	
	@Override
	public void removeTreeModelListener(final TreeModelListener l)
	{
		listenerList.remove(TreeModelListener.class, l);
	}
	
	
	/*
	 * Notify all listeners that have registered interest for
	 * notification on this event type. The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 * @see EventListenerList
	 */
	protected void fireTreeNodesChanged(final Object source, final Object[] path, final int[] childIndices,
			final Object[] children)
	{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if (listeners[i] == TreeModelListener.class)
			{
				// Lazily create the event:
				if (e == null)
				{
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
			}
		}
	}
	
	
	/*
	 * Notify all listeners that have registered interest for
	 * notification on this event type. The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 * @see EventListenerList
	 */
	protected void fireTreeNodesInserted(final Object source, final Object[] path, final int[] childIndices,
			final Object[] children)
	{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if (listeners[i] == TreeModelListener.class)
			{
				// Lazily create the event:
				if (e == null)
				{
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
			}
		}
	}
	
	
	/*
	 * Notify all listeners that have registered interest for
	 * notification on this event type. The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 * @see EventListenerList
	 */
	protected void fireTreeNodesRemoved(final Object source, final Object[] path, final int[] childIndices,
			final Object[] children)
	{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if (listeners[i] == TreeModelListener.class)
			{
				// Lazily create the event:
				if (e == null)
				{
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
			}
		}
	}
	
	
	/*
	 * Notify all listeners that have registered interest for
	 * notification on this event type. The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 * @see EventListenerList
	 */
	protected void fireTreeStructureChanged(final Object source, final Object[] path, final int[] childIndices,
			final Object[] children)
	{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if (listeners[i] == TreeModelListener.class)
			{
				// Lazily create the event:
				if (e == null)
				{
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
			}
		}
	}
}
