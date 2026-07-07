package edu.tigers.sumatra.gui.ai.offensive.statistics.view;

import edu.tigers.sumatra.ai.metis.pass.PassStats;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Visualize some interesting pass stats in a histogram
 */
public class PassStatsHistogram extends JPanel
{
	private static class LabelGenerator extends StandardCategoryItemLabelGenerator
	{
		private final List<Integer> labels;


		public LabelGenerator(List<Integer> labels)
		{
			this.labels = labels;
		}


		@Override
		public String generateLabel(CategoryDataset dataset, int row, int column)
		{
			return MessageFormat.format("{0}", labels.get(column));
		}


		@Override
		public boolean equals(Object o)
		{
			if (o == null || getClass() != o.getClass())
			{
				return false;
			}
			if (!super.equals(o))
			{
				return false;
			}
			LabelGenerator that = (LabelGenerator) o;
			return Objects.equals(labels, that.labels);
		}


		@Override
		public int hashCode()
		{
			return Objects.hash(super.hashCode(), labels);
		}
	}


	public PassStatsHistogram(PassStats stats)
	{
		setLayout(new BorderLayout());
		if (stats != null)
		{
			JFreeChart chart = getHistogramChart(stats);
			var chartPanel = new ChartPanel(chart);
			add(chartPanel, BorderLayout.CENTER);
		}
	}


	private JFreeChart getHistogramChart(PassStats stats)
	{
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		int numOfGroups = 5;
		var data = stats.getInterceptIntervalSuccessRatios(numOfGroups);
		List<Integer> labels = new ArrayList<>();
		for (var entry : data.entrySet())
		{
			dataset.addValue(
					entry.getValue().getFirst(), "", String.format("%.2f", (double) entry.getKey() / numOfGroups));
			labels.add(entry.getValue().getSecond());
		}

		var chart = ChartFactory.createBarChart("Pass Success Ratios", "Intercept score", "success ratio", dataset);

		Color bg = UIManager.getColor("Panel.background");
		Color fg = UIManager.getColor("Label.foreground");
		Color grid = fg.darker();

		StandardChartTheme theme = (StandardChartTheme) StandardChartTheme.createJFreeTheme();

		theme.setTitlePaint(fg);
		theme.setRangeGridlinePaint(grid);
		theme.setPlotBackgroundPaint(bg);
		theme.setChartBackgroundPaint(bg);
		theme.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
		theme.setBarPainter(new StandardBarPainter());
		theme.setAxisLabelPaint(fg);
		theme.apply(chart);
		chart.getCategoryPlot().setOutlineVisible(false);
		chart.getCategoryPlot().getRangeAxis().setAxisLineVisible(false);
		chart.getCategoryPlot().getRangeAxis().setTickMarksVisible(false);
		chart.getCategoryPlot().setRangeGridlineStroke(new BasicStroke());
		chart.getCategoryPlot().getRangeAxis().setTickLabelPaint(fg);
		chart.getCategoryPlot().getDomainAxis().setTickLabelPaint(fg);
		chart.setTextAntiAlias(true);
		chart.setAntiAlias(true);
		chart.getCategoryPlot().getRenderer().setSeriesPaint(0, fg);
		BarRenderer rend = (BarRenderer) chart.getCategoryPlot().getRenderer();
		chart.removeLegend();

		rend.setShadowVisible(true);
		rend.setShadowXOffset(2);
		rend.setShadowYOffset(0);
		rend.setShadowPaint(grid);
		rend.setMaximumBarWidth(0.1);
		rend.setDefaultItemLabelGenerator(new LabelGenerator(labels));
		rend.setDefaultItemLabelsVisible(true);
		rend.setDefaultItemLabelPaint(fg);

		return chart;
	}
}
