/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.11.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.config;

import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;

import org.apache.commons.configuration.HierarchicalConfiguration.Node;
import org.apache.commons.configuration.XMLConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.view.commons.treetable.ATreeTableModel;
import edu.dhbw.mannheim.tigers.sumatra.view.commons.treetable.ITreeTableModel;


/**
 * An {@link ATreeTableModel} implementation based on a {@link XMLConfiguration}.
 * 
 * @author Gero
 */
public class ConfigXMLTreeTableModel extends ATreeTableModel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final String[]		COLUMNS	= new String[] { "Node", "Value", "Comment" };
	private static final Class<?>[]	CLASSES	= new Class<?>[] { ITreeTableModel.class, String.class, String.class };
	
	
	private final XMLConfiguration	xml;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param xml
	 */
	public ConfigXMLTreeTableModel(XMLConfiguration xml)
	{
		// Hopefully there is no comment as first element... :-P
		super(xml.getRoot());
		this.xml = xml;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public Object getValueAt(Object obj, int col)
	{
		if (col == 0)
		{
			// is handled by TreeCellRenderer!!!
			return null;
		}
		
		final Node node = (Node) obj;
		String result = "";
		switch (col)
		{
			case 1:
				final Object val = node.getValue();
				result = val == null ? "" : val.toString();
				break;
			
			case 2:
				final org.w3c.dom.Node comment = getComment(node);
				if (comment != null)
				{
					result = comment.getTextContent();
				}
				break;
		}
		return result;
	}
	
	
	@Override
	public void renderTreeCellComponent(JLabel label, Object value)
	{
		final Node node = (Node) value;
		label.setText(node.getName());
	}
	
	
	@Override
	public Object getChild(Object obj, int index)
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
	public int getIndexOfChild(Object parentObj, Object childObj)
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
	public int getChildCount(Object obj)
	{
		final Node node = (Node) obj;
		return node.getChildrenCount();
	}
	
	
	@Override
	public boolean isCellEditable(Object obj, int col)
	{
		// 0 = "Name"
		if (col == 0)
		{
			// For tree-expansion/collapse
			return super.isCellEditable(obj, col);
		}
		
		if (!isEditable())
		{
			// Editing disabled
			return false;
		}
		
		switch (col)
		{
		
		// "Value"
			case 1:
				return isLeaf(obj);
				
				// "Comment"
			case 2:
				final org.w3c.dom.Node comment = getComment(obj);
				return comment != null;
		}
		return false;
	}
	
	
	@Override
	public void setValueAt(Object value, Object obj, int col)
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
		}
	}
	
	
	@Override
	public String getToolTipText(MouseEvent event)
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
	private Object[] getPathTo(Node node)
	{
		// Gather path elements
		final List<Node> list = new LinkedList<Node>();
		
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
	private org.w3c.dom.Node getComment(Object obj)
	{
		final Node xmlNode = (Node) obj;
		final org.w3c.dom.Node node = (org.w3c.dom.Node) xmlNode.getReference();
		
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
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public int getColumnCount()
	{
		return COLUMNS.length;
	}
	
	
	@Override
	public String getColumnName(int col)
	{
		return COLUMNS[col];
	}
	
	
	@Override
	public Class<?> getColumnClass(int col)
	{
		return CLASSES[col];
	}
	
	
	/**
	 * @return The {@link org.w3c.dom.Document} the model is based on
	 */
	public XMLConfiguration getXMLConfiguration()
	{
		return xml;
	}
}
