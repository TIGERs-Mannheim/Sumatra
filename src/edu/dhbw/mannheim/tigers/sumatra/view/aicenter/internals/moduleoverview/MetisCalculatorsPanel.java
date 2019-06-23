/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 21, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

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

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ECalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;


/**
 * Panel for selecting metis calculators
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MetisCalculatorsPanel extends JPanel implements IAIObserver
{
	/**  */
	private static final long						serialVersionUID	= 9125108113440167202L;
	
	private final JTable								table;
	private final List<ICalculatorObserver>	observers			= new CopyOnWriteArrayList<>();
	
	
	/**
	  * 
	  */
	public MetisCalculatorsPanel()
	{
		setLayout(new BorderLayout());
		TableModel model = new TableModel(new String[] { "active", "calculator", "time [us]" },
				ECalculator.values().length);
		table = new JTable(model);
		table.setEnabled(false);
		
		DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer();
		textRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(2).setCellRenderer(textRenderer);
		
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
			table.getModel().setValueAt(0, row, 2);
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
	
	
	private void notifyObserver(final ECalculator eCalc, final boolean active)
	{
		for (ICalculatorObserver o : observers)
		{
			o.onCalculatorStateChanged(eCalc, active);
		}
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
	
	
	@Override
	public void onNewAIInfoFrame(final IRecordFrame lastAIInfoframe)
	{
		// if (!table.isEnabled())
		// {
		// return;
		// }
		int row = 0;
		for (ECalculator eCalc : ECalculator.values())
		{
			Integer value = lastAIInfoframe.getTacticalField().getMetisCalcTimes().get(eCalc);
			if (value == null)
			{
				continue;
			}
			int lastValue = (int) table.getModel().getValueAt(row, 2);
			table.getModel().setValueAt((lastValue + value) / 2, row, 2);
			row++;
		}
	}
	
	
	@Override
	public void onAIException(final Throwable ex, final IRecordFrame frame, final IRecordFrame prevFrame)
	{
	}
	
	private static class TableModel extends DefaultTableModel
	{
		/**  */
		private static final long	serialVersionUID	= -3700170198897729348L;
		
		
		private TableModel(final Object[] columnNames, final int rowCount)
		{
			super(columnNames, rowCount);
		}
		
		
		@Override
		public Class<?> getColumnClass(final int columnIndex)
		{
			if (columnIndex == 0)
			{
				return Boolean.class;
			}
			return super.getColumnClass(columnIndex);
		}
		
		
		@Override
		public boolean isCellEditable(final int row, final int column)
		{
			if (column == 0)
			{
				return true;
			}
			return false;
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
				notifyObserver(eCalc, active);
			}
		}
	}
}
