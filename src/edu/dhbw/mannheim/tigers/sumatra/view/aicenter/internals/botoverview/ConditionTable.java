/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.05.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.botoverview;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;


/**
 * This class displays the {@link ACondition} of a role in a single column.
 * 
 * @author Gero
 */
public class ConditionTable extends JTable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long				serialVersionUID			= 8677282421109655188L;
	
	private static final Color				UNCHECKED_CONDITION_CLR	= Color.LIGHT_GRAY;
	private static final Color				TRUE_CONDITION_CLR		= Color.GREEN;
	private static final Color				FALSE_CONDITION_CLR		= Color.RED;
	
	private final ConditionTableModel	conditionModel;
	private final ConditionCellRenderer	conditionCellRenderer;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * 
	  */
	public ConditionTable()
	{
		super(new ConditionTableModel());
		conditionModel = (ConditionTableModel) getModel();
		
		conditionCellRenderer = new ConditionCellRenderer();
		setDefaultRenderer(ACondition.class, conditionCellRenderer);
		
		setEnabled(false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public static class ConditionTableModel extends AbstractTableModel
	{
		private static final long			serialVersionUID	= 2212173179587810981L;
		
		private final List<ACondition>	content				= new ArrayList<ACondition>();
		
		private static final String[]		COLUMN_NAMES		= new String[] { "Condition" };
		private static final Class<?>[]	COLUMN_CLASSES		= new Class<?>[] { ACondition.class };
		
		
		public void updateEntireTable(final List<ACondition> newConditions)
		{
			synchronized (content)
			{
				content.clear();
				content.addAll(newConditions);
			}
			
//			fireTableDataChanged();
		}
		

//		public void updateConditionsStatus()
//		{
//			fireTableDataChanged();
//		}
		

		public void resetContent()
		{
			synchronized (content)
			{
				content.clear();
				fireTableDataChanged();
			}
		}
		

		@Override
		public Object getValueAt(int row, int col)
		{
			synchronized (content)
			{
				return content.get(row);
			}
		}
		

		@Override
		public String getColumnName(int col)
		{
			return COLUMN_NAMES[col];
		}
		

		@Override
		public Class<?> getColumnClass(int col)
		{
			return COLUMN_CLASSES[col];
		}
		

		@Override
		public int getRowCount()
		{
			synchronized (content)
			{
				return content.size();
			}
		}
		

		@Override
		public int getColumnCount()
		{
			return COLUMN_NAMES.length;
		}
	}
	
	
	private static class ConditionCellRenderer extends DefaultTableCellRenderer
	{
		private static final long	serialVersionUID	= -5331145585820015736L;
		
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column)
		{
			final ACondition newCon = (ACondition) value;
			final String newText = newCon.getType().name();
			final Boolean newStatus = newCon.getLastConditionResult();
			
			if (!newText.equals(getText()))
			{
				setText(newText);
			}
			
			if (newStatus == null)
			{
				setBackground(UNCHECKED_CONDITION_CLR);
			} else if (newStatus)
			{
				setBackground(TRUE_CONDITION_CLR);
			} else
			{
				setBackground(FALSE_CONDITION_CLR);
			}
			
			return this;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public ConditionTableModel getConditionModel()
	{
		return conditionModel;
	}
}
