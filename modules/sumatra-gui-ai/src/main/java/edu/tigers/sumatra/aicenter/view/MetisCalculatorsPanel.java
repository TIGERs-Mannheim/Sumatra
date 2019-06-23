/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 21, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.aicenter.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.metis.ECalculator;


/**
 * Panel for selecting metis calculators
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MetisCalculatorsPanel extends JPanel
{
	/**  */
	private static final long serialVersionUID = 9125108113440167202L;
	
	private final JTable table;
	private final transient List<ICalculatorObserver> observers = new CopyOnWriteArrayList<>();
	
	
	/**
	 * Creates a new MetisCalculatorsPanel
	 */
	public MetisCalculatorsPanel()
	{
		setLayout(new BorderLayout());
		TableModel model = new TableModel(new String[] { "active", "calculator", "executed", "time [us]" },
				ECalculator.values().length);
		table = new JTable(model);
		table.setEnabled(false);
		
		DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer();
		textRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(3).setCellRenderer(textRenderer);
		
		table.getModel().addTableModelListener(new MyTableModelListener());
		
		JScrollPane scrlPane = new JScrollPane(table);
		scrlPane.setPreferredSize(new Dimension(0, 0));
		add(scrlPane, BorderLayout.CENTER);
		
		reset();
	}
	
	
	/**
	 * Set all values to default
	 */
	private void reset()
	{
		int row = 0;
		for (ECalculator eCalc : ECalculator.values())
		{
			table.getModel().setValueAt(eCalc.isInitiallyActive(), row, 0);
			table.getModel().setValueAt(eCalc.name(), row, 1);
			table.getModel().setValueAt(false, row, 2);
			table.getModel().setValueAt(0, row, 3);
			row++;
		}
	}
	
	
	/**
	 * @param active
	 */
	public void setActive(final boolean active)
	{
		reset();
		table.setEnabled(active);
	}
	
	
	/**
	 * @param o
	 */
	public void addObserver(final ICalculatorObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(final ICalculatorObserver o)
	{
		observers.remove(o);
	}
	
	
	/**
	 * New AIInfoFrame.
	 * 
	 * @param lastAIInfoframe
	 */
	public void updateAIInfoFrame(final AIInfoFrame lastAIInfoframe)
	{
		int row = 0;
		for (ECalculator eCalc : ECalculator.values())
		{
			Integer value = lastAIInfoframe.getTacticalField().getMetisCalcTimes().get(eCalc);
			boolean execution = lastAIInfoframe.getTacticalField().getMetisExecutionStatus().get(eCalc);
			if (value == null)
			{
				continue;
			}
			table.getModel().setValueAt(execution, row, 2);
			int lastValue = (int) table.getModel().getValueAt(row, 3);
			table.getModel().setValueAt((lastValue + value) / 2, row, 3);
			row++;
		}
	}
	
	
	private static class TableModel extends DefaultTableModel
	{
		/**  */
		private static final long serialVersionUID = -3700170198897729348L;
		
		
		private TableModel(final Object[] columnNames, final int rowCount)
		{
			super(columnNames, rowCount);
		}
		
		
		@Override
		public Class<?> getColumnClass(final int columnIndex)
		{
			if (columnIndex == 0 || columnIndex == 2)
			{
				return Boolean.class;
			}
			return super.getColumnClass(columnIndex);
		}
		
		
		@Override
		public boolean isCellEditable(final int row, final int column)
		{
			return column == 0;
		}
	}
	
	private class MyTableModelListener implements TableModelListener
	{
		@Override
		public void tableChanged(final TableModelEvent e)
		{
			if (table.isEnabled() && (e.getColumn() == 0))
			{
				int row = e.getFirstRow();
				ECalculator eCalc = ECalculator.valueOf(table.getModel().getValueAt(row, 1).toString());
				boolean active = (Boolean) table.getModel().getValueAt(row, 0);
				
				for (ICalculatorObserver o : observers)
				{
					o.onCalculatorStateChanged(eCalc, active);
				}
			}
		}
	}
}
