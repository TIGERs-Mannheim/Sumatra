package edu.tigers.sumatra.gui.kick.presenter.sample;

import edu.tigers.sumatra.botmanager.TigersBotManager;
import edu.tigers.sumatra.botmanager.configs.ConfigFile;
import edu.tigers.sumatra.botmanager.configs.ConfigFileDatabase;
import edu.tigers.sumatra.botmanager.configs.ConfigFileDatabaseManager;
import edu.tigers.sumatra.botmanager.configs.IConfigFileDatabaseObserver;
import edu.tigers.sumatra.gui.kick.presenter.sample.kick.Poly2Fit;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.modelidentification.kickspeed.data.KickModelSample;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;
import info.monitorenter.util.Range;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import javax.swing.JCheckBox;
import javax.swing.table.AbstractTableModel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.io.Serial;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;


@Log4j2
public abstract class AKickSamplePresenter extends ASamplePresenter<KickModelSample>
{
	protected final ITrace2D plotSamples;
	protected final ITrace2D plotFitPoly1;
	protected final ITrace2D plotFitPoly2;
	protected final ITrace2D plotUsedSamples;
	private final ITrace2D plotCurrent;

	private final JCheckBox checkShowCurrentModel;
	private final JCheckBox checkShowSamples;

	private float[] dash = { 5f, 5f };
	BasicStroke dashed = new BasicStroke(
			1.0f,                   // line thickness
			BasicStroke.CAP_BUTT,         // end‑cap style
			BasicStroke.JOIN_BEVEL,       // join style
			0f,                           // miter limit (irrelevant for simple dashes)
			dash,                         // the dash pattern
			0f                            // dash phase (offset into pattern)
	);


	protected AKickSamplePresenter(List<KickModelSample> samples, boolean isChip)
	{
		super(true, sample -> sample.isChip() == isChip);

		Chart2D kickChart = new Chart2D();
		kickChart.setName("Kicks");
		kickChart.getAxisY().setRangePolicy(new RangePolicyMinimumViewport(new Range(0, 5)));
		kickChart.getAxisX().setRangePolicy(new RangePolicyMinimumViewport(new Range(0, 6.5)));
		kickChart.getAxisX().setAxisTitle(new IAxis.AxisTitle("v [m/s]"));
		kickChart.getAxisY().setAxisTitle(new IAxis.AxisTitle("t [ms]"));
		kickChart.getAxisX().setPaintGrid(true);
		kickChart.getAxisY().setPaintGrid(true);
		kickChart.setGridColor(Color.LIGHT_GRAY);
		kickChart.setUseAntialiasing(true);

		plotSamples = new Trace2DSimple("Samples");
		plotSamples.setTracePainter(new TracePainterDisc(3));
		plotSamples.setColor(Color.GRAY);
		plotFitPoly1 = new Trace2DSimple("Linear");
		plotFitPoly1.setColor(Color.BLUE);
		plotFitPoly2 = new Trace2DSimple("Quadratic");
		plotFitPoly2.setColor(Color.CYAN);
		plotUsedSamples = new Trace2DSimple("Used samples");
		plotUsedSamples.setTracePainter(new TracePainterDisc(4));
		plotUsedSamples.setColor(Color.RED);

		plotCurrent = new Trace2DSimple("Current model");
		plotCurrent.setColor(Color.ORANGE.darker());
		plotCurrent.setStroke(dashed);

		kickChart.addTrace(plotSamples);
		kickChart.addTrace(plotUsedSamples);
		kickChart.addTrace(plotFitPoly1);
		kickChart.addTrace(plotFitPoly2);

		kickChart.addTrace(plotCurrent);


		kickChart.setPreferredSize(new Dimension(200, 200));
		kickChart.setMinimumSize(kickChart.getPreferredSize());

		var tableModel = new KickTableModel(samples, isChip);
		init(samples, tableModel);


		checkShowCurrentModel = new JCheckBox("Show current polys", false);
		checkShowCurrentModel.addActionListener(e -> setPlotVisibility());

		checkShowSamples = new JCheckBox("Show samples", true);
		checkShowSamples.addActionListener(e -> setPlotVisibility());

		getPlotArea().add(checkShowCurrentModel, "split 2, aligny center");
		getPlotArea().add(checkShowSamples, "wrap");


		getPlotArea().add(kickChart, "spanx 2, pushy, grow");

		setPlotVisibility();
	}


