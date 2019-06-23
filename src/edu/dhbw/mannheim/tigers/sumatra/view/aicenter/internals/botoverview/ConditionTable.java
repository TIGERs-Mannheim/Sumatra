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
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.lang.StringEscapeUtils;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.DummyCondition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.ECondition;


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
		
		ConditionCellRenderer conditionCellRenderer = new ConditionCellRenderer();
		setDefaultRenderer(ACondition.class, conditionCellRenderer);
		
		setEnabled(true);
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public static class ConditionTableModel extends AbstractTableModel
	{
		private static final long			serialVersionUID	= 2212173179587810981L;
		
		private final List<ACondition>	content				= new ArrayList<ACondition>();
		
		private static final String[]		COLUMN_NAMES		= new String[] { "Condition" };
		private static final Class<?>[]	COLUMN_CLASSES		= new Class<?>[] { ACondition.class };
		
		
		/**
		 * @param newConditions
		 */
		public void updateEntireTable(final List<ACondition> newConditions)
		{
			synchronized (content)
			{
				content.clear();
				content.addAll(newConditions);
			}
			
			fireTableDataChanged();
		}
		
		
		/**
		 */
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
				try
				{
					return content.get(row);
				} catch (final IndexOutOfBoundsException e)
				{
					return new DummyCondition(ECondition.DUMMY);
				}
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
		private String					oldText				= "";
		
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column)
		{
			final ACondition newCon = (ACondition) value;
			final String newText = newCon.getCondition();
			final EConditionState newStatus = newCon.getLastConditionResult();
			final String toolTipText = newCon.getType().name();
			
			if (!oldText.equals(newText))
			{
				oldText = newText;
				final int colWidth = 18;
				int count = 1;
				
				final StringBuilder sb = new StringBuilder();
				for (int i = 0; i < newText.length(); i++)
				{
					sb.append(newText.charAt(i));
					if (((i + 1) % colWidth) == 0)
					{
						sb.append("\n");
						count++;
					}
				}
				
				table.setRowHeight(row, 13 * count);
				setText("<html>" + StringEscapeUtils.escapeHtml(sb.toString()) + "</html>");
			}
			
			setVerticalAlignment(TOP);
			setFont(new Font("monospaced", Font.PLAIN, 9));
			
			if (!toolTipText.equals(getToolTipText()))
			{
				setToolTipText(toolTipText);
			}
			
			if (newStatus == EConditionState.NOT_CHECKED)
			{
				setBackground(UNCHECKED_CONDITION_CLR);
			} else if (newStatus == EConditionState.FULFILLED)
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
	/**
	 * @return
	 */
	public ConditionTableModel getConditionModel()
	{
		return conditionModel;
	}
}
