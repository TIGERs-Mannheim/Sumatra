/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.aicenter.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.ai.lachesis.PlayPrioComparatorInfo;
import edu.tigers.sumatra.ai.lachesis.RoleFinderInfo;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * Control Plays and Roles
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AthenaControlPanel extends JPanel
{
	private static final Logger log = Logger.getLogger(AthenaControlPanel.class.getName());
	private static final long serialVersionUID = 8561402774656016979L;
	private static final int NUM_ROWS = 7;
	private static final String REPLACE_PATTERN = "[,; ]";
	
	private final transient List<IAthenaControlPanelObserver> observers = new CopyOnWriteArrayList<>();
	
	private final JTable table;
	private ETeamColor teamColor = ETeamColor.UNINITIALIZED;
	
	private final List<Component> components = new ArrayList<>();
	private final JLabel lblAvailableBots;
	
	private enum EColumn
	{
		PLAY(0, "Play", ""),
		MIN_ROLES(1, "minRoles", ""),
		DESIRED_ROLES(2, "desiredRoles", ""),
		MAX_ROLES(3, "maxRoles", ""),
		NUM_ROLES(4, "numRoles", ""),
		DESIRED_BOTS(5, "desiredBots", ""),
		ASSIGNED_BOTS(6, "assignedBots", ""),
		AI(7, "use AI", false),;
		
		private final int idx;
		private final String title;
		private final Object defValue;
		
		
		EColumn(final int idx, final String title, final Object defValue)
		{
			this.idx = idx;
			this.title = title;
			this.defValue = defValue;
		}
	}
	
	
	/**
	 * Default
	 */
	public AthenaControlPanel()
	{
		setLayout(new BorderLayout());
		TableModel model = new TableModel(getColumHeaders(), NUM_ROWS);
		table = new JTable(model);
		table.setEnabled(false);
		clear();
		table.addPropertyChangeListener(new ChangeListener());
		
		String[] playsData = new String[EPlay.values().length + 1];
		EPlay[] ePlays = EPlay.values();
		playsData[0] = "";
		for (int i = 0; i < ePlays.length; i++)
		{
			playsData[i + 1] = ePlays[i].name();
		}
		JComboBox<String> playCombo = new JComboBox<>(playsData);
		TableColumn playColumn = table.getColumnModel().getColumn(EColumn.PLAY.idx);
		playColumn.setMinWidth(150);
		playColumn.setCellEditor(new DefaultCellEditor(playCombo));
		
		DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer();
		textRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(EColumn.MIN_ROLES.idx).setCellRenderer(textRenderer);
		table.getColumnModel().getColumn(EColumn.DESIRED_ROLES.idx).setCellRenderer(textRenderer);
		table.getColumnModel().getColumn(EColumn.MAX_ROLES.idx).setCellRenderer(textRenderer);
		table.getColumnModel().getColumn(EColumn.NUM_ROLES.idx).setCellRenderer(textRenderer);
		
		JScrollPane scrlPane = new JScrollPane(table);
		scrlPane.setPreferredSize(new Dimension(0, 0));
		add(scrlPane, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		lblAvailableBots = new JLabel("Available bots: ?");
		buttonPanel.add(lblAvailableBots);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		add(buttonPanel, BorderLayout.NORTH);
		JButton btnClear = new JButton("Clear Plays");
		btnClear.addActionListener(new ClearListener());
		buttonPanel.add(btnClear);
		JButton btnAddRole = new JButton("Add Role");
		btnAddRole.addActionListener(new ChangeDesiredRolesListener(1));
		buttonPanel.add(btnAddRole);
		JButton btnRemoveRole = new JButton("Remove Role");
		btnRemoveRole.addActionListener(new ChangeDesiredRolesListener(-1));
		buttonPanel.add(btnRemoveRole);
		JButton btnClearRoles = new JButton("Clear Roles");
		btnClearRoles.addActionListener(new ClearRolesListener());
		buttonPanel.add(btnClearRoles);
		
		components.add(btnClearRoles);
		components.add(btnAddRole);
		components.add(btnClear);
		components.add(btnRemoveRole);
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IAthenaControlPanelObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IAthenaControlPanelObserver observer)
	{
		observers.remove(observer);
	}
	
	
	private String[] getColumHeaders()
	{
		String[] header = new String[EColumn.values().length];
		int i = 0;
		for (EColumn col : EColumn.values())
		{
			header[i] = col.title;
			i++;
		}
		return header;
	}
	
	
	private int valueOrZero(final String strInt)
	{
		if (StringUtils.isBlank(strInt))
		{
			return 0;
		}
		try
		{
			return Integer.valueOf(strInt);
		} catch (NumberFormatException err)
		{
			log.debug("Could not parse string: " + strInt, err);
		}
		return 0;
	}
	
	
	private void clear()
	{
		for (int j = 0; j < NUM_ROWS; j++)
		{
			for (int colIdx = 0; colIdx < (EColumn.values().length); colIdx++)
			{
				table.getModel().setValueAt(EColumn.values()[colIdx].defValue, j, colIdx);
			}
		}
	}
	
	
	private void sendRoleFinderInfos()
	{
		Map<EPlay, RoleFinderInfo> infos = new EnumMap<>(EPlay.class);
		Map<EPlay, Boolean> useAiPlays = new EnumMap<>(EPlay.class);
		for (int row = 0; row < NUM_ROWS; row++)
		{
			String strPlay = table.getModel().getValueAt(row, EColumn.PLAY.idx).toString();
			if (strPlay.isEmpty())
			{
				continue;
			}
			EPlay ePlay = EPlay.valueOf(strPlay);
			int minRoles = valueOrZero(table.getModel().getValueAt(row, EColumn.MIN_ROLES.idx).toString());
			int maxRoles = valueOrZero(table.getModel().getValueAt(row, EColumn.MAX_ROLES.idx).toString());
			int desiredRoles = valueOrZero(table.getModel().getValueAt(row, EColumn.DESIRED_ROLES.idx).toString());
			RoleFinderInfo info = new RoleFinderInfo(minRoles, maxRoles, desiredRoles);
			String desBots = table.getModel().getValueAt(row, EColumn.DESIRED_BOTS.idx).toString();
			if (!desBots.isEmpty())
			{
				String[] desBotsArr = desBots.split(REPLACE_PATTERN);
				for (String strDesBot : desBotsArr)
				{
					info.getDesiredBots().add(BotID.createBotId(valueOrZero(strDesBot), teamColor));
				}
			}
			Boolean override = Boolean.valueOf(table.getModel().getValueAt(row, EColumn.AI.idx).toString());
			infos.put(ePlay, info);
			useAiPlays.put(ePlay, override);
		}
		for (IAthenaControlPanelObserver o : observers)
		{
			o.onNewRoleFinderInfos(infos);
			o.onNewRoleFinderUseAiFlags(useAiPlays);
		}
	}
	
	
	public void setAiControlState(final EAIControlState mode)
	{
		switch (mode)
		{
			case EMERGENCY_MODE:
			case MATCH_MODE:
			case MIXED_TEAM_MODE:
				table.setEnabled(false);
				break;
			case OFF:
				clear();
				table.setEnabled(false);
				break;
			case TEST_MODE:
			default:
				clear();
				setDefaultPlays();
				table.setEnabled(true);
				break;
		}
	}
	
	
	private void setDefaultPlays()
	{
		String defPlays = SumatraModel.getInstance().getUserProperty(AthenaControlPanel.class + ".plays");
		if (defPlays == null)
		{
			return;
		}
		String[] splitPlays = defPlays.split(",");
		int i = 0;
		for (String play : splitPlays)
		{
			try
			{
				EPlay ePlay = EPlay.valueOf(play);
				table.getModel().setValueAt(ePlay.name(), i, EColumn.PLAY.idx);
				i++;
			} catch (IllegalArgumentException err)
			{
				log.debug("Could not convert to play: " + play, err);
			}
		}
	}
	
	
	/**
	 * New VisualizationFrame.
	 * 
	 * @param lastAIInfoframe
	 */
	public void updateVisualizationFrame(final VisualizationFrame lastAIInfoframe)
	{
		EventQueue.invokeLater(() -> updateTable(lastAIInfoframe));
	}
	
	
	private void updateTable(final VisualizationFrame lastAIInfoframe)
	{
		teamColor = lastAIInfoframe.getTeamColor();
		
		int nAvailable = lastAIInfoframe.getWorldFrame().getTigerBotsAvailable().size();
		lblAvailableBots.setText("Available Bots: " + nAvailable);
		
		int i = 0;
		Map<EPlay, RoleFinderInfo> newRoleFinderInfo = lastAIInfoframe.getRoleFinderInfos();
		List<Map.Entry<EPlay, RoleFinderInfo>> infoEntries = new ArrayList<>(
				newRoleFinderInfo.entrySet());
		infoEntries.sort(new PlayPrioComparatorInfo());
		for (Map.Entry<EPlay, RoleFinderInfo> entry : infoEntries)
		{
			EPlay ePlay = entry.getKey();
			RoleFinderInfo info = entry.getValue();
			table.getModel().setValueAt(ePlay.name(), i, EColumn.PLAY.idx);
			table.getModel().setValueAt(String.valueOf(info.getMinRoles()), i, EColumn.MIN_ROLES.idx);
			table.getModel().setValueAt(String.valueOf(info.getDesiredRoles()), i, EColumn.DESIRED_ROLES.idx);
			table.getModel().setValueAt(String.valueOf(info.getMaxRoles()), i, EColumn.MAX_ROLES.idx);
			int numRoles = (int) lastAIInfoframe.getAiInfos().values().stream()
					.filter(aiInfo -> aiInfo.getPlay().equals(ePlay.name())).count();
			table.getModel().setValueAt(String.valueOf(numRoles), i, EColumn.NUM_ROLES.idx);
			
			String desBots = botIds2String(info.getDesiredBots());
			table.getModel().setValueAt(desBots, i, EColumn.DESIRED_BOTS.idx);
			
			List<BotID> assignedIds = lastAIInfoframe.getAiInfos().entrySet().stream()
					.filter(e -> e.getValue().getPlay().equals(ePlay.name())).map(Map.Entry::getKey)
					.collect(Collectors.toList());
			String assignedBots = botIds2String(assignedIds);
			table.getModel().setValueAt(assignedBots, i, EColumn.ASSIGNED_BOTS.idx);
			i++;
		}
		for (int j = i; j < NUM_ROWS; j++)
		{
			for (int colIdx = 0; colIdx < EColumn.values().length; colIdx++)
			{
				table.getModel().setValueAt(EColumn.values()[colIdx].defValue, j, colIdx);
			}
		}
	}
	
	
	private String botIds2String(final List<BotID> bots)
	{
		StringBuilder desBots = new StringBuilder("");
		for (BotID botId : bots)
		{
			if (botId == null)
			{
				continue;
			}
			if (!desBots.toString().isEmpty())
			{
				desBots.append(',');
			}
			desBots.append(botId.getNumber());
		}
		return desBots.toString();
	}
	
	
	private class ChangeDesiredRolesListener implements ActionListener
	{
		private final int inc;
		
		
		/**
		 * 
		 */
		private ChangeDesiredRolesListener(final int inc)
		{
			this.inc = inc;
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			for (int selRowIdx : table.getSelectedRows())
			{
				String curVal = table.getModel().getValueAt(selRowIdx, EColumn.DESIRED_ROLES.idx).toString();
				int val = 1;
				try
				{
					val = Math.max(0, Integer.valueOf(curVal) + inc);
				} catch (NumberFormatException err)
				{
					log.debug("Could not parse int: " + curVal, err);
				}
				String strVal = String.valueOf(val);
				table.getModel().setValueAt(strVal, selRowIdx, EColumn.DESIRED_ROLES.idx);
				
				updateDesiredRoles(selRowIdx, val);
			}
			sendRoleFinderInfos();
		}
		
		
		private void updateDesiredRoles(final int selRowIdx, final int val)
		{
			List<String> desBots = getCurrentDesiredBots(selRowIdx);
			while (desBots.size() > val)
			{
				desBots.remove(desBots.size() - 1);
			}
			
			Set<String> usedBotIds = getUsedBotIds();
			
			while (desBots.size() < val)
			{
				int botId = 0;
				while (usedBotIds.contains(String.valueOf(botId)))
				{
					botId++;
				}
				desBots.add(String.valueOf(botId));
			}
			
			String newValue = StringUtils.join(desBots, ",");
			table.getModel().setValueAt(newValue, selRowIdx, EColumn.DESIRED_BOTS.idx);
		}
		
		
		private List<String> getCurrentDesiredBots(final int selRowIdx)
		{
			String desBotsStr = table.getModel().getValueAt(selRowIdx, EColumn.DESIRED_BOTS.idx).toString();
			List<String> desBots = new ArrayList<>(Arrays.asList(desBotsStr.split(REPLACE_PATTERN)));
			if ("".equals(desBots.get(0)))
			{
				desBots.clear();
			}
			return desBots;
		}
		
		
		private Set<String> getUsedBotIds()
		{
			Set<String> usedBotIds = new HashSet<>();
			for (int rowId = 0; rowId < table.getRowCount(); rowId++)
			{
				String desiredBotsStr = table.getModel().getValueAt(rowId, EColumn.DESIRED_BOTS.idx).toString();
				usedBotIds.addAll(Arrays.asList(desiredBotsStr.split(REPLACE_PATTERN)));
			}
			return usedBotIds;
		}
	}
	
	
	private class ChangeListener implements PropertyChangeListener
	{
		@Override
		public void propertyChange(final PropertyChangeEvent evt)
		{
			if (table.isEnabled())
			{
				StringBuilder sb = new StringBuilder();
				boolean first = true;
				for (int i = 0; i < NUM_ROWS; i++)
				{
					String play = table.getModel().getValueAt(i, EColumn.PLAY.idx).toString();
					if (play.isEmpty())
					{
						continue;
					}
					if (!first)
					{
						sb.append(",");
					}
					first = false;
					sb.append(play);
				}
				SumatraModel.getInstance().setUserProperty(AthenaControlPanel.class + ".plays", sb.toString());
				sendRoleFinderInfos();
			}
		}
	}
	
	private class ClearListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			clear();
			sendRoleFinderInfos();
		}
	}
	
	private class ClearRolesListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			clearRoles();
			sendRoleFinderInfos();
		}
		
		
		private void clearRoles()
		{
			for (int j = 0; j < NUM_ROWS; j++)
			{
				table.getModel()
						.setValueAt(EColumn.values()[EColumn.DESIRED_ROLES.idx].defValue, j, EColumn.DESIRED_ROLES.idx);
				table.getModel().setValueAt(EColumn.values()[EColumn.MIN_ROLES.idx].defValue, j, EColumn.MIN_ROLES.idx);
				table.getModel().setValueAt(EColumn.values()[EColumn.MAX_ROLES.idx].defValue, j, EColumn.MAX_ROLES.idx);
				table.getModel().setValueAt(EColumn.values()[EColumn.DESIRED_BOTS.idx].defValue, j,
						EColumn.DESIRED_BOTS.idx);
			}
		}
	}
	
	private static class TableModel extends DefaultTableModel
	{
		/**  */
		private static final long serialVersionUID = -3700170198897729348L;
		
		
		private TableModel(final Object[] columnNames, final int rowCount)
		{
			super(columnNames, rowCount);
		}
		
		
		@Override
		public Class<?> getColumnClass(final int columnIndex)
		{
			if (columnIndex == EColumn.AI.idx)
			{
				return Boolean.class;
			}
			return super.getColumnClass(columnIndex);
		}
		
		
		@Override
		public boolean isCellEditable(final int row, final int column)
		{
			return (column != EColumn.NUM_ROLES.idx) && super.isCellEditable(row, column);
		}
	}
	
	
	@Override
	public void setEnabled(final boolean enabled)
	{
		super.setEnabled(enabled);
		for (Component comp : components)
		{
			comp.setEnabled(enabled);
		}
	}
}
