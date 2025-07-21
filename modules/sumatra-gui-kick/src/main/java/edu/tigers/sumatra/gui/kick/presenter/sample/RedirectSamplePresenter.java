/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.kick.presenter.sample;

import edu.tigers.sumatra.math.StatisticsMath;
import edu.tigers.sumatra.modelidentification.kickspeed.data.RedirectModelSample;
import lombok.RequiredArgsConstructor;

import javax.swing.table.AbstractTableModel;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;


public class RedirectSamplePresenter extends ASamplePresenter<RedirectModelSample>
{
	public RedirectSamplePresenter(final List<RedirectModelSample> samples)
	{
		var tableModel = new RedirectTableModel(samples);
		init(samples, tableModel);
	}


	@Override
	protected void filterOutBadSamples(List<RedirectModelSample> samples)
	{
		List<RedirectModelSample> usedSamples = samples.stream()
				.filter(RedirectModelSample::isSampleUsed)
				.toList();

		List<ToDoubleFunction<RedirectModelSample>> params = List.of(
				RedirectModelSample::getSpinFactor,
				RedirectModelSample::getVerticalSpinFactor,
				RedirectModelSample::getRedirectRestitutionCoefficient
		);

		var worstParam = params.stream()
				.min(Comparator.comparingDouble(param -> StatisticsMath.snr(usedSamples, param)))
				.orElseThrow();

		List<Double> values = usedSamples.stream()
				.map(worstParam::applyAsDouble)
				.sorted()
				.toList();

		double mean = StatisticsMath.mean(values);
		double std = StatisticsMath.std(values);
		usedSamples.stream()
				.filter(s -> !isInsideRange(worstParam.applyAsDouble(s), mean, 2 * std))
				.forEach(s -> s.setSampleUsed(false));

		update();
	}


	@Override
	protected String doComputeResult(List<RedirectModelSample> samples)
	{
		StringBuilder text = new StringBuilder();

		List<RedirectModelSample> usedSamples = samples.stream()
				.filter(RedirectModelSample::isSampleUsed)
				.toList();

		Map<String, Function<RedirectModelSample, Double>> params = new LinkedHashMap<>();
		params.put("H-Spin-Factor", RedirectModelSample::getSpinFactor);
		params.put("V-Spin-Factor", RedirectModelSample::getVerticalSpinFactor);
		params.put("Restitution Coefficient", RedirectModelSample::getRedirectRestitutionCoefficient);

		params.forEach((name, param) -> {
			List<Double> values = usedSamples.stream()
					.map(param)
					.toList();
			double average = StatisticsMath.mean(values);
			double std = StatisticsMath.std(values);
			double snr = StatisticsMath.snr(values);

			text.append(name);
			text.append(": ");
			text.append(String.format(Locale.ENGLISH, "%.3f (%.3f|%.3f)", average, std, snr));
			text.append(System.lineSeparator());
		});

		return text.toString();
	}


	@RequiredArgsConstructor
	private static class RedirectTableModel extends AbstractTableModel
	{
		private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SSS");
		private final String[] columnNames = new String[] {
				"Kick timestamp",
				"Bot ID",
				"In Velocity",
				"In Angle",
				"Kick Speed",
				"Out Velocity",
				"Out Angle",
				"Restitution Coeff.",
				"V-Spin Factor",
				"H-Spin Factor",
				"Use"
		};
		private final transient List<RedirectModelSample> samples;


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
			RedirectModelSample sample = samples.get(row);

			return switch (col)
			{
				case 0 -> sdf.format(new Date(sample.getKickTimestamp() / 1_000_000));
				case 1 -> sample.getBotId();
				case 2 -> String.format(Locale.ENGLISH, "%.2f", sample.getInVelocity().getLength2());
				case 3 -> String.format(Locale.ENGLISH, "%.2f", sample.getInVelocity().getAngle());
				case 4 -> String.format(Locale.ENGLISH, "%.2f", sample.getKickSpeed());
				case 5 -> String.format(Locale.ENGLISH, "%.2f", sample.getOutVelocity().getLength2());
				case 6 -> String.format(Locale.ENGLISH, "%.2f", sample.getOutVelocity().getAngle());
				case 7 -> String.format(Locale.ENGLISH, "%.3f", sample.getRedirectRestitutionCoefficient());
				case 8 -> String.format(Locale.ENGLISH, "%.2f", sample.getVerticalSpinFactor());
				case 9 -> String.format(Locale.ENGLISH, "%.2f", sample.getSpinFactor());
				case 10 -> sample.isSampleUsed();
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
		}
	}
}
