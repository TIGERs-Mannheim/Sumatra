/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.kick.presenter;

import edu.tigers.sumatra.kick.data.KickModelSample;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;
import info.monitorenter.util.Range;
import lombok.Value;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;

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
	private final ITrace2D plotStraightFitPoly1;
	private final ITrace2D plotStraightFitPoly2;
	private final ITrace2D plotChipFitPoly1;
	private final ITrace2D plotChipFitPoly2;
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

		plotStraight = new Trace2DSimple("Straight Samples");
		plotStraight.setTracePainter(new TracePainterDisc(3));
		plotStraight.setColor(Color.BLUE);
		plotChip = new Trace2DSimple("Chip Samples");
		plotChip.setTracePainter(new TracePainterDisc(3));
		plotChip.setColor(Color.RED);
		plotStraightFitPoly1 = new Trace2DSimple("Straight Poly1");
		plotStraightFitPoly1.setColor(new Color(0x00C4FF));
		plotStraightFitPoly2 = new Trace2DSimple("Straight Poly2");
		plotStraightFitPoly2.setColor(Color.BLUE);
		plotChipFitPoly1 = new Trace2DSimple("Chip Poly1");
		plotChipFitPoly1.setColor(Color.PINK);
		plotChipFitPoly2 = new Trace2DSimple("Chip Poly2");
		plotChipFitPoly2.setColor(Color.RED);
		plotUsedSamples = new Trace2DSimple("Filtered");
		plotUsedSamples.setTracePainter(new TracePainterDisc(7));
		plotUsedSamples.setColor(Color.BLACK);

		kickChart.addTrace(plotStraight);
		kickChart.addTrace(plotChip);
		kickChart.addTrace(plotUsedSamples);
		kickChart.addTrace(plotStraightFitPoly1);
		kickChart.addTrace(plotStraightFitPoly2);
		kickChart.addTrace(plotChipFitPoly1);
		kickChart.addTrace(plotChipFitPoly2);

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

		plotUsedSamples.removeAllPoints();

		List<KickModelSample> straightSamples = samples.stream()
				.filter(s -> !s.isChip() && s.isSampleUsed())
				.toList();

		List<KickModelSample> chipSamples = samples.stream()
				.filter(s -> s.isChip() && s.isSampleUsed())
				.toList();

		text.append(computeStraightFit(straightSamples));
		text.append(computeChipFit(chipSamples));

		resultArea.setText(text.toString());
	}


	private String computeStraightFit(List<KickModelSample> straightSamples)
	{
		StringBuilder text = new StringBuilder();

		double maxStraight = straightSamples.stream()
				.mapToDouble(KickModelSample::getKickVel)
				.max().orElse(0);

		List<IVector2> usedStraightPoints = straightSamples.stream()
				.filter(s -> s.getKickVel() > 0.1)
				.map(s -> Vector2.fromXY(s.getKickVel(), s.getKickDuration() * 1e-3))
				.map(IVector2.class::cast)
				.toList();

		plotStraight.removeAllPoints();
		plotStraightFitPoly1.removeAllPoints();
		plotStraightFitPoly2.removeAllPoints();

		Optional<Poly1Fit> straightPoly1 = Poly1Fit.fromPointsList(usedStraightPoints);
		if (straightPoly1.isPresent())
		{
			text.append(
							String.format(Locale.ENGLISH, "Straight Poly1 Factors (1, x):      % .3f, % .3f         (Err: %.3fms)",
									straightPoly1.get().getOffset(), straightPoly1.get().getLinear(),
									straightPoly1.get().getAverageError()))
					.append(System.lineSeparator());

			for (double x = 0; x <= maxStraight; x += 0.1)
			{
				plotStraightFitPoly1.addPoint(x, straightPoly1.get().getYValue(x));
			}
		} else
		{
			text.append("Not enough fit samples for poly1 straight kick model").append(System.lineSeparator());
		}

		Optional<Poly2Fit> straightPoly2 = Poly2Fit.fromPointsList(usedStraightPoints);
		if (straightPoly2.isPresent())
		{
			text.append(
							String.format(Locale.ENGLISH, "Straight Poly2 Factors (1, x, x^2): % .3f, % .3f, % .3f (Err: %.3fms)",
									straightPoly2.get().getOffset(), straightPoly2.get().getLinear(),
									straightPoly2.get().getQuadratic(), straightPoly2.get().getAverageError()))
					.append(System.lineSeparator());

			for (double x = 0; x <= maxStraight; x += 0.1)
			{
				plotStraightFitPoly2.addPoint(x, straightPoly2.get().getYValue(x));
			}
		} else
		{
			text.append("Not enough fit samples for poly2 straight kick model").append(System.lineSeparator());
		}

		straightSamples.forEach(s -> plotStraight.addPoint(s.getKickVel(), s.getKickDuration() * 1e-3));
		usedStraightPoints.forEach(p -> plotUsedSamples.addPoint(p.x(), p.y()));

		return text.toString();
	}


	private String computeChipFit(List<KickModelSample> chipSamples)
	{
		StringBuilder text = new StringBuilder();

		double maxChip = chipSamples.stream()
				.mapToDouble(KickModelSample::getKickVel)
				.max().orElse(0);

		List<IVector2> usedChipPoints = chipSamples.stream()
				.filter(s -> (s.getKickVel() < (maxChip * 0.95)) && (s.getKickVel() > 0.2))
				.map(s -> Vector2.fromXY(s.getKickVel(), s.getKickDuration() * 1e-3))
				.map(IVector2.class::cast)
				.toList();

		plotChip.removeAllPoints();
		plotChipFitPoly1.removeAllPoints();
		plotChipFitPoly2.removeAllPoints();

		Optional<Poly1Fit> chipPoly1 = Poly1Fit.fromPointsList(usedChipPoints);
		if (chipPoly1.isPresent())
		{
			text.append(
							String.format(Locale.ENGLISH, "    Chip Poly1 Factors (1, x):      % .3f, % .3f         (Err: %.3fms)",
									chipPoly1.get().getOffset(), chipPoly1.get().getLinear(), chipPoly1.get().getAverageError()))
					.append(System.lineSeparator());

			for (double x = 0; x <= maxChip; x += 0.1)
			{
				plotChipFitPoly1.addPoint(x, chipPoly1.get().getYValue(x));
			}
		} else
		{
			text.append("Not enough fit samples for poly1 chip kick model").append(System.lineSeparator());
		}

		Optional<Poly2Fit> chipPoly2 = Poly2Fit.fromPointsList(usedChipPoints);
		if (chipPoly2.isPresent())
		{
			text.append(
							String.format(Locale.ENGLISH, "    Chip Poly2 Factors (1, x, x^2): % .3f, % .3f, % .3f (Err: %.3fms)",
									chipPoly2.get().getOffset(), chipPoly2.get().getLinear(), chipPoly2.get().getQuadratic(),
									chipPoly2.get().getAverageError()))
					.append(System.lineSeparator());

			for (double x = 0; x <= maxChip; x += 0.1)
			{
				plotChipFitPoly2.addPoint(x, chipPoly2.get().getYValue(x));
			}
		} else
		{
			text.append("Not enough fit samples for poly2 chip kick model").append(System.lineSeparator());
		}

		chipSamples.forEach(s -> plotChip.addPoint(s.getKickVel(), s.getKickDuration() * 1e-3));
		usedChipPoints.forEach(p -> plotUsedSamples.addPoint(p.x(), p.y()));

		return text.toString();
	}


	@Value
	private static class Poly1Fit
	{
		double linear;
		double offset;
		double averageError; // sum of absolute differences


		public double getYValue(double x)
		{
			return linear * x + offset;
		}


		/**
		 * Calculates a first order poly line through all points.
		 *
		 * @param points
		 * @return Optional Line.
		 */
		@SuppressWarnings("squid:S1166") // Exception from solver not logged
		public static Optional<Poly1Fit> fromPointsList(final List<IVector2> points)
		{
			int numPoints = points.size();

			if (numPoints < 2)
			{
				return Optional.empty();
			}

			RealMatrix matA = new Array2DRowRealMatrix(numPoints, 2);
			RealVector b = new ArrayRealVector(numPoints);

			for (int i = 0; i < numPoints; i++)
			{
				matA.setEntry(i, 0, points.get(i).x());
				matA.setEntry(i, 1, 1.0);

				b.setEntry(i, points.get(i).y());
			}

			DecompositionSolver solver = new QRDecomposition(matA).getSolver();
			RealVector x;
			try
			{
				x = solver.solve(b);
			} catch (SingularMatrixException e)
			{
				return Optional.empty();
			}

			double slope = x.getEntry(0);
			double offset = x.getEntry(1);

			double averageError = points.stream()
					.mapToDouble(p -> Math.abs(slope * p.x() + offset - p.y()))
					.average().orElse(Double.NaN);

			return Optional.of(new Poly1Fit(slope, offset, averageError));
		}
	}

	@Value
	private static class Poly2Fit
	{
		double quadratic;
		double linear;
		double offset;
		double averageError; // sum of absolute differences


		public double getYValue(double x)
		{
			return quadratic * x * x + linear * x + offset;
		}


		/**
		 * Calculates a second order poly line through all points.
		 *
		 * @param points
		 * @return Optional Line.
		 */
		@SuppressWarnings("squid:S1166") // Exception from solver not logged
		public static Optional<Poly2Fit> fromPointsList(final List<IVector2> points)
		{
			int numPoints = points.size();

			if (numPoints < 3)
			{
				return Optional.empty();
			}

			RealMatrix matA = new Array2DRowRealMatrix(numPoints, 3);
			RealVector b = new ArrayRealVector(numPoints);

			for (int i = 0; i < numPoints; i++)
			{
				matA.setEntry(i, 0, points.get(i).x() * points.get(i).x());
				matA.setEntry(i, 1, points.get(i).x());
				matA.setEntry(i, 2, 1.0);

				b.setEntry(i, points.get(i).y());
			}

			DecompositionSolver solver = new QRDecomposition(matA).getSolver();
			RealVector x;
			try
			{
				x = solver.solve(b);
			} catch (SingularMatrixException e)
			{
				return Optional.empty();
			}

			double quadratic = x.getEntry(0);
			double slope = x.getEntry(1);
			double offset = x.getEntry(2);

			double averageError = points.stream()
					.mapToDouble(p -> Math.abs(quadratic * p.x() * p.x() + slope * p.x() + offset - p.y()))
					.average().orElse(Double.NaN);

			return Optional.of(new Poly2Fit(quadratic, slope, offset, averageError));
		}
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
						case 1 -> String.format(Locale.ENGLISH, "%.2fms", sample.getKickDuration() * 1e-3);
						case 2 -> String.format(Locale.ENGLISH, "%.3fm/s", sample.getKickVel());
						case 3 -> sample.isChip();
						case 4 -> String.format(Locale.ENGLISH, "%.0fm/s", sample.getDribbleSpeed());
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
		@SuppressWarnings({ "rawtypes" })
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
