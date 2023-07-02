/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.kick.presenter;

import edu.tigers.sumatra.kick.data.KickModelSample;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.ILineBase;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;
import info.monitorenter.util.Range;
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class KickSamplePresenter
{
	private final List<KickModelSample> samples;

	private final JPanel container;
	private final JTextArea resultArea;
	private final KickTableModel tableModel;

	private final ITrace2D plotStraight;
	private final ITrace2D plotChip;
	private final ITrace2D plotStraightFit;
	private final ITrace2D plotChipFit;
	private final ITrace2D plotUsedSamples;


	/**
	 * @param samples
	 */
	public KickSamplePresenter(final List<KickModelSample> samples)
	{
		this.samples = samples;

		container = new JPanel(new MigLayout("fill"));

		// final result area
		JPanel resultPanel = new JPanel(new BorderLayout());

		resultArea = new JTextArea(4, 50);
		resultArea.setEditable(false);
		resultArea.setMinimumSize(resultArea.getPreferredSize());

		resultPanel.add(resultArea);
		resultPanel.setBorder(BorderFactory.createTitledBorder("Kick Model Coefficients"));

		// table setup
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

		tableModel = new KickTableModel();
		JTable table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		scrollPane.setPreferredSize(new Dimension(200, 200));

		table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
		table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
		table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

		// controls
		JButton btnDeleteUnused = new JButton("Delete unused samples");
		btnDeleteUnused.addActionListener(l -> {
			samples.removeIf(s -> !s.isSampleUsed());
			update();
		});

		JButton btnDeleteSamples = new JButton("Delete selected sample(s)");
		btnDeleteSamples.addActionListener(l -> {
			samples.removeAll(
					Arrays.stream(table.getSelectedRows()).mapToObj(samples::get).toList());
			update();
		});

		JPanel controlPanel = new JPanel(new MigLayout());
		controlPanel.add(btnDeleteUnused, "grow, wrap");
		controlPanel.add(btnDeleteSamples);
		controlPanel.setBorder(BorderFactory.createTitledBorder("Data Manipulation"));

		// plot
		final Chart2D kickChart = new Chart2D();
		kickChart.setName("Kicks");
		kickChart.getAxisY().setRangePolicy(new RangePolicyMinimumViewport(new Range(0, 5)));
		kickChart.getAxisX().setRangePolicy(new RangePolicyMinimumViewport(new Range(0, 6.5)));
		kickChart.getAxisX().setAxisTitle(new AxisTitle("v [m/s]"));
		kickChart.getAxisY().setAxisTitle(new AxisTitle("t [ms]"));
		kickChart.getAxisX().setPaintGrid(true);
		kickChart.getAxisY().setPaintGrid(true);
		kickChart.setGridColor(Color.LIGHT_GRAY);

		plotStraight = new Trace2DSimple("Straight");
		plotStraight.setTracePainter(new TracePainterDisc(3));
		plotStraight.setColor(Color.BLUE);
		plotChip = new Trace2DSimple("Chip");
		plotChip.setTracePainter(new TracePainterDisc(3));
		plotChip.setColor(Color.RED);
		plotStraightFit = new Trace2DSimple("");
		plotStraightFit.setColor(Color.BLUE);
		plotChipFit = new Trace2DSimple("");
		plotChipFit.setColor(Color.RED);
		plotUsedSamples = new Trace2DSimple("Filtered");
		plotUsedSamples.setTracePainter(new TracePainterDisc(7));
		plotUsedSamples.setColor(Color.BLACK);

		kickChart.addTrace(plotStraight);
		kickChart.addTrace(plotChip);
		kickChart.addTrace(plotUsedSamples);
		kickChart.addTrace(plotStraightFit);
		kickChart.addTrace(plotChipFit);

		kickChart.setPreferredSize(new Dimension(200, 200));
		kickChart.setMinimumSize(kickChart.getPreferredSize());

		container.add(resultPanel);
		container.add(controlPanel, "wrap");
		container.add(scrollPane, "spanx 2, pushy, grow, wrap");
		container.add(kickChart, "spanx 2, pushy, grow");

		update();
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


	private void computeResult()
	{
		StringBuilder text = new StringBuilder();

		plotStraightFit.removeAllPoints();
		plotChipFit.removeAllPoints();

		List<KickModelSample> straightSamples = samples.stream()
				.filter(s -> !s.isChip() && s.isSampleUsed())
				.toList();

		List<KickModelSample> chipSamples = samples.stream()
				.filter(s -> s.isChip() && s.isSampleUsed())
				.toList();

		double maxStraight = straightSamples.stream()
				.mapToDouble(KickModelSample::getKickVel)
				.max().orElse(0);

		double maxChip = chipSamples.stream()
				.mapToDouble(KickModelSample::getKickVel)
				.max().orElse(0);

		List<IVector2> usedStraightPoints = straightSamples.stream()
				.filter(s -> (s.getKickVel() < (maxStraight * 0.95)) && (s.getKickVel() > 0.1))
				.map(s -> Vector2.fromXY(s.getKickVel(), s.getKickDuration()))
				.map(IVector2.class::cast)
				.toList();

		List<IVector2> usedChipPoints = chipSamples.stream()
				.filter(s -> (s.getKickVel() < (maxChip * 0.95)) && (s.getKickVel() > 0.2))
				.map(s -> Vector2.fromXY(s.getKickVel(), s.getKickDuration()))
				.map(IVector2.class::cast)
				.toList();

		Optional<ILine> straightLine = Lines.regressionLineFromPointsList(usedStraightPoints).map(ILineBase::toLine);
		if (straightLine.isPresent())
		{
			text.append("Straight Offset: ")
					.append(String.format(Locale.ENGLISH, "%.6f", straightLine.get().getYIntercept().orElse(0.0)))
					.append(System.lineSeparator());
			text.append("Straight Slope: ")
					.append(String.format(Locale.ENGLISH, "%.6f", straightLine.get().getSlope().orElse(0.0)))
					.append(System.lineSeparator());

			plotStraightFit.addPoint(0, straightLine.get().getYIntercept().orElse(0.0));
			plotStraightFit.addPoint(maxStraight, straightLine.get().getYValue(maxStraight).orElse(0.0));
		} else
		{
			text.append("Not enough straight kick samples").append(System.lineSeparator());
		}

		Optional<ILine> chipLine = Lines.regressionLineFromPointsList(usedChipPoints).map(ILineBase::toLine);
		if (chipLine.isPresent())
		{
			text.append("Chip Offset: ")
					.append(String.format(Locale.ENGLISH, "%.6f", chipLine.get().getYIntercept().orElse(0.0)))
					.append(System.lineSeparator());
			text.append("Chip Slope: ")
					.append(String.format(Locale.ENGLISH, "%.6f", chipLine.get().getSlope().orElse(0.0)));

			plotChipFit.addPoint(0, chipLine.get().getYIntercept().orElse(0.0));
			plotChipFit.addPoint(maxChip, chipLine.get().getYValue(maxChip).orElse(0.0));
		} else
		{
			text.append("Not enough chip kick samples");
		}

		resultArea.setText(text.toString());

		plotStraight.removeAllPoints();
		plotChip.removeAllPoints();
		plotUsedSamples.removeAllPoints();

		samples.stream()
				.filter(s -> !s.isChip() && s.isSampleUsed())
				.forEach(s -> plotStraight.addPoint(s.getKickVel(), s.getKickDuration()));

		samples.stream()
				.filter(s -> s.isChip() && s.isSampleUsed())
				.forEach(s -> plotChip.addPoint(s.getKickVel(), s.getKickDuration()));

		usedStraightPoints.forEach(p -> plotUsedSamples.addPoint(p.x(), p.y()));
		usedChipPoints.forEach(p -> plotUsedSamples.addPoint(p.x(), p.y()));
	}


	private class KickTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = -2975471462465845L;

		private final String[] columnNames = new String[] { "Bot",
				"Kick Duration", "Kick Speed", "Chip Kick", "Dribbler", "Use" };

		@SuppressWarnings("rawtypes")
		private final Class[] classes = new Class[] { String.class,
				String.class, String.class, Boolean.class, String.class, Boolean.class };


		@Override
		public int getColumnCount()
		{
			return 6;
		}


		@Override
		public int getRowCount()
		{
			return samples.size();
		}


		@Override
		public Object getValueAt(final int row, final int col)
		{
			KickModelSample sample = samples.get(row);

			return switch (col)
			{
				case 0 -> sample.getBotId();
				case 1 -> String.format(Locale.ENGLISH, "%.2fms", sample.getKickDuration());
				case 2 -> String.format(Locale.ENGLISH, "%.3fm/s", sample.getKickVel());
				case 3 -> sample.isChip();
				case 4 -> String.format(Locale.ENGLISH, "%.0fRPM", sample.getDribbleSpeed());
				case 5 -> sample.isSampleUsed();
				default -> null;
			};
		}


		@Override
		public String getColumnName(final int col)
		{
			return columnNames[col];
		}


		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(final int col)
		{
			return classes[col];
		}


		@Override
		public boolean isCellEditable(final int row, final int col)
		{
			return col == 5;
		}


		@Override
		public void setValueAt(final Object value, final int row, final int col)
		{
			if (col != 5)
			{
				return;
			}

			samples.get(row).setSampleUsed((Boolean) value);
			fireTableCellUpdated(row, col);
			computeResult();
		}
	}
}
