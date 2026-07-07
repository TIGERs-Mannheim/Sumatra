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
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;


public abstract class ASamplePresenter<T extends Sample>
{
	@Getter
	private final JPanel container;
	private final JTextArea resultArea;
	@Getter
	private final JPanel plotArea;
	@Getter
	private final JTable table = new JTable();

	private final Predicate<T> isSampleUsablePredicate;
	private List<T> samples = new ArrayList<>();
	private AbstractTableModel tableModel;


	protected ASamplePresenter(boolean createPlotArea)
	{
		this(createPlotArea, e -> true);
	}


	protected ASamplePresenter(boolean createPlotArea, Predicate<T> isSampleUsablePredicate)
	{
		this.isSampleUsablePredicate = isSampleUsablePredicate;

		resultArea = new JTextArea(6, 50);
		resultArea.setEditable(false);
		resultArea.setMinimumSize(resultArea.getPreferredSize());
		resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, resultArea.getFont().getSize()));

		JPanel resultPanel = new JPanel(new BorderLayout());
		resultPanel.setBorder(BorderFactory.createTitledBorder("Result"));
		resultPanel.add(resultArea);


		table.setFillsViewportHeight(true);
		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "selectDeselectAll");
		table.getActionMap().put("selectDeselectAll", new TableKeyListener(table));

		plotArea = new JPanel(new MigLayout("fill"));
		if (createPlotArea)
		{
			JPanel topHalf = new JPanel(new MigLayout("fill"));
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			splitPane.setTopComponent(topHalf);
			splitPane.setBottomComponent(plotArea);
			splitPane.setResizeWeight(1);

			topHalf.add(resultPanel);
			topHalf.add(getControlPanel(table), "wrap");
			topHalf.add(new JScrollPane(table), "spanx 2, pushy, grow, wrap");

			container = new JPanel(new MigLayout("fill"));
			container.add(splitPane, "span, pushy, grow, wrap");
		} else
		{
			container = new JPanel(new MigLayout("fill"));
			container.add(resultPanel);
			container.add(getControlPanel(table), "wrap");
			container.add(new JScrollPane(table), "spanx 2, pushy, grow, wrap");
		}

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
			samples.removeIf(s -> isSampleUsablePredicate.test(s) && !s.isSampleUsed());
			update();
		});

		JButton btnDeleteSamples = new JButton("Delete selected sample(s)");
		btnDeleteSamples.addActionListener(l -> {
			var usableSamples = samples.stream()
					.filter(isSampleUsablePredicate)
					.toList();

			List<T> selectedSamples = Arrays.stream(table.getSelectedRows())
					.mapToObj(usableSamples::get)
					.toList();

			samples.removeAll(selectedSamples);
			update();
		});


		JPanel controlPanel = new JPanel(new MigLayout());
		controlPanel.setBorder(BorderFactory.createTitledBorder("Data Manipulation"));
		controlPanel.add(btnDeleteUnused, "grow, wrap");
		controlPanel.add(btnDeleteSamples, "grow, wrap");

		if (isFilterOutBadSamplesImplemented())
		{
			JButton btnFilterSamples = new JButton("Filter out bad samples");
			btnFilterSamples.addActionListener(l -> filterOutBadSamples(samples));
			controlPanel.add(btnFilterSamples);
		}
		return controlPanel;
	}


	protected boolean isFilterOutBadSamplesImplemented()
	{
		return false;
	}


	protected void filterOutBadSamples(List<T> samples)
	{
	}


	private void computeResult()
	{
		resultArea.setText(doComputeResult(samples.stream().filter(isSampleUsablePredicate).toList()));
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
			var usableSamples = samples.stream().filter(isSampleUsablePredicate).toList();

			boolean allUsed = Arrays.stream(table.getSelectedRows())
					.mapToObj(usableSamples::get)
					.allMatch(T::isSampleUsed);
			if (allUsed)
			{
				Arrays.stream(table.getSelectedRows())
						.mapToObj(usableSamples::get)
						.forEach(s -> s.setSampleUsed(false));
			} else
			{
				Arrays.stream(table.getSelectedRows())
						.mapToObj(usableSamples::get)
						.forEach(s -> s.setSampleUsed(true));
			}
			SwingUtilities.invokeLater(table::repaint);
			computeResult();
		}
	}
}
