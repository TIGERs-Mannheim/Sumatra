/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.aicenter.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ai.athena.roleassigner.RoleMapping;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
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
	private EAiTeam team;
	
	private final List<Component> components = new ArrayList<>();
	
	
	/**
	 * Default
	 *
	 * @param team
	 */
	public AthenaControlPanel(final EAiTeam team)
	{
		setLayout(new BorderLayout());
		this.team = team;
		
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
		table.getColumnModel().getColumn(EColumn.DESIRED_BOTS.idx).setCellRenderer(textRenderer);
		
		JScrollPane scrlPane = new JScrollPane(table);
		scrlPane.setPreferredSize(new Dimension(0, 0));
		add(scrlPane, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		add(buttonPanel, BorderLayout.NORTH);
		
		JButton btnClearPlays = new JButton("Clear Plays");
		btnClearPlays.addActionListener(new ClearListener());
		buttonPanel.add(btnClearPlays);
		components.add(btnClearPlays);
		
		JButton btnClearRoles = new JButton("Clear Roles");
		btnClearRoles.addActionListener(new ClearRolesListener());
		buttonPanel.add(btnClearRoles);
		components.add(btnClearRoles);
	}
	
	
	private void sendRoleFinderInfos()
	{
		Map<EPlay, RoleMapping> infos = new EnumMap<>(EPlay.class);
		Map<EPlay, Boolean> useAiPlays = new EnumMap<>(EPlay.class);
		for (int row = 0; row < NUM_ROWS; row++)
		{
			String strPlay = table.getModel().getValueAt(row, EColumn.PLAY.idx).toString();
			if (strPlay.isEmpty())
			{
				continue;
			}
			EPlay ePlay = EPlay.valueOf(strPlay);
			RoleMapping info = new RoleMapping();
			String desBots = table.getModel().getValueAt(row, EColumn.DESIRED_BOTS.idx).toString();
			if (!desBots.isEmpty())
			{
				String[] desBotsArr = desBots.split(REPLACE_PATTERN);
				for (String strDesBot : desBotsArr)
				{
					info.getDesiredBots().add(BotID.createBotId(valueOrZero(strDesBot), team.getTeamColor()));
				}
			}
			Boolean override = Boolean.valueOf(table.getModel().getValueAt(row, EColumn.AI.idx).toString());
			infos.put(ePlay, info);
			useAiPlays.put(ePlay, override);
		}
		for (IAthenaControlPanelObserver o : observers)
		{
			o.onNewRoleMapping(infos);
			o.onNewUseAiFlags(useAiPlays);
		}
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
	
	
	public void setAiControlState(final EAIControlState mode)
	{
		switch (mode)
		{
			case EMERGENCY_MODE:
			case MATCH_MODE:
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
	
	
	private enum EColumn
	{
		PLAY(0, "Play", ""),
		DESIRED_BOTS(1, "desiredBots", ""),
		AI(2, "use AI", true),;
		
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
