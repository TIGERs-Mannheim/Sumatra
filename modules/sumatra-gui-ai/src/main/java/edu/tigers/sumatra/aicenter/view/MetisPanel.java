/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.aicenter.view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import edu.tigers.sumatra.ai.metis.ECalculator;
import edu.tigers.sumatra.components.RoundedNumberRenderer;


/**
 * Panel for selecting metis calculators
 */
public class MetisPanel extends JPanel
{
	private static final long serialVersionUID = 9125108113440167202L;
	public static final int COL_ACTIVE = 0;
	public static final int COL_CALCULATOR = 1;
	public static final int COL_TIME_REL = 2;
	public static final int COL_TIME_AVG = 3;
	public static final int COL_EXECUTED = 4;

	private static final String[] COLUMN_NAMES = {
			"active",
			"calculator",
			"time [% of metis]",
			"avg. time [us]",
			"executed" };

	private final JTable table;
	private final JCheckBox automaticReorderingCheckBox = new JCheckBox("Automatic Reordering");
	private final JButton resetButton = new JButton("Reset Averages");


	public MetisPanel()
	{
		setLayout(new BorderLayout());
		TableModel model = new TableModel(COLUMN_NAMES, ECalculator.values().length);
		table = new JTable(model);
		table.setEnabled(false);
		table.setAutoCreateRowSorter(true);

		DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer();
		RoundedNumberRenderer numberRenderer = new RoundedNumberRenderer();
		textRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		numberRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(COL_TIME_AVG).setCellRenderer(numberRenderer);
		table.getColumnModel().getColumn(COL_TIME_REL).setCellRenderer(numberRenderer);


		JScrollPane scrlPane = new JScrollPane(table);
		scrlPane.setPreferredSize(new Dimension(0, 0));
		add(scrlPane, BorderLayout.CENTER);

		JPanel topBar = new JPanel();
		topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS));
		topBar.add(automaticReorderingCheckBox);
		topBar.add(resetButton);

		add(topBar, BorderLayout.NORTH);

		reset();
	}


	public JButton getResetButton()
	{
		return resetButton;
	}


	public JTable getTable()
	{
		return table;
	}


	public JCheckBox getAutomaticReorderingCheckBox()
	{
		return automaticReorderingCheckBox;
	}


	/**
	 * Set all values to default
	 */
	public void reset()
	{
		int row = 0;
		for (ECalculator eCalc : ECalculator.values())
		{
			table.getModel().setValueAt(eCalc.isInitiallyActive(), row, COL_ACTIVE);
			table.getModel().setValueAt(eCalc.name(), row, COL_CALCULATOR);
			table.getModel().setValueAt(0, row, COL_TIME_REL);
			row++;
		}
	}


	public void setActive(final boolean active)
	{
		reset();
		table.setEnabled(active);
	}

	private static class TableModel extends DefaultTableModel
	{
		private static final long serialVersionUID = -3700170198897729348L;


		private TableModel(final Object[] columnNames, final int rowCount)
		{
			super(columnNames, rowCount);
		}


		@Override
		public Class<?> getColumnClass(final int columnIndex)
		{
			if (columnIndex == COL_ACTIVE || columnIndex == COL_EXECUTED)
			{
				return Boolean.class;
			} else if (columnIndex == COL_CALCULATOR)
			{
				return String.class;
			} else if (columnIndex == COL_TIME_AVG || columnIndex == COL_TIME_REL)
			{
				return Float.class;
			}

			return super.getColumnClass(columnIndex);
		}


		@Override
		public boolean isCellEditable(final int row, final int column)
		{
			return column == COL_ACTIVE;
		}
	}
}
