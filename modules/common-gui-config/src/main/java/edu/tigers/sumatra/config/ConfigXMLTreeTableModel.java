/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.config;

import com.github.g3force.s2vconverter.String2ValueConverter;
import edu.tigers.sumatra.treetable.ATreeTableModel;
import edu.tigers.sumatra.treetable.ITreeTableModel;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration.Node;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * An {@link ATreeTableModel} implementation based on a {@link HierarchicalConfiguration}.
 *
 * @author Gero
 */
public class ConfigXMLTreeTableModel extends ATreeTableModel
{
	private static final String[] COLUMNS = new String[] { "Node", "Value", "Comment" };
	private static final Class<?>[] CLASSES = new Class<?>[] { ITreeTableModel.class, String.class, String.class };


	private final String2ValueConverter s2vConv = String2ValueConverter.getDefault();


	public ConfigXMLTreeTableModel(final HierarchicalConfiguration xml)
	{
		// Hopefully there is no comment as first element... :-P
		super(xml.getRoot());
	}


	@Override
	public Object getValueAt(final Object obj, final int col)
	{
		if (col == 0)
		{
			// is handled by TreeCellRenderer!!!
			return null;
		}

		final Node node = (Node) obj;
		Object result = "";
		switch (col)
		{
			case 1:
				final Object val = node.getValue();
				if (val != null)
				{
					for (ConfigurationNode attr : node.getAttributes("class"))
					{
						Class<?> classType = String2ValueConverter.getClassFromValue(attr.getValue());
						if (classType.isEnum() || (classType == Boolean.TYPE) || (classType == Boolean.class))
						{
							result = s2vConv.parseString(classType, val.toString());
						} else
						{
							result = val.toString();
						}
						break;
					}
				}
				break;

			case 2:
				for (ConfigurationNode attr : node.getAttributes("comment"))
				{
					result = attr.getValue().toString();
					break;
				}
				final org.w3c.dom.Node comment = getComment(node);
				if (comment != null)
				{
					result += " " + comment.getTextContent();
				}
				break;
			default:
				throw new IllegalArgumentException("Invalid value for col: " + col);
		}
		return result;
	}


	@Override
	public void renderTreeCellComponent(final JLabel label, final Object value)
	{
		final Node node = (Node) value;
		label.setText(node.getName());
	}


	@Override
	public Object getChild(final Object obj, final int index)
	{
		final Node node = (Node) obj;
		final List<?> list = node.getChildren();
		if ((list == null) || (list.size() <= index))
		{
			// Should not happen!
			return null;
		}
		return list.get(index);
	}


	@Override
	public int getIndexOfChild(final Object parentObj, final Object childObj)
	{
		final Node node = (Node) parentObj;
		final List<?> children = node.getChildren();
		for (int i = 0; i < children.size(); i++)
		{
			if (children.get(i).equals(childObj))
			{
				return i;
			}
		}
		// Not found!
		return -1;
	}


	@Override
	public int getChildCount(final Object obj)
	{
		final Node node = (Node) obj;
		return node.getChildrenCount();
	}


	@Override
	public void setValueAt(final Object value, final Object obj, final int col)
	{
		final Node node = (Node) obj;
		switch (col)
		{
			case 1:
				node.setValue(value);
				fireTreeNodesChanged(this, getPathTo(node), new int[0], new Object[0]);
				break;

			case 2:
				final org.w3c.dom.Node comment = getComment(obj);
				if (comment != null)
				{
					comment.setTextContent(value.toString());
					fireTreeNodesChanged(this, getPathTo(node), new int[0], new Object[0]);
				}
				break;
			default:
				throw new IllegalArgumentException();
		}
	}


	@Override
	public String getToolTipText(final MouseEvent event)
	{
		final JTable table = (JTable) event.getSource();
		final int row = table.rowAtPoint(event.getPoint());

		// Always show the comment as tooltip
		final Object obj = table.getValueAt(row, 2);
		// This is the value which is actually displayed in the table: a String!
		final String value = (String) obj;

		if (!value.isEmpty())
		{
			return value;
		}
		return null;
	}


	// --------------------------------------------------------------------------
	// --- local helper functions -----------------------------------------------
	// --------------------------------------------------------------------------
	private Object[] getPathTo(final Node node)
	{
		// Gather path elements
		final List<Node> list = new LinkedList<>();

		Node parent = node.getParent();
		while (parent != null)
		{
			list.add(parent);
			parent = parent.getParent();
		}

		// Reverse order
		final Iterator<Node> it = list.iterator();
		final Object[] result = new Object[list.size()];
		for (int i = list.size() - 1; i >= 0; i--)
		{
			result[i] = it.next();
		}

		return result;
	}


	/**
	 * This function returns the comment which is associated (means: is the next before/above) with the given
	 * {@link Node} (as Object, for easier usage). If there no parent or another ELEMENT_NODE is reached.<br/>
	 * The general problem is that the {@link XMLConfiguration} this model is based on does not contain any comments, but
	 * access to the underlying {@link org.w3c.dom.Document} which is used to load the configuration initially.
	 *
	 * @param obj
	 * @return The associated comment-node (or <code>null</code>)
	 */
	private org.w3c.dom.Node getComment(final Object obj)
	{
		final Node xmlNode = (Node) obj;
		final org.w3c.dom.Node node = (org.w3c.dom.Node) xmlNode.getReference();
		if (node == null)
		{
			return null;
		}
		org.w3c.dom.Node prevSibl = node.getPreviousSibling();
		while (prevSibl != null)
		{
			if (prevSibl.getNodeType() == org.w3c.dom.Node.COMMENT_NODE)
			{
				return prevSibl;
			}
			if (prevSibl.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
			{
				return null;
			}

			prevSibl = prevSibl.getPreviousSibling();
		}

		return null;
	}


	@Override
	protected Stream<TreePath> getAllTreePaths(Object node, List<Object> parentPath)
	{
		if (node instanceof ConfigurationNode configNode)
		{
			var currentPath = new ArrayList<>(parentPath);
			currentPath.add(configNode);

			return Stream.concat(
					configNode.getChildren().stream().flatMap(child -> getAllTreePaths(child, currentPath)),
					Stream.of(new TreePath(currentPath.toArray()))
			);
		}
		return Stream.of();
	}


	@Override
	public int getColumnCount()
	{
		return COLUMNS.length;
	}


	@Override
	public String getColumnName(final int col)
	{
		return COLUMNS[col];
	}


	@Override
	public Class<?> getColumnClass(final int col)
	{
		return CLASSES[col];
	}


	@Override
	protected Optional<String> nodeToString(Object node)
	{
		if (node instanceof ConfigurationNode configNode)
		{
			return Optional.ofNullable(configNode.getName());
		}
		return Optional.empty();
	}
}
