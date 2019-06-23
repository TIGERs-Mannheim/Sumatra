/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 7, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.PlayPrioComparatorInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.RoleFinderInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;


/**
 * Control Plays and Roles
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AthenaControlPanel extends JPanel implements IAIModeChanged, IAIObserver
{
	/**  */
	private static final long								serialVersionUID	= 8561402774656016979L;
	private static final int								NUM_ROWS				= 5;
	
	private final List<IAthenaControlPanelObserver>	observers			= new CopyOnWriteArrayList<IAthenaControlPanelObserver>();
	
	private final JTable										table;
	private ETeamColor										teamColor			= ETeamColor.UNINITIALIZED;
	
	private List<Component>									components			= new ArrayList<>();
	
	private enum EColumn
	{
		PLAY(0, "Play", ""),
		MIN_ROLES(1, "minRoles", ""),
		DESIRED_ROLES(2, "desiredRoles", ""),
		MAX_ROLES(3, "maxRoles", ""),
		NUM_ROLES(4, "numRoles", ""),
		DESIRED_BOTS(5, "desiredBots", ""),
		ASSIGNED_BOTS(6, "assignedBots", ""),
		OVERRIDE(7, "override", true), ;
		
		private final int		idx;
		private final String	title;
		private final Object	defValue;
		
		
		private EColumn(final int idx, final String title, final Object defValue)
		{
			this.idx = idx;
			this.title = title;
			this.defValue = defValue;
		}
	}
	
	
	/**
	  * 
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
		JComboBox<String> playCombo = new JComboBox<String>(playsData);
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
		try
		{
			return Integer.valueOf(strInt);
		} catch (NumberFormatException err)
		{
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
	
	
	private void clearRoles()
	{
		for (int j = 0; j < NUM_ROWS; j++)
		{
			table.getModel()
					.setValueAt(EColumn.values()[EColumn.DESIRED_ROLES.idx].defValue, j, EColumn.DESIRED_ROLES.idx);
			table.getModel().setValueAt(EColumn.values()[EColumn.MIN_ROLES.idx].defValue, j, EColumn.MIN_ROLES.idx);
			table.getModel().setValueAt(EColumn.values()[EColumn.MAX_ROLES.idx].defValue, j, EColumn.MAX_ROLES.idx);
		}
	}
	
	
	private void sendRoleFinderInfos()
	{
		Map<EPlay, RoleFinderInfo> infos = new HashMap<EPlay, RoleFinderInfo>();
		Map<EPlay, Boolean> overrides = new HashMap<EPlay, Boolean>();
		for (int row = 0; row < NUM_ROWS; row++)
		{
			String strPlay = table.getModel().getValueAt(row, EColumn.PLAY.idx).toString();
			if (strPlay.isEmpty())
			{
				continue;
			}
			EPlay ePlay = EPlay.valueOf(strPlay);
			int minRoles = valueOrZero(table.getModel().getValueAt(row, EColumn.MIN_ROLES.idx).toString());
			int maxRoles = valueOrZero(table.getModel().getValueAt(row, EColumn.DESIRED_ROLES.idx).toString());
			int desiredRoles = valueOrZero(table.getModel().getValueAt(row, EColumn.MAX_ROLES.idx).toString());
			RoleFinderInfo info = new RoleFinderInfo(minRoles, maxRoles, desiredRoles);
			String desBots = table.getModel().getValueAt(row, EColumn.DESIRED_BOTS.idx).toString();
			if (!desBots.isEmpty())
			{
				String[] desBotsArr = desBots.split("[,; ]");
				for (String strDesBot : desBotsArr)
				{
					info.getDesiredBots().add(BotID.createBotId(valueOrZero(strDesBot), teamColor));
				}
			}
			Boolean override = Boolean.valueOf(table.getModel().getValueAt(row, EColumn.OVERRIDE.idx).toString());
			infos.put(ePlay, info);
			overrides.put(ePlay, override);
		}
		for (IAthenaControlPanelObserver o : observers)
		{
			o.onNewRoleFinderInfos(infos);
			o.onNewRoleFinderOverrides(overrides);
		}
	}
	
	
	@Override
	public void onAiModeChanged(final EAIControlState mode)
	{
		switch (mode)
		{
			case EMERGENCY_MODE:
			case MATCH_MODE:
			case MIXED_TEAM_MODE:
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
				// ignore
			}
		}
	}
	
	
	@Override
	public void onNewAIInfoFrame(final IRecordFrame lastAIInfoframe)
	{
		teamColor = lastAIInfoframe.getTeamColor();
		
		if (table.isEnabled())
		{
			for (int row = 0; row < NUM_ROWS; row++)
			{
				String strPlay = table.getModel().getValueAt(row, EColumn.PLAY.idx).toString();
				if (strPlay.isEmpty())
				{
					continue;
				}
				EPlay ePlay = EPlay.valueOf(strPlay);
				int numRoles = lastAIInfoframe.getPlayStrategy().getActiveRoles(ePlay).size();
				table.getModel().setValueAt(String.valueOf(numRoles), row, EColumn.NUM_ROLES.idx);
			}
			return;
		}
		
		int i = 0;
		Map<EPlay, RoleFinderInfo> newRoleFinderInfo = lastAIInfoframe.getTacticalField().getRoleFinderInfos();
		List<Map.Entry<EPlay, RoleFinderInfo>> infoEntries = new ArrayList<Map.Entry<EPlay, RoleFinderInfo>>(
				newRoleFinderInfo.entrySet());
		Collections
				.sort(infoEntries, new PlayPrioComparatorInfo(lastAIInfoframe.getTacticalField().getGameBehavior()));
		for (Map.Entry<EPlay, RoleFinderInfo> entry : infoEntries)
		{
			EPlay ePlay = entry.getKey();
			RoleFinderInfo info = entry.getValue();
			table.getModel().setValueAt(ePlay.name(), i, EColumn.PLAY.idx);
			table.getModel().setValueAt(String.valueOf(info.getMinRoles()), i, EColumn.MIN_ROLES.idx);
			table.getModel().setValueAt(String.valueOf(info.getDesiredRoles()), i, EColumn.DESIRED_ROLES.idx);
			table.getModel().setValueAt(String.valueOf(info.getMaxRoles()), i, EColumn.MAX_ROLES.idx);
			int numRoles = (int) lastAIInfoframe.getTacticalField().getBotAiInformation().values().stream()
					.filter(aiInfo -> aiInfo.getPlay().equals(ePlay.name())).count();
			table.getModel().setValueAt(String.valueOf(numRoles), i, EColumn.NUM_ROLES.idx);
			
			String desBots = botIds2String(info.getDesiredBots());
			table.getModel().setValueAt(desBots, i, EColumn.DESIRED_BOTS.idx);
			
			List<BotID> assignedIds = lastAIInfoframe.getTacticalField().getBotAiInformation().entrySet().stream()
					.filter(e -> e.getValue().getPlay().equals(ePlay.name())).map(e -> e.getKey())
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
	
	
	@Override
	public void onAIException(final Throwable ex, final IRecordFrame frame, final IRecordFrame prevFrame)
	{
	}
	
	
	private class ChangeDesiredRolesListener implements ActionListener
	{
		private final int	inc;
		
		
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
				}
				String strVal = String.valueOf(val);
				table.getModel().setValueAt(strVal, selRowIdx, EColumn.DESIRED_ROLES.idx);
			}
			sendRoleFinderInfos();
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
	}
	
	private static class TableModel extends DefaultTableModel
	{
		/**  */
		private static final long	serialVersionUID	= -3700170198897729348L;
		
		
		private TableModel(final Object[] columnNames, final int rowCount)
		{
			super(columnNames, rowCount);
		}
		
		
		@Override
		public Class<?> getColumnClass(final int columnIndex)
		{
			if (columnIndex == EColumn.OVERRIDE.idx)
			{
				return Boolean.class;
			}
			return super.getColumnClass(columnIndex);
		}
		
		
		@Override
		public boolean isCellEditable(final int row, final int column)
		{
			if (column == EColumn.NUM_ROLES.idx)
			{
				return false;
			}
			return super.isCellEditable(row, column);
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
