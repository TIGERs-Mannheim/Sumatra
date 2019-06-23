/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.offensive.view;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import edu.tigers.sumatra.ai.data.OffensiveStrategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.data.SpecialMoveCommand;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
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
	/**  */
	private static final long	serialVersionUID						= 5289153581163080893L;
	
	private JPanel					numberPanel								= null;
	private JLabel					numberLabel								= null;
	private JLabel					minNumberLabel							= null;
	private JTextField			minNumberText							= null;
	private JLabel					maxNumberLabel							= null;
	private JTextField			maxNumberText							= null;
	
	private JPanel					desiredBotsPanel						= null;
	private JLabel					desiredBotsLabel						= null;
	private JTable					desiredBotsTable						= null;
	private TableModel			desiredBotsTableModel				= null;
	
	private JPanel					playConfigurationPanel				= null;
	private JLabel					playConfigurationLabel				= null;
	private JTable					playConfigurationTable				= null;
	private TableModel			playConfigurationTableModel		= null;
	
	private JPanel					unassignedStrategiesPanel			= null;
	private JLabel					unassignedStrategiesLabel			= null;
	private JTable					unassignedStrategiesTable			= null;
	private TableModel			unassignedStrategiesTableModel	= null;
	
	private JPanel					specialMoveCommandsPanel			= null;
	private JLabel					specialMoveCommandsLabel			= null;
	private JTable					specialMoveCommandsTable			= null;
	private TableModel			specialMoveCommandsTableModel		= null;
	
	private JPanel					offensiveActionsPanel				= null;
	private JLabel					offensiveActionsLabel				= null;
	private JTable					offensiveActionsTable				= null;
	private TableModel			offensiveActionsTableModel			= null;
	
	
	/**
	 * Default
	 */
	public TeamOffensiveStrategyPanel()
	{
		setLayout(new MigLayout());
		
		numberPanel = new JPanel(new MigLayout("fill", "", ""));
		numberLabel = new JLabel("Number of bots:");
		
		minNumberLabel = new JLabel("min.");
		minNumberText = new JTextField("0");
		minNumberText.setEditable(false);
		
		maxNumberLabel = new JLabel("max.");
		maxNumberText = new JTextField("0");
		maxNumberText.setEditable(false);
		
		numberPanel.add(numberLabel);
		numberPanel.add(minNumberLabel);
		numberPanel.add(minNumberText);
		numberPanel.add(maxNumberLabel);
		numberPanel.add(maxNumberText);
		
		String botIdHeader = "Bot ID";
		
		desiredBotsPanel = new JPanel(new MigLayout());
		desiredBotsLabel = new JLabel("Desired Bots:");
		desiredBotsTableModel = new TableModel(new String[] { botIdHeader });
		desiredBotsTable = new JTable(desiredBotsTableModel);
		desiredBotsTable.setPreferredScrollableViewportSize(new Dimension(1920, 1080));
		JScrollPane desiredBotsScrollPane = new JScrollPane(desiredBotsTable);
		desiredBotsTable.setFillsViewportHeight(true);
		
		desiredBotsPanel.add(desiredBotsLabel, "wrap");
		desiredBotsPanel.add(desiredBotsScrollPane);
		
		playConfigurationPanel = new JPanel(new MigLayout());
		playConfigurationLabel = new JLabel("Current Configuration:");
		playConfigurationTableModel = new TableModel(new String[] { botIdHeader, "Offensive Strategy" });
		playConfigurationTable = new JTable(playConfigurationTableModel);
		playConfigurationTable.setPreferredScrollableViewportSize(new Dimension(1920, 1080));
		JScrollPane playConfigurationScrollPane = new JScrollPane(playConfigurationTable);
		playConfigurationTable.setFillsViewportHeight(true);
		
		playConfigurationPanel.add(playConfigurationLabel, "wrap");
		playConfigurationPanel.add(playConfigurationScrollPane);
		
		unassignedStrategiesPanel = new JPanel(new MigLayout());
		unassignedStrategiesLabel = new JLabel("Unassigned Startegies:");
		unassignedStrategiesTableModel = new TableModel(new String[] { "Unassigned Strategy" });
		unassignedStrategiesTable = new JTable(unassignedStrategiesTableModel);
		unassignedStrategiesTable.setPreferredScrollableViewportSize(new Dimension(1920, 1080));
		JScrollPane unassignedStrategiesScrollPane = new JScrollPane(unassignedStrategiesTable);
		unassignedStrategiesTable.setFillsViewportHeight(true);
		
		unassignedStrategiesPanel.add(unassignedStrategiesLabel, "wrap");
		unassignedStrategiesPanel.add(unassignedStrategiesScrollPane);
		
		specialMoveCommandsPanel = new JPanel(new MigLayout());
		specialMoveCommandsLabel = new JLabel("Special Move Commands:");
		specialMoveCommandsTableModel = new TableModel(new String[] { "Move Positions" });
		specialMoveCommandsTable = new JTable(specialMoveCommandsTableModel);
		specialMoveCommandsTable.setPreferredScrollableViewportSize(new Dimension(1920, 1080));
		JScrollPane specialMoveCommandsScrollPane = new JScrollPane(specialMoveCommandsTable);
		specialMoveCommandsTable.setFillsViewportHeight(true);
		
		specialMoveCommandsPanel.add(specialMoveCommandsLabel, "wrap");
		specialMoveCommandsPanel.add(specialMoveCommandsScrollPane);
		
		
		offensiveActionsPanel = new JPanel(new MigLayout());
		offensiveActionsLabel = new JLabel("Offensive Actions:");
		offensiveActionsTableModel = new TableModel(new String[] { botIdHeader, "Pass Target", "Pass Target Bot ID",
				"Pass Target Rating", "Direct Shot and Clearing Target", "Action", "Value" });
		offensiveActionsTable = new JTable(offensiveActionsTableModel);
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
		
		add(numberPanel, "wrap,span");
		add(desiredBotsPanel);
		add(playConfigurationPanel);
		add(unassignedStrategiesPanel);
		add(specialMoveCommandsPanel, "wrap");
		add(offensiveActionsPanel, "span");
	}
	
	
	/**
	 * @param num minimum number of bots need
	 */
	public void setMinNumberOfBots(final int num)
	{
		minNumberText.setText(String.valueOf(num));
	}
	
	
	/**
	 * @param num max number of bots that can be handled
	 */
	public void setMaxNumberOfBots(final int num)
	{
		maxNumberText.setText(String.valueOf(num));
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
	
	
	/**
	 * @param unassignedStartegies
	 */
	public void setUnassignedStrategies(final List<EOffensiveStrategy> unassignedStartegies)
	{
		String[][] strategies = new String[unassignedStartegies.size()][1];
		int i = 0;
		for (EOffensiveStrategy strategy : unassignedStartegies)
		{
			strategies[i][0] = strategy.toString();
			i++;
		}
		unassignedStrategiesTableModel.setData(strategies);
		unassignedStrategiesTableModel.fireTableDataChanged();
	}
	
	
	/**
	 * @param specialMoveCommands
	 */
	public void setSpecialMoveCommands(final List<SpecialMoveCommand> specialMoveCommands)
	{
		String[][] commands = new String[specialMoveCommands.size()][1];
		int i = 0;
		for (SpecialMoveCommand command : specialMoveCommands)
		{
			commands[i][0] = "";
			for (IVector2 position : command.getMovePosition())
			{
				commands[i][0] += "(" + position.x() + ", " + position.y() + ")";
			}
			i++;
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
			actions[i][0] = entry.getKey().toString();
			
			IPassTarget passTarget = action.getPassTarget();
			if (passTarget != null)
			{
				actions[i][1] = "(" + (((int) (passTarget.getKickerPos().x() * 100)) / 100.0) + ", "
						+ (((int) (passTarget.getKickerPos().y() * 100)) / 100.0) + ")";
				actions[i][2] = passTarget.getBotId().toString();
				actions[i][3] = Double.toString((int) (passTarget.getScore() * 1000) / 1000.0);
			} else
			{
				actions[i][1] = null;
				actions[i][2] = null;
				actions[i][3] = null;
			}
			
			DynamicPosition directShotAndClearingTarget = action.getDirectShotAndClearingTarget();
			if (directShotAndClearingTarget != null)
			{
				actions[i][4] = "(" + directShotAndClearingTarget.x() + ", " + directShotAndClearingTarget.y() + ")";
			} else
			{
				actions[i][4] = null;
			}
			
			actions[i][5] = action.getType().toString();
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
		private static final long	serialVersionUID	= -8703476015733916026L;
		private final String[]		columns;
		private String[][]			data;
		
		
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
