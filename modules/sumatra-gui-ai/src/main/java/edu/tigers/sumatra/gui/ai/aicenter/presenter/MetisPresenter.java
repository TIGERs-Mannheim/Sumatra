/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.ai.aicenter.presenter;

import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.filter.iir.ExponentialMovingAverageFilter;
import edu.tigers.sumatra.gui.ai.aicenter.view.MetisPanel;
import edu.tigers.sumatra.util.UiThrottler;
import lombok.Setter;

import javax.swing.JTable;
import java.util.HashMap;
import java.util.Map;

import static edu.tigers.sumatra.gui.ai.aicenter.view.MetisPanel.COL_CALCULATOR;
import static edu.tigers.sumatra.gui.ai.aicenter.view.MetisPanel.COL_EXECUTED;
import static edu.tigers.sumatra.gui.ai.aicenter.view.MetisPanel.COL_TIME_AVG;
import static edu.tigers.sumatra.gui.ai.aicenter.view.MetisPanel.COL_TIME_REL;


public class MetisPresenter
{
	private final Map<Class<? extends ACalculator>, ExponentialMovingAverageFilter> averageValueMap = new HashMap<>();
	private final MetisPanel metisPanel;
	private final UiThrottler aiFrameThrottler = new UiThrottler(1000);

	@Setter
	private boolean shown = false;


	public MetisPresenter(MetisPanel metisPanel)
	{
		this.metisPanel = metisPanel;

		metisPanel.getResetButton().addActionListener(actionEvent -> averageValueMap.clear());

		reset();
		aiFrameThrottler.start();
	}


	/**
	 * Set all values to default
	 */
	private void reset()
	{
		metisPanel.reset();
		averageValueMap.clear();
	}


	public void updateAIInfoFrame(final AIInfoFrame lastAIInfoframe)
	{
		for (var entry : lastAIInfoframe.getAthenaAiFrame().getMetisAiFrame().getCalculatorExecutions().entrySet())
		{
			var value = entry.getValue();
			var eCalc = entry.getKey();

			averageValueMap.computeIfAbsent(eCalc, c -> new ExponentialMovingAverageFilter(0.99));
			averageValueMap.get(eCalc).update(value.getProcessingTime() / 1e6);

			if (shown)
			{
				aiFrameThrottler.execute(() -> updateTable(lastAIInfoframe));
			}
		}
	}


	private void updateTable(final AIInfoFrame lastAIInfoframe)
	{
		JTable table = metisPanel.getTable();
		double sum = averageValueMap.values().stream().mapToDouble(ExponentialMovingAverageFilter::getState).sum();
		int row = 0;
		var calculatorExecutions = lastAIInfoframe.getAthenaAiFrame().getMetisAiFrame().getCalculatorExecutions();
		metisPanel.setRowCount(calculatorExecutions.size());
		for (var entry : calculatorExecutions.entrySet())
		{
			var eCalc = entry.getKey();
			var execution = entry.getValue();
			table.getModel().setValueAt(eCalc.getSimpleName(), row, COL_CALCULATOR);
			table.getModel().setValueAt(execution.isExecuted(), row, COL_EXECUTED);

			table.getModel().setValueAt(100.0 * averageValueMap.get(eCalc).getState() / sum, row, COL_TIME_REL);
			table.getModel().setValueAt(averageValueMap.get(eCalc).getState(), row, COL_TIME_AVG);
			row++;
		}

		if (metisPanel.getAutomaticReorderingCheckBox().isSelected())
		{
			table.getRowSorter().allRowsChanged();
		}
	}
}