	@Override
	public String doComputeResult(List<KickModelSample> samples)
	{
		StringBuilder text = new StringBuilder();

		plotUsedSamples.removeAllPoints();

		var usedSamples = samples.stream()
				.filter(KickModelSample::isSampleUsed)
				.toList();

		text.append(computeFit(usedSamples));

		return text.toString();
	}


	protected abstract String computeFit(List<KickModelSample> samples);


	private record DBPath(int configID, int version) {}

	private record KickParams(double offset, double slope, double quad)
	{
		private KickParams(List<Double> params)
		{
			this(params.get(0), params.get(1), params.get(2));
		}
	}


	private void updateCurrentPolys()
	{
		if (!checkShowCurrentModel.isSelected())
		{
			return;
		}

		ConfigFileDatabaseManager database = getDatabase().orElse(null);

		if (database == null)
		{
			checkShowCurrentModel.setSelected(false);
			log.error("TigersBotManager not loaded. Select a different moduli");
			return;
		}

		DBPath path = ((ConfigFileDatabase) database.getDatabase()).getSavedConfigFiles().entrySet()
				.stream()
				.flatMap(outer -> outer.getValue().entrySet().stream()
						.filter(inner -> inner.getValue().containsKey("name")
								&& inner.getValue().get("name").equals("kick/cfg"))
						.map(inner -> new DBPath(outer.getKey(), inner.getKey())))
				.findFirst().orElse(null);

		if (path == null)
		{
			checkShowCurrentModel.setSelected(false);
			log.error("No kick config found");
			return;
		}
		database.addObserver(new IConfigFileDatabaseObserver()
		{
			@Override
			public void onConfigFileAdded(ConfigFile file)
			{
				updateCurrentPolys();
			}


			@Override
			public void onConfigFileRemoved(int configId, int version)
			{
				updateCurrentPolys();
			}
		});
		ConfigFile kickConfig = database.getSelectedEntry(path.configID(), path.version()).orElseThrow();

		var kickParamValues = getKickParams(kickConfig, getKickParamNames());
		if (kickParamValues.isEmpty())
		{
			return;
		}

		KickParams kickParams = new KickParams(kickParamValues.get());
		Poly2Fit currentModel = new Poly2Fit(kickParams.quad, kickParams.slope, kickParams.offset, 0);

		plotCurrent.removeAllPoints();

		for (double x = 0; x <= plotSamples.getMaxX(); x += 0.1)
		{
			plotCurrent.addPoint(x, currentModel.getYValue(x));
		}
	}


	protected abstract String[] getKickParamNames();


	private Optional<List<Double>> getKickParams(ConfigFile conf, String[] names)
	{
		List<Double> params = new ArrayList<>();
		for (String name : names)
		{
			var value = getConfigValueFromName(conf, name);
			if (value.isEmpty())
			{
				return Optional.empty();
			}
			params.add(value.get());
		}
		return Optional.of(params);
	}


	private Optional<Double> getConfigValueFromName(ConfigFile conf, String name)
	{
		int index = conf.getNames().indexOf(name);
		try
		{
			if (index == -1)
			{
				checkShowCurrentModel.setSelected(false);
				log.error("Could not find config value for: {}", name);
				return Optional.empty();
			}
			return Optional.of(Double.parseDouble(conf.getValues().get(index)));
		} catch (NumberFormatException e)
		{
			checkShowCurrentModel.setSelected(false);
			log.error("Could not parse config value: {}, from: {}", conf.getValues().get(index), name);
			return Optional.empty();
		}
	}


	private void setPlotVisibility()
	{
		plotSamples.setVisible(checkShowSamples.isSelected());
		plotUsedSamples.setVisible(checkShowSamples.isSelected());

		if (checkShowCurrentModel.isSelected())
		{
			updateCurrentPolys();
		}
		plotCurrent.setVisible(checkShowCurrentModel.isSelected());
	}


	private Optional<ConfigFileDatabaseManager> getDatabase()
	{
		if (SumatraModel.getInstance().isModuleLoaded(TigersBotManager.class))
		{
			return Optional.of(SumatraModel.getInstance().getModule(TigersBotManager.class).getConfigDatabase());
		}
		return Optional.empty();
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
		private final boolean isChip;

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
			return (int) samples.stream().filter(s -> s.isChip() == isChip).count();
		}


		@Override
		public Object getValueAt(final int row, final int col)
		{
			var sample = getSample(row);

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

			getSample(row).setSampleUsed((Boolean) value);
			fireTableCellUpdated(row, col);
		}


		private KickModelSample getSample(int row)
		{
			return samples.stream()
					.filter(s -> s.isChip() == isChip)
					.skip(row).findFirst().orElseThrow();
		}
	}
}
