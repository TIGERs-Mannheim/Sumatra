/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.aicenter.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

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

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * Control Plays and Roles
 */
public class AthenaControlPanel extends JPanel
{
	private static final Logger log = Logger.getLogger(AthenaControlPanel.class.getName());
	private static final long serialVersionUID = 8561402774656016979L;
	public static final int NUM_ROWS = 7;

	private final JTable table;
	private final JPanel buttonPanel = new JPanel();
	private final JButton btnClearPlays = new JButton("Clear Plays");
	private final JButton btnClearRoles = new JButton("Clear Roles");
	private final JButton btnAddAllToSelected = new JButton("Add all to Selected");


	public AthenaControlPanel()
	{
		setLayout(new BorderLayout());

		TableModel model = new TableModel(getColumHeaders(), NUM_ROWS);
		table = new JTable(model);
		table.setEnabled(false);
		clear();

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

		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		add(buttonPanel, BorderLayout.NORTH);

		buttonPanel.add(btnClearPlays);
		buttonPanel.add(btnClearRoles);
		buttonPanel.add(btnAddAllToSelected);
	}


	public JTable getTable()
	{
		return table;
	}


	public JButton getBtnClearPlays()
	{
		return btnClearPlays;
	}


	public JButton getBtnClearRoles()
	{
		return btnClearRoles;
	}
	
	
	public JButton getBtnAddAllToSelected()
	{
		return btnAddAllToSelected;
	}


	private String[] getColumHeaders()
	{
		String[] header = new String[EColumn.values().length];
		int i = 0;
		for (EColumn col : EColumn.values())
		{
			header[i] = col.getTitle();
			i++;
		}
		return header;
	}


	public void clear()
	{
		for (int j = 0; j < NUM_ROWS; j++)
		{
			clearRow(j);
		}
	}


	public void clearRow(final int rowId)
	{
		for (int colIdx = 0; colIdx < (EColumn.values().length); colIdx++)
		{
			table.getModel().setValueAt(EColumn.values()[colIdx].defValue, rowId, colIdx);
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


	public enum EColumn
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


		public int getIdx()
		{
			return idx;
		}


		public String getTitle()
		{
			return title;
		}


		public Object getDefValue()
		{
			return defValue;
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
		for (Component comp : buttonPanel.getComponents())
		{
			comp.setEnabled(enabled);
		}
	}
}
