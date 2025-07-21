/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.kick.presenter.sample;

import edu.tigers.sumatra.modelidentification.kickspeed.data.Sample;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class ASamplePresenter<T extends Sample>
{
	@Getter
	private final JPanel container;
	private final JTextArea resultArea;
	@Getter
	private final JTable table = new JTable();

	private List<T> samples = new ArrayList<>();
	private AbstractTableModel tableModel;


	protected ASamplePresenter()
	{
		resultArea = new JTextArea(6, 50);
		resultArea.setEditable(false);
		resultArea.setMinimumSize(resultArea.getPreferredSize());

		JPanel resultPanel = new JPanel(new BorderLayout());
		resultPanel.setBorder(BorderFactory.createTitledBorder("Result"));
		resultPanel.add(resultArea);


		table.setFillsViewportHeight(true);
		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "selectDeselectAll");
		table.getActionMap().put("selectDeselectAll", new TableKeyListener(table));

		container = new JPanel(new MigLayout("fill"));
		container.add(resultPanel);
		container.add(getControlPanel(table), "wrap");
		container.add(new JScrollPane(table), "spanx 2, pushy, grow, wrap");
	}


	protected void init(List<T> samples, AbstractTableModel tableModel)
	{
		this.samples = samples;
		this.tableModel = tableModel;
		table.setModel(tableModel);
		tableModel.addTableModelListener(e -> computeResult());
		computeResult();
	}


	private JPanel getControlPanel(JTable table)
	{
		JButton btnDeleteUnused = new JButton("Delete unused samples");
		btnDeleteUnused.addActionListener(l -> {
			samples.removeIf(s -> !s.isSampleUsed());
			update();
		});

		JButton btnDeleteSamples = new JButton("Delete selected sample(s)");
		btnDeleteSamples.addActionListener(l -> {
			List<T> selectedSamples = Arrays.stream(table.getSelectedRows()).mapToObj(samples::get).toList();
			samples.removeAll(selectedSamples);
			update();
		});

		JButton btnFilterSamples = new JButton("Filter out bad samples");
		btnFilterSamples.addActionListener(l -> filterOutBadSamples(samples));

		JPanel controlPanel = new JPanel(new MigLayout());
		controlPanel.setBorder(BorderFactory.createTitledBorder("Data Manipulation"));
		controlPanel.add(btnDeleteUnused, "grow, wrap");
		controlPanel.add(btnDeleteSamples, "grow, wrap");
		controlPanel.add(btnFilterSamples);
		return controlPanel;
	}


	protected abstract void filterOutBadSamples(List<T> samples);


	private void computeResult()
	{
		resultArea.setText(doComputeResult(samples));
	}


	protected abstract String doComputeResult(List<T> samples);


	protected boolean isInsideRange(double value, double mean, double range)
	{
		return value >= mean - range && value <= mean + range;
	}


	public void update()
	{
		SwingUtilities.invokeLater(tableModel::fireTableDataChanged);
		SwingUtilities.invokeLater(this::computeResult);
	}


	@RequiredArgsConstructor
	private class TableKeyListener extends AbstractAction
	{
		private final JTable table;


		@Override
		public void actionPerformed(ActionEvent e)
		{
			boolean allUsed = Arrays.stream(table.getSelectedRows())
					.mapToObj(samples::get)
					.allMatch(T::isSampleUsed);
			if (allUsed)
			{
				Arrays.stream(table.getSelectedRows())
						.mapToObj(samples::get)
						.forEach(s -> s.setSampleUsed(false));
			} else
			{
				Arrays.stream(table.getSelectedRows())
						.mapToObj(samples::get)
						.forEach(s -> s.setSampleUsed(true));
			}
			SwingUtilities.invokeLater(table::repaint);
			computeResult();
		}
	}
}
