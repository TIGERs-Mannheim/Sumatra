/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.offensive.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.strategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTarget;
import edu.tigers.sumatra.ai.metis.support.passtarget.IRatedPassTarget;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import net.miginfocom.swing.MigLayout;


/**
 * Strategy panel for one team.
 * 
 * @author Lukas Schmierer <lukas.schmierer@dlr.de>
 */
public class TeamOffensiveStrategyPanel extends JPanel
{
	private static final long serialVersionUID = 5289153581163080893L;
	
	private final TableModel desiredBotsTableModel;
	private final TableModel playConfigurationTableModel;
	private final TableModel specialMoveCommandsTableModel;
	private final TableModel offensiveActionsTableModel;
	
	
	/**
	 * Default
	 */
	public TeamOffensiveStrategyPanel()
	{
		setLayout(new MigLayout());
		
		String botIdHeader = "Bot ID";
		
		final JPanel desiredBotsPanel = new JPanel(new MigLayout());
		final JLabel desiredBotsLabel = new JLabel("Desired Bots:");
		desiredBotsTableModel = new TableModel(new String[] { botIdHeader });
		final JTable desiredBotsTable = new JTable(desiredBotsTableModel);
		desiredBotsTable.setPreferredScrollableViewportSize(new Dimension(1920, 1080));
		JScrollPane desiredBotsScrollPane = new JScrollPane(desiredBotsTable);
		desiredBotsTable.setFillsViewportHeight(true);
		
		desiredBotsPanel.add(desiredBotsLabel, "wrap");
		desiredBotsPanel.add(desiredBotsScrollPane);
		
		final JPanel playConfigurationPanel = new JPanel(new MigLayout());
		final JLabel playConfigurationLabel = new JLabel("Current Configuration:");
		playConfigurationTableModel = new TableModel(new String[] { botIdHeader, "Offensive Strategy" });
		final JTable playConfigurationTable = new JTable(playConfigurationTableModel);
		playConfigurationTable.setPreferredScrollableViewportSize(new Dimension(1920, 1080));
		JScrollPane playConfigurationScrollPane = new JScrollPane(playConfigurationTable);
		playConfigurationTable.setFillsViewportHeight(true);
		
		playConfigurationPanel.add(playConfigurationLabel, "wrap");
		playConfigurationPanel.add(playConfigurationScrollPane);
		
		final JPanel specialMoveCommandsPanel = new JPanel(new MigLayout());
		final JLabel specialMoveCommandsLabel = new JLabel("Special Move Commands:");
		specialMoveCommandsTableModel = new TableModel(new String[] { "Move Positions" });
		final JTable specialMoveCommandsTable = new JTable(specialMoveCommandsTableModel);
		specialMoveCommandsTable.setPreferredScrollableViewportSize(new Dimension(1920, 1080));
		JScrollPane specialMoveCommandsScrollPane = new JScrollPane(specialMoveCommandsTable);
		specialMoveCommandsTable.setFillsViewportHeight(true);
		
		specialMoveCommandsPanel.add(specialMoveCommandsLabel, "wrap");
		specialMoveCommandsPanel.add(specialMoveCommandsScrollPane);
		
		
		final JPanel offensiveActionsPanel = new JPanel(new MigLayout());
		final JLabel offensiveActionsLabel = new JLabel("Offensive Actions:");
		offensiveActionsTableModel = new TableModel(new String[] { botIdHeader, "Pass Target", "Pass Target Bot ID",
				"Pass Target Rating", "Kick Target", "Action", "Value" });
		final JTable offensiveActionsTable = new JTable(offensiveActionsTableModel);
		offensiveActionsTable.setPreferredScrollableViewportSize(new Dimension(1920, 1080));
		JScrollPane offensiveActionsScrollPane = new JScrollPane(offensiveActionsTable);
		offensiveActionsTable.setFillsViewportHeight(true);
		offensiveActionsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
		{
			/**  */
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
		
		offensiveActionsPanel.add(offensiveActionsLabel, "wrap");
		offensiveActionsPanel.add(offensiveActionsScrollPane);
		
		add(desiredBotsPanel);
		add(playConfigurationPanel);
		add(specialMoveCommandsPanel, "wrap");
		add(offensiveActionsPanel, "span");
	}
	
	
	/**
	 * @param desiredBots
	 */
	public void setDesiredBots(final List<BotID> desiredBots)
	{
		String[][] bots = new String[desiredBots.size()][1];
		for (int i = 0; i < desiredBots.size(); i++)
		{
			bots[i][0] = String.valueOf(desiredBots.get(i));
		}
		Arrays.sort(bots, Comparator.comparing(o -> o[0]));
		desiredBotsTableModel.setData(bots);
		desiredBotsTableModel.fireTableDataChanged();
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
	
	
	public void setSpecialMoveCommands(final IPassTarget passTarget)
	{
		String[][] commands = new String[1][1];
		if (passTarget != null)
		{
			IVector2 position = passTarget.getPos();
			commands[0][0] = "(" + position.x() + ", " + position.y() + ")";
		} else
		{
			commands[0][0] = "";
		}
		specialMoveCommandsTableModel.setData(commands);
		specialMoveCommandsTableModel.fireTableDataChanged();
	}
	
	
	/**
	 * @param offensiveActions
	 */
	public void setOffensiveActions(final Map<BotID, OffensiveAction> offensiveActions)
	{
		String[][] actions = new String[offensiveActions.size()][7];
		int i = 0;
		for (Map.Entry<BotID, OffensiveAction> entry : offensiveActions.entrySet())
		{
			OffensiveAction action = entry.getValue();
			final BotID botID = entry.getKey();
			actions[i][0] = botID.toString();
			
			IRatedPassTarget passTarget = action.getRatedPassTarget().orElse(null);
			if (passTarget != null)
			{
				actions[i][1] = "(" + (((int) (passTarget.getPos().x() * 100)) / 100.0) + ", "
						+ (((int) (passTarget.getPos().y() * 100)) / 100.0) + ")";
				actions[i][2] = passTarget.getBotId().toString();
				actions[i][3] = Double.toString((int) (passTarget.getScore() * 1000) / 1000.0);
			} else
			{
				actions[i][1] = null;
				actions[i][2] = null;
				actions[i][3] = null;
			}
			
			DynamicPosition kickTarget = action.getKickTarget().getTarget();
			if (kickTarget != null)
			{
				actions[i][4] = "(" + kickTarget.getPos().x() + ", " + kickTarget.getPos().y() + ")";
			} else
			{
				actions[i][4] = null;
			}
			
			actions[i][5] = action.getAction().toString();
			actions[i][6] = String.valueOf((int) (action.getViability() * 100) / 100.0);
			i++;
		}
		Arrays.sort(actions, (o1, o2) -> (int) (Double.parseDouble(o2[6]) - Double.parseDouble(o1[6])));
		offensiveActionsTableModel.setData(actions);
		offensiveActionsTableModel.fireTableDataChanged();
	}
	
	/**
	 * Simple TableModel that displays an array.
	 * 
	 * @author Lukas Schmierer <lukas.schmierer@dlr.de>
	 */
	private static class TableModel extends AbstractTableModel
	{
		/**  */
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
		 * @return the data
		 */
		@SuppressWarnings("unused")
		public String[][] getData()
		{
			return data;
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
