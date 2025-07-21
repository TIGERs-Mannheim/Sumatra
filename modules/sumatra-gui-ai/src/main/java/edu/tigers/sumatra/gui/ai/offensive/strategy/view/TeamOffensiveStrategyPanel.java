/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.ai.offensive.strategy.view;

import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.strategy.EOffensiveStrategy;
import edu.tigers.sumatra.ids.BotID;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;


/**
 * Strategy panel for one team.
 */
public class TeamOffensiveStrategyPanel extends JPanel
{
	private static final long serialVersionUID = 5289153581163080893L;

	private final TableModel playConfigurationTableModel;
	private final TableModel offensiveActionsTableModel;


	public TeamOffensiveStrategyPanel()
	{
		setLayout(new MigLayout());

		playConfigurationTableModel = new TableModel(new String[] { "Bot", "Strategy" });
		final JTable playConfigurationTable = new JTable(playConfigurationTableModel);
		playConfigurationTable.setPreferredScrollableViewportSize(new Dimension(1920, 1080));
		JScrollPane playConfigurationScrollPane = new JScrollPane(playConfigurationTable);
		playConfigurationTable.setFillsViewportHeight(true);

		JPanel playConfigurationPanel = new JPanel(new MigLayout());
		JLabel playConfigurationLabel = new JLabel("Strategies");
		playConfigurationPanel.add(playConfigurationLabel, "wrap");
		playConfigurationPanel.add(playConfigurationScrollPane);


		offensiveActionsTableModel = new TableModel(new String[] { "Bot", "Action", "Move", "Viability" });
		final JTable offensiveActionsTable = new JTable(offensiveActionsTableModel);
		offensiveActionsTable.setPreferredScrollableViewportSize(new Dimension(1920, 1080));
		JScrollPane offensiveActionsScrollPane = new JScrollPane(offensiveActionsTable);
		offensiveActionsTable.setFillsViewportHeight(true);
		offensiveActionsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
		{
			private static final long serialVersionUID = -3070324342817507587L;


			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value,
					final boolean isSelected, final boolean hasFocus, final int row, final int column)
			{
				final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				c.setBackground((row % 2) == 0 ? Color.LIGHT_GRAY : Color.WHITE);
				return c;
			}
		});

		JPanel offensiveActionsPanel = new JPanel(new MigLayout());
		JLabel offensiveActionsLabel = new JLabel("Actions");
		offensiveActionsPanel.add(offensiveActionsLabel, "wrap");
		offensiveActionsPanel.add(offensiveActionsScrollPane);

		add(playConfigurationPanel, "wrap");
		add(offensiveActionsPanel, "span");
	}


	/**
	 * @param playConfiguration
	 */
	public void setPlayConfiguration(final Map<BotID, EOffensiveStrategy> playConfiguration)
	{
		String[][] bots = new String[playConfiguration.size()][2];
		int i = 0;
		for (Map.Entry<BotID, EOffensiveStrategy> entry : playConfiguration.entrySet())
		{
			bots[i][0] = entry.getKey().toString(); // botID
			bots[i][1] = entry.getValue().toString(); // strategy
			i++;
		}
		Arrays.sort(bots, Comparator.comparing(o -> o[0]));
		playConfigurationTableModel.setData(bots);
		playConfigurationTableModel.fireTableDataChanged();
	}


	/**
	 * @param offensiveActions
	 */
	public void setOffensiveActions(final Map<BotID, RatedOffensiveAction> offensiveActions)
	{
		var actions = new String[offensiveActions.size()][7];
		var i = 0;
		for (Map.Entry<BotID, RatedOffensiveAction> entry : offensiveActions.entrySet())
		{
			RatedOffensiveAction action = entry.getValue();
			var botID = entry.getKey();
			actions[i][0] = botID.toString();
			actions[i][1] = "invalid";
			actions[i][2] = action.getMove().toString();
			actions[i][3] = String.valueOf((int) (action.getViability().getScore() * 100) / 100.0);
			i++;
		}
		Arrays.sort(actions, (o1, o2) -> (int) (Double.parseDouble(o2[3]) - Double.parseDouble(o1[3])));
		offensiveActionsTableModel.setData(actions);
		offensiveActionsTableModel.fireTableDataChanged();
	}


	/**
	 * Simple TableModel that displays an array.
	 */
	private static class TableModel extends AbstractTableModel
	{
		/**
		 *
		 */
		private static final long serialVersionUID = -8703476015733916026L;
		private final String[] columns;
		private String[][] data;


		public TableModel(final String[] columnNames)
		{
			columns = columnNames;
			data = null;
		}


		@Override
		public int getColumnCount()
		{
			if (columns != null)
			{
				return columns.length;
			}
			return 0;
		}


		@Override
		public String getColumnName(final int column)
		{
			return columns[column];
		}


		@Override
		public int getRowCount()
		{
			if (data != null)
			{
				return data.length;
			}
			return 0;
		}


		@Override
		public String getValueAt(final int rowIndex, final int columnIndex)
		{
			return data[rowIndex][columnIndex];
		}


		/**
		 * @param data the data to set
		 */
		public void setData(final String[][] data)
		{
			this.data = data;
		}
	}
}
