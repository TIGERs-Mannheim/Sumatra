/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.kick.presenter.sample;

import edu.tigers.sumatra.gui.kick.presenter.sample.kick.Poly1Fit;
import edu.tigers.sumatra.gui.kick.presenter.sample.kick.Poly2Fit;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.modelidentification.kickspeed.data.KickModelSample;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis.AxisTitle;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;
import info.monitorenter.util.Range;
import lombok.RequiredArgsConstructor;

import javax.swing.table.AbstractTableModel;
import java.awt.Color;
import java.awt.Dimension;
import java.io.Serial;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


public class KickSamplePresenter extends ASamplePresenter<KickModelSample>
{
	private final ITrace2D plotStraight;
	private final ITrace2D plotChip;
	private final ITrace2D plotStraightFitPoly1;
	private final ITrace2D plotStraightFitPoly2;
	private final ITrace2D plotChipFitPoly1;
	private final ITrace2D plotChipFitPoly2;
	private final ITrace2D plotUsedSamples;


	public KickSamplePresenter(List<KickModelSample> samples)
	{
		Chart2D kickChart = new Chart2D();
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

		var tableModel = new KickTableModel(samples);
		init(samples, tableModel);
		getContainer().add(kickChart, "spanx 2, pushy, grow");
	}


	@Override
	protected void filterOutBadSamples(List<KickModelSample> samples)
	{
		// not implemented
	}


	@Override
	public String doComputeResult(List<KickModelSample> samples)
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

		return text.toString();
	}


	private String computeStraightFit(List<KickModelSample> straightSamples)
	{
		StringBuilder text = new StringBuilder();

		double maxStraight = straightSamples.stream()
				.mapToDouble(KickModelSample::getMeasuredKickVel)
				.max().orElse(0);

		List<IVector2> usedStraightPoints = straightSamples.stream()
				.filter(s -> s.getMeasuredKickVel() > 0.1)
				.map(s -> Vector2.fromXY(s.getMeasuredKickVel(), s.getBotStateAtKick().getKickDuration()))
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

		straightSamples.forEach(
				s -> plotStraight.addPoint(s.getMeasuredKickVel(), s.getBotStateAtKick().getKickDuration()));
		usedStraightPoints.forEach(p -> plotUsedSamples.addPoint(p.x(), p.y()));

		return text.toString();
	}


	private String computeChipFit(List<KickModelSample> chipSamples)
	{
		StringBuilder text = new StringBuilder();

		double maxChip = chipSamples.stream()
				.mapToDouble(KickModelSample::getMeasuredKickVel)
				.max().orElse(0);

		List<IVector2> usedChipPoints = chipSamples.stream()
				.filter(s -> (s.getMeasuredKickVel() < (maxChip * 0.95)) && (s.getMeasuredKickVel() > 0.2))
				.map(s -> Vector2.fromXY(s.getMeasuredKickVel(), s.getBotStateAtKick().getKickDuration()))
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

		chipSamples.forEach(s -> plotChip.addPoint(s.getMeasuredKickVel(), s.getBotStateAtKick().getKickDuration()));
		usedChipPoints.forEach(p -> plotUsedSamples.addPoint(p.x(), p.y()));

		return text.toString();
	}


	@RequiredArgsConstructor
	private static class KickTableModel extends AbstractTableModel
	{
		@Serial
		private static final long serialVersionUID = -2975471462465845L;

		private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");
		private final String[] columnNames = new String[] {
				"Kick timestamp", "Bot", "Cmd Speed", "Meas Speed", "Cmd Dribbler", "Chip Kick", "Feedback", "Use"
		};
		private final transient List<KickModelSample> samples;

		@SuppressWarnings("rawtypes")
		private final Class[] classes = new Class[] {
				String.class, String.class, String.class, String.class, String.class, Boolean.class, String.class,
				Boolean.class
		};


		@Override
		public int getColumnCount()
		{
			return columnNames.length;
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
				case 0 -> sdf.format(new Date(sample.getKickTimestamp() / 1_000_000));
				case 1 -> sample.getBotId();
				case 2 -> sample.getCmdKickVel() == null ?
						"-" :
						String.format(Locale.ENGLISH, "%.2fm/s", sample.getCmdKickVel());
				case 3 -> String.format(Locale.ENGLISH, "%.2fm/s", sample.getMeasuredKickVel());
				case 4 -> String.format(Locale.ENGLISH, "%.0fm/s", sample.getCmdDribbleSpeed());
				case 5 -> sample.isChip();
				case 6 -> String.format(
						Locale.ENGLISH, "%4.1fms %5.2fm/s %5.2fN", sample.getBotStateAtKick().getKickDuration(),
						sample.getBotStateAtKick().getDribblerSpeed(), sample.getBotStateAtKick().getDribblerForce()
				);
				case 7 -> sample.isSampleUsed();
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
		}
	}
}
