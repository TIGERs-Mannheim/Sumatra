/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.treetable;

import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.tree.TreeModel;


/**
 * The basic interface for any models for the {@link JTreeTable}. It features cell editing, customized rendering and
 * customized tooltips. The base implementation is {@link ATreeTableModel}.
 * 
 * @author Gero
 * @see JTreeTable
 */
public interface ITreeTableModel extends TreeModel
{
	/**
	 * Returns the number ofs availible column.
	 * 
	 * @return
	 */
	int getColumnCount();
	
	
	/**
	 * Returns the name for column number <code>column</code>.
	 * 
	 * @param column
	 * @return
	 */
	String getColumnName(int column);
	
	
	/**
	 * Returns the type for column number <code>column</code>.
	 * 
	 * @param column
	 * @return
	 */
	Class<?> getColumnClass(int column);
	
	
	/**
	 * Returns the value to be displayed for node <code>node</code>,
	 * at column number <code>column</code>.
	 * 
	 * @param node
	 * @param column
	 * @return
	 */
	Object getValueAt(Object node, int column);
	
	
	/**
	 * Indicates whether the the value for node <code>node</code>,
	 * at column number <code>column</code> is editable.
	 * 
	 * @param node
	 * @param column
	 * @return
	 */
	boolean isCellEditable(Object node, int column);
	
	
	/**
	 * @param editable
	 */
	void setEditable(boolean editable);
	
	
	/**
	 * @return model is editable (default: <code>true</code>)
	 */
	boolean isEditable();
	
	
	/**
	 * Sets the value for node <code>node</code>,
	 * at column number <code>column</code>.
	 * 
	 * @param aValue
	 * @param node
	 * @param column
	 */
	void setValueAt(Object aValue, Object node, int column);
	
	
	/**
	 * This lets the model decide whether it wants to add/change something of the things the
	 * {@link javax.swing.tree.DefaultTreeCellRenderer} does
	 * 
	 * @param label
	 * @param node
	 */
	void renderTreeCellComponent(JLabel label, Object node);
	
	
	/**
	 * Forwarded from {@link javax.swing.JTable#getToolTipText(MouseEvent)}; called by the {@link javax.swing.JTable}s
	 * {@link javax.swing.ToolTipManager}.
	 * 
	 * @param event
	 * @return The tooltip for this mouse-event. <code>null</code> if none.
	 */
	String getToolTipText(MouseEvent event);
}
