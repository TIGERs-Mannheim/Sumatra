/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.botparams.view;

import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.tigers.sumatra.treetable.ATreeTableModel;
import edu.tigers.sumatra.treetable.ITreeTableModel;


/**
 * An {@link ATreeTableModel} implementation based on a {@link JsonNode}.
 * 
 * @author AndreR
 */
public class ConfigJSONTreeTableModel extends ATreeTableModel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final String[]		COLUMNS	= new String[] { "Team", "Value" };
	private static final Class<?>[]	CLASSES	= new Class<?>[] { ITreeTableModel.class, String.class };
	
	/**
	 * An entry of the JSON tree.
	 */
	public static class TreeEntry
	{
		private JsonNode	node;
		private String		name;
		private TreeEntry	parent;
		
		
		private TreeEntry(final JsonNode node, final TreeEntry parent, final String name)
		{
			this.node = node;
			this.parent = parent;
			this.name = name;
		}
		
		
		/**
		 * @return the node
		 */
		public JsonNode getNode()
		{
			return node;
		}
		
		
		/**
		 * @return the name
		 */
		public String getName()
		{
			return name;
		}
		
		
		/**
		 * @return the parent
		 */
		public TreeEntry getParent()
		{
			return parent;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param json
	 */
	public ConfigJSONTreeTableModel(final JsonNode json)
	{
		super(new TreeEntry(json, null, "root"));
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public Object getValueAt(final Object obj, final int col)
	{
		if (col == 0)
		{
			// is handled by TreeCellRenderer!!!
			return null;
		}
		
		if (col > 1)
		{
			throw new IllegalArgumentException("Invalid value for col: " + col);
		}
		
		final TreeEntry entry = (TreeEntry) obj;
		return entry.node.asText();
	}
	
	
	@Override
	public void renderTreeCellComponent(final JLabel label, final Object value)
	{
		final TreeEntry entry = (TreeEntry) value;
		label.setText(entry.name);
	}
	
	
	@Override
	public Object getChild(final Object obj, final int index)
	{
		final TreeEntry entry = (TreeEntry) obj;
		if (index >= entry.node.size())
		{
			// Should not happen!
			return null;
		}
		
		Iterator<Map.Entry<String, JsonNode>> iter = entry.node.fields();
		Map.Entry<String, JsonNode> child = iter.next();
		
		for (int i = 0; i < index; i++)
		{
			child = iter.next();
		}
		
		return new TreeEntry(child.getValue(), entry, child.getKey());
	}
	
	
	@Override
	public int getIndexOfChild(final Object parentObj, final Object childObj)
	{
		final TreeEntry parentEntry = (TreeEntry) parentObj;
		final TreeEntry childEntry = (TreeEntry) childObj;
		
		int index = 0;
		
		Iterator<JsonNode> iter = parentEntry.node.elements();
		while (iter.hasNext())
		{
			JsonNode child = iter.next();
			if (child.equals(childEntry.node))
			{
				return index;
			}
			
			index++;
		}
		
		// Not found!
		return -1;
	}
	
	
	@Override
	public int getChildCount(final Object obj)
	{
		final TreeEntry entry = (TreeEntry) obj;
		return entry.node.size();
	}
	
	
	@Override
	public boolean isCellEditable(final Object obj, final int col)
	{
		// 0.0 = "Name"
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
		
		return isLeaf(obj);
	}
	
	
	@Override
	public void setValueAt(final Object value, final Object obj, final int col)
	{
		final TreeEntry entry = (TreeEntry) obj;
		if (col != 1)
		{
			throw new IllegalArgumentException();
		}
		
		ObjectNode parentNode = (ObjectNode) entry.parent.node;
		if (entry.node.isNumber())
		{
			parentNode.put(entry.name, Double.valueOf((String) value));
		} else
		{
			parentNode.put(entry.name, (String) value);
		}
		
		entry.node = parentNode.get(entry.name);
		
		fireTreeNodesChanged(this, getPathTo(entry), new int[0], new Object[0]);
	}
	
	
	@Override
	public String getToolTipText(final MouseEvent event)
	{
		return null;
	}
	
	
	// --------------------------------------------------------------------------
	// --- local helper functions -----------------------------------------------
	// --------------------------------------------------------------------------
	private Object[] getPathTo(final TreeEntry entry)
	{
		// Gather path elements
		final List<TreeEntry> list = new LinkedList<>();
		
		TreeEntry parent = entry;
		while (parent != null)
		{
			list.add(parent);
			parent = parent.parent;
		}
		
		// Reverse order
		final Iterator<TreeEntry> it = list.iterator();
		final Object[] result = new Object[list.size()];
		for (int i = list.size() - 1; i >= 0; i--)
		{
			result[i] = it.next();
		}
		
		return result;
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
	public String getColumnName(final int col)
	{
		return COLUMNS[col];
	}
	
	
	@Override
	public Class<?> getColumnClass(final int col)
	{
		return CLASSES[col];
	}
}
