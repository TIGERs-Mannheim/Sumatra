/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.kick.presenter.sample;

import edu.tigers.sumatra.math.StatisticsMath;
import edu.tigers.sumatra.modelidentification.kickspeed.data.BallModelSample;
import edu.tigers.sumatra.vision.kick.estimators.EBallModelIdentType;

import javax.swing.table.AbstractTableModel;
import java.io.Serial;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;


public class BallSamplePresenter extends ASamplePresenter<BallModelSample>
{
	private final EBallModelIdentType type;


	public BallSamplePresenter(List<BallModelSample> samples, EBallModelIdentType type)
	{
		this.type = type;

		BallTableModel ballTableModel = new BallTableModel(samples);
		init(samples, ballTableModel);
	}


	@Override
	protected void filterOutBadSamples(List<BallModelSample> samples)
	{
		List<BallModelSample> usedSamples = samples.stream()
				.filter(BallModelSample::isSampleUsed)
				.toList();

		for (String name : type.getParameterNames())
		{
			List<Double> values = usedSamples.stream()
					.map(s -> s.getParameters().get(name))
					.toList();
			double mean = StatisticsMath.mean(values);
			double std = StatisticsMath.std(values);

			usedSamples.stream()
					.filter(s -> !isInsideRange(s.getParameters().get(name), mean, 2 * std))
					.forEach(s -> s.setSampleUsed(false));
		}

		update();
	}


	@Override
	protected String doComputeResult(List<BallModelSample> samples)
	{
		StringBuilder text = new StringBuilder();
		text.append("Average values for used samples with standard deviation and SNR:");
		text.append(System.lineSeparator());

		List<BallModelSample> usedSamples = samples.stream()
				.filter(BallModelSample::isSampleUsed)
				.toList();

		for (String name : type.getParameterNames())
		{
			List<Double> values = usedSamples.stream()
					.map(s -> s.getParameters().get(name))
					.toList();
			double average = StatisticsMath.mean(values);
			double std = StatisticsMath.std(values);
			double snr = StatisticsMath.snr(values);

			text.append(name);
			text.append(": ");
			text.append(String.format(Locale.ENGLISH, "%.3f (%.3f|%.3f)", average, std, snr));
			text.append(System.lineSeparator());
		}

		return text.toString();
	}


	private class BallTableModel extends AbstractTableModel
	{
		@Serial
		private static final long serialVersionUID = 8616993168778298675L;

		private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");
		private final List<String> columnNames;
		private final transient List<BallModelSample> samples;


		private BallTableModel(List<BallModelSample> samples)
		{
			this.samples = samples;

			columnNames = Stream.of(
					Stream.of("Timestamp", "Bot id", "Feedback"),
					Arrays.stream(type.getParameterNames()),
					Stream.of("Use")
			).flatMap(Function.identity()).toList();
		}


		private List<BallModelSample> getSamples()
		{
			return samples;
		}


		@Override
		public int getColumnCount()
		{
			return columnNames.size();
		}


		@Override
		public int getRowCount()
		{
			return getSamples().size();
		}


		@Override
		public Object getValueAt(final int row, final int col)
		{
			BallModelSample sample = getSamples().get(row);

			if (col == 0)
			{
				return sdf.format(sample.getTimestamp() / 1_000_000);
			}
			if (col == 1)
			{
				return sample.getBotId();
			}
			if (col == 2)
			{
				return String.format(
						Locale.ENGLISH, "%4.1fms %5.2fm/s %5.2fN", sample.getKickState().getKickDuration(),
						sample.getKickState().getDribblerSpeed(), sample.getKickState().getDribblerForce()
				);
			}
			if (col == columnNames.size() - 1)
			{
				return sample.isSampleUsed();
			}

			Double value = sample.getParameters().get(columnNames.get(col));
			return String.format(Locale.ENGLISH, "%5.2f", value);
		}


		@Override
		public String getColumnName(final int col)
		{
			return columnNames.get(col);
		}


		@Override
		@SuppressWarnings({ "rawtypes" })
		public Class getColumnClass(final int col)
		{
			if (col == columnNames.size() - 1)
			{
				return Boolean.class;
			}

			return String.class;
		}


		@Override
		public boolean isCellEditable(final int row, final int col)
		{
			return col == columnNames.size() - 1;
		}


		@Override
		public void setValueAt(final Object value, final int row, final int col)
		{
			if (col != columnNames.size() - 1)
			{
				return;
			}

			getSamples().get(row).setSampleUsed((Boolean) value);
			fireTableCellUpdated(row, col);
		}
	}
}
