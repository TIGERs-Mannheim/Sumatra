/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.kick.presenter;

import edu.tigers.sumatra.kick.data.RedirectModelSample;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


public class RedirectSamplePresenter
{
	private final List<RedirectModelSample> samples;

	private final JPanel container;
	private final JTextArea resultArea;

	private final RedirectTableModel tableModel;


	public RedirectSamplePresenter(final List<RedirectModelSample> samples)
	{
		this.samples = samples;

		// final result area
		JPanel resultPanel = new JPanel(new BorderLayout());

		resultArea = new JTextArea(4, 50);
		resultArea.setEditable(false);
		resultArea.setMinimumSize(resultArea.getPreferredSize());

		resultPanel.add(resultArea);
		resultPanel.setBorder(BorderFactory.createTitledBorder("Redirect Model Coefficients"));

		// table setup
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

		tableModel = new RedirectTableModel();
		JTable table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);

		for (int i = 0; i < table.getColumnCount() - 1; i++)
		{
			table.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
		}

		// controls
		JButton btnDeleteUnused = new JButton("Delete unused samples");
		btnDeleteUnused.addActionListener(l -> {
			samples.removeIf(s -> !s.isSampleUsed());
			update();
		});

		JButton btnDeleteSamples = new JButton("Delete selected sample(s)");
		btnDeleteSamples.addActionListener(l -> {
			samples.removeAll(
					Arrays.stream(table.getSelectedRows()).mapToObj(samples::get).collect(Collectors.toList()));
			update();
		});

		JPanel controlPanel = new JPanel(new MigLayout());
		controlPanel.add(btnDeleteUnused, "grow, wrap");
		controlPanel.add(btnDeleteSamples);
		controlPanel.setBorder(BorderFactory.createTitledBorder("Data Manipulation"));

		container = new JPanel(new MigLayout("fill"));
		container.add(resultPanel);
		container.add(controlPanel, "wrap");
		container.add(scrollPane, "spanx 2, pushy, grow");

		computeResult();
	}


	public Component getComponent()
	{
		return container;
	}


	public void update()
	{
		EventQueue.invokeLater(tableModel::fireTableDataChanged);
		EventQueue.invokeLater(this::computeResult);
	}


	private void computeResult()
	{
		StringBuilder text = new StringBuilder();

		double avgSpinFactor = samples.stream()
				.filter(RedirectModelSample::isSampleUsed)
				.mapToDouble(RedirectModelSample::getSpinFactor)
				.average()
				.orElse(0.0);

		double avgVerticalSpinFactor = samples.stream()
				.filter(RedirectModelSample::isSampleUsed)
				.mapToDouble(RedirectModelSample::getVerticalSpinFactor)
				.average()
				.orElse(0.0);

		double avgRestitutionCoeff = samples.stream()
				.filter(RedirectModelSample::isSampleUsed)
				.mapToDouble(RedirectModelSample::getRedirectRestitutionCoefficient)
				.average()
				.orElse(0.0);

		text.append(String.format(Locale.ENGLISH, "H-Spin Factor: %.3f", avgSpinFactor)).append(System.lineSeparator());
		text.append(String.format(Locale.ENGLISH, "V-Spin Factor: %.3f", avgVerticalSpinFactor))
				.append(System.lineSeparator());
		text.append(String.format(Locale.ENGLISH, "Restitution Coefficient: %.3f", avgRestitutionCoeff)).append(System
				.lineSeparator());

		resultArea.setText(text.toString());
	}


	private class RedirectTableModel extends AbstractTableModel
	{
		private final String[] columnNames = new String[] { "In Velocity", "Kick Speed", "Out Velocity",
				"Restitution Coeff.", "V-Spin Factor", "H-Spin Factor", "Use" };


		@Override
		public int getColumnCount()
		{
			return 7;
		}


		@Override
		public int getRowCount()
		{
			return samples.size();
		}


		@Override
		public Object getValueAt(final int row, final int col)
		{
			RedirectModelSample sample = samples.get(row);

			switch (col)
			{
				case 0:
					return sample.getInVelocity().toString();
				case 1:
					return String.format(Locale.ENGLISH, "%.3f", sample.getKickSpeed());
				case 2:
					return sample.getOutVelocity().toString();
				case 3:
					return String.format(Locale.ENGLISH, "%.3f", sample.getRedirectRestitutionCoefficient());
				case 4:
					return String.format(Locale.ENGLISH, "%.2f", sample.getVerticalSpinFactor());
				case 5:
					return String.format(Locale.ENGLISH, "%.2f", sample.getSpinFactor());
				case 6:
					return sample.isSampleUsed();
				default:
					return null;
			}
		}


		@Override
		public String getColumnName(final int col)
		{
			return columnNames[col];
		}


		@Override
		@SuppressWarnings({ "rawtypes" })
		public Class getColumnClass(final int col)
		{
			if (col == columnNames.length - 1)
			{
				return Boolean.class;
			}

			return String.class;
		}


		@Override
		public boolean isCellEditable(final int row, final int col)
		{
			return col == columnNames.length - 1;
		}


		@Override
		public void setValueAt(final Object value, final int row, final int col)
		{
			if (col != columnNames.length - 1)
			{
				return;
			}

			samples.get(row).setSampleUsed((Boolean) value);
			fireTableCellUpdated(row, col);
			computeResult();
		}
	}
}
