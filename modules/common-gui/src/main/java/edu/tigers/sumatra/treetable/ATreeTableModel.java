/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.treetable;


import lombok.Setter;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * Base implementation of {@link ITreeTableModel}. Provides an implementation of the necessary change-propagation.
 *
 * @author Gero
 * @see JTreeTable
 */
public abstract class ATreeTableModel implements ITreeTableModel
{
	private Object root;
	private EventListenerList listenerList = new EventListenerList();

	@Setter
	private List<String> searchWords;


	/**
	 * @param root
	 */
	protected ATreeTableModel(final Object root)
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


	@Override
	public Stream<NodeNameAndObjectTreePath> getAllTreePaths()
	{
		return getAllTreePaths(getRoot(), List.of())
				.map(this::buildTreePathPair)
				.flatMap(Optional::stream)
				.sorted(Comparator.comparingInt(pair -> -pair.nodeNamePath().getPathCount()));
	}


	@Override
	public Optional<TreePath> getNodeNameTreePathToRoot(Object node)
	{
		return getAllTreePaths(getRoot(), List.of())
				.filter(path -> path.getLastPathComponent().equals(node))
				.map(this::getNodeNameTreePathFromObjectTreePath)
				.flatMap(Optional::stream)
				.findAny();
	}


	@Override
	public Optional<TreePath> getNodeNameTreePathFromObjectTreePath(TreePath path)
	{
		var objectNames = new ArrayList<String>(path.getPathCount());
		for (var p : path.getPath())
		{
			var name = nodeToString(p);
			if (name.isEmpty())
			{
				return Optional.empty();
			}
			objectNames.add(name.get());
		}
		return Optional.of(new TreePath(objectNames.toArray()));
	}


	protected abstract Optional<String> nodeToString(Object node);


	private Optional<NodeNameAndObjectTreePath> buildTreePathPair(TreePath objectPath)
	{
		return getNodeNameTreePathFromObjectTreePath(objectPath).map(
				namPath -> new NodeNameAndObjectTreePath(namPath, objectPath));
	}


	protected Stream<TreePath> getAllTreePaths(Object node, List<Object> parentPath)
	{
		var currentPath = new ArrayList<>(parentPath);
		currentPath.add(node);
		return Stream.concat(
				IntStream.range(0, getChildCount(node))
						.mapToObj(i -> getChild(node, i))
						.flatMap(child -> getAllTreePaths(child, currentPath)),
				Stream.of(new TreePath(currentPath.toArray()))
		);
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
