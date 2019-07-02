/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.kick.presenter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import edu.tigers.sumatra.kick.data.BallModelSample;
import edu.tigers.sumatra.vision.kick.estimators.EBallModelIdentType;
import net.miginfocom.swing.MigLayout;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class BallSamplePresenter
{
	private final List<BallModelSample> samples;
	private final EBallModelIdentType type;
	
	private final JPanel container;
	private final JTextArea resultArea;
	
	private final BallTableModel tableModel;
	
	
	/**
	 * @param samples
	 * @param type
	 */
	public BallSamplePresenter(final List<BallModelSample> samples, final EBallModelIdentType type)
	{
		this.samples = samples;
		this.type = type;
		
		// final result area
		JPanel resultPanel = new JPanel(new BorderLayout());
		
		resultArea = new JTextArea(type.getParameterNames().length, 50);
		resultArea.setEditable(false);
		resultArea.setMinimumSize(resultArea.getPreferredSize());
		
		resultPanel.add(resultArea);
		resultPanel.setBorder(BorderFactory.createTitledBorder("Average of selected samples"));
		
		// table setup
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		
		tableModel = new BallTableModel();
		JTable table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		
		for (int i = 0; i < type.getParameterNames().length; i++)
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
			List<BallModelSample> mySamples = samples.stream()
					.filter(s -> s.getType() == type)
					.collect(Collectors.toList());
			
			samples.removeAll(
					Arrays.stream(table.getSelectedRows()).mapToObj(mySamples::get).collect(Collectors.toList()));
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
	
	
	private void computeResult()
	{
		StringBuilder text = new StringBuilder();
		
		List<BallModelSample> usedSamples = samples.stream()
				.filter(s -> (s.getType() == type) && s.isSampleUsed())
				.collect(Collectors.toList());
		
		for (String name : type.getParameterNames())
		{
			double average = usedSamples.stream()
					.mapToDouble(s -> s.getParameters().get(name))
					.average().orElse(0);
			
			text.append(name + ": " + String.format(Locale.ENGLISH, "%.6f", average) + System.lineSeparator());
		}
		
		resultArea.setText(text.toString());
	}
	
	
	public Component getComponent()
	{
		return container;
	}
	
	
	/**
	 * Update table.
	 */
	public void update()
	{
		EventQueue.invokeLater(tableModel::fireTableDataChanged);
		EventQueue.invokeLater(this::computeResult);
	}
	
	private class BallTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 8616993168778298675L;
		private final String[] columnNames = type.getParameterNames();
		
		
		private List<BallModelSample> getSamples()
		{
			return samples.stream().filter(s -> s.getType() == type).collect(Collectors.toList());
		}
		
		
		@Override
		public int getColumnCount()
		{
			return columnNames.length + 1;
		}
		
		
		@Override
		public int getRowCount()
		{
			return getSamples().size();
		}
		
		
		@Override
		public Object getValueAt(final int row, final int col)
		{
			if (col == columnNames.length)
			{
				return getSamples().get(row).isSampleUsed();
			}
			
			return String.format(Locale.ENGLISH, "%.3f", getSamples().get(row).getParameters().get(columnNames[col]));
		}
		
		
		@Override
		public String getColumnName(final int col)
		{
			if (col == columnNames.length)
			{
				return "Use";
			}
			
			return columnNames[col];
		}
		
		
		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(final int col)
		{
			if (col == columnNames.length)
			{
				return Boolean.class;
			}
			
			return String.class;
		}
		
		
		@Override
		public boolean isCellEditable(final int row, final int col)
		{
			return col == columnNames.length;
		}
		
		
		@Override
		public void setValueAt(final Object value, final int row, final int col)
		{
			if (col != columnNames.length)
			{
				return;
			}
			
			getSamples().get(row).setSampleUsed((Boolean) value);
			fireTableCellUpdated(row, col);
			computeResult();
		}
	}
}
