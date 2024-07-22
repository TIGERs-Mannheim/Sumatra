/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.aicenter.view;

import edu.tigers.sumatra.components.RoundedNumberRenderer;
import edu.tigers.sumatra.util.ScalingUtil;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Objects;


/**
 * Panel for selecting metis calculators
 */
public class MetisPanel extends JPanel
{
	private static final long serialVersionUID = 9125108113440167202L;
	public static final int COL_CALCULATOR = 0;
	public static final int COL_TIME_REL = 1;
	public static final int COL_TIME_AVG = 2;
	public static final int COL_EXECUTED = 3;

	private static final String[] COLUMN_NAMES = {
			"calculator",
			"time [% of metis]",
			"avg. time [ms]",
			"executed" };

	private final JTable table;
	private final MetisTableModel model;
	private final JCheckBox automaticReorderingCheckBox = new JCheckBox("Automatic Reordering");
	private final JButton resetButton = new JButton("Reset Averages");


	public MetisPanel()
	{
		setLayout(new BorderLayout());
		model = new MetisTableModel(COLUMN_NAMES, 0);
		table = new JTable(model);
		table.setEnabled(false);
		table.setAutoCreateRowSorter(true);
		table.setRowHeight(ScalingUtil.getTableRowHeight());
		table.setDefaultRenderer(Class.class, new ClassCellRenderer());

		RoundedNumberRenderer relNumberRenderer = new RoundedNumberRenderer("0.0");
		RoundedNumberRenderer timeRenderer = new RoundedNumberRenderer("0.000");
		relNumberRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		timeRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(COL_TIME_REL).setCellRenderer(relNumberRenderer);
		table.getColumnModel().getColumn(COL_TIME_AVG).setCellRenderer(timeRenderer);


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
		setRowCount(0);
	}


	public void setRowCount(int rows)
	{
		model.setRowCount(rows);
	}


	public void setActive(final boolean active)
	{
		reset();
		table.setEnabled(active);
	}


	private static class MetisTableModel extends DefaultTableModel
	{
		private static final long serialVersionUID = -3700170198897729348L;


		private MetisTableModel(final Object[] columnNames, final int rowCount)
		{
			super(columnNames, rowCount);
		}


		@Override
		public Class<?> getColumnClass(final int columnIndex)
		{
			if (columnIndex == COL_EXECUTED)
			{
				return Boolean.class;
			} else if (columnIndex == COL_CALCULATOR)
			{
				return Class.class;
			} else if (columnIndex == COL_TIME_AVG || columnIndex == COL_TIME_REL)
			{
				return Float.class;
			}

			return super.getColumnClass(columnIndex);
		}


		@Override
		public boolean isCellEditable(final int row, final int column)
		{
			return false;
		}
	}

	private static class ClassCellRenderer extends DefaultTableCellRenderer
	{
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column)
		{
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value == null)
			{
				setText("");
			} else
			{
				setText(Objects.toString(value));
			}
			return this;
		}
	}
}
