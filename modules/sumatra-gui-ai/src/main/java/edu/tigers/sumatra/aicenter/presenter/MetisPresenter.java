package edu.tigers.sumatra.aicenter.presenter;

import static edu.tigers.sumatra.aicenter.view.MetisPanel.COL_ACTIVE;
import static edu.tigers.sumatra.aicenter.view.MetisPanel.COL_CALCULATOR;
import static edu.tigers.sumatra.aicenter.view.MetisPanel.COL_EXECUTED;
import static edu.tigers.sumatra.aicenter.view.MetisPanel.COL_TIME_AVG;
import static edu.tigers.sumatra.aicenter.view.MetisPanel.COL_TIME_REL;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.metis.ECalculator;
import edu.tigers.sumatra.aicenter.view.MetisPanel;
import edu.tigers.sumatra.filter.iir.ExponentialMovingAverageFilter;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.util.UiThrottler;


public class MetisPresenter
{
	private final EAiTeam team;
	private final Map<ECalculator, ExponentialMovingAverageFilter> averageValueMap = new EnumMap<>(ECalculator.class);
	private final MetisPanel metisPanel;
	private final UiThrottler aiFrameThrottler = new UiThrottler(1000);

	private boolean shown = false;


	public MetisPresenter(final EAiTeam team, MetisPanel metisPanel)
	{
		this.team = team;
		this.metisPanel = metisPanel;

		metisPanel.getResetButton().addActionListener(actionEvent -> averageValueMap.clear());
		metisPanel.getTable().getModel().addTableModelListener(new MyTableModelListener());

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
		for (ECalculator eCalc : ECalculator.values())
		{
			Integer value = lastAIInfoframe.getTacticalField().getMetisCalcTimes().get(eCalc);

			averageValueMap.computeIfAbsent(eCalc, c -> new ExponentialMovingAverageFilter(0.99));
			averageValueMap.get(eCalc).update(value);

			if (shown)
			{
				aiFrameThrottler.execute(() -> updateTable(lastAIInfoframe));
			}
		}
	}


	private void updateTable(final AIInfoFrame lastAIInfoframe)
	{
		JTable table = metisPanel.getTable();
		int sum = averageValueMap.values().stream().mapToInt(a -> (int) a.getState()).sum();
		int row = 0;
		for (ECalculator eCalc : ECalculator.values())
		{
			boolean execution = lastAIInfoframe.getTacticalField().getMetisExecutionStatus().get(eCalc);
			table.getModel().setValueAt(execution, row, COL_EXECUTED);

			table.getModel().setValueAt(100.0 * averageValueMap.get(eCalc).getState() / sum, row, COL_TIME_REL);
			table.getModel().setValueAt(averageValueMap.get(eCalc).getState(), row, COL_TIME_AVG);
			row++;
		}

		if (metisPanel.getAutomaticReorderingCheckBox().isSelected())
		{
			table.getRowSorter().allRowsChanged();
		}
	}


	public void setShown(final boolean shown)
	{
		this.shown = shown;
	}

	private class MyTableModelListener implements TableModelListener
	{
		@Override
		public void tableChanged(final TableModelEvent e)
		{
			JTable table = metisPanel.getTable();
			if (table.isEnabled() && (e.getColumn() == COL_ACTIVE))
			{
				int row = e.getFirstRow();
				ECalculator eCalc = ECalculator.valueOf(table.getModel().getValueAt(row, COL_CALCULATOR).toString());
				boolean active = (Boolean) table.getModel().getValueAt(row, COL_ACTIVE);

				SumatraModel.getInstance().getModuleOpt(Agent.class)
						.map(agent -> agent.getAi(team))
						.filter(Optional::isPresent)
						.map(Optional::get)
						.ifPresent(ai -> ai.getMetis().setCalculatorActive(eCalc, active));
			}
		}
	}
}
