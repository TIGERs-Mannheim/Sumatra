/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.ai.aicenter.view;

import com.github.g3force.instanceables.InstanceablePanel;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.util.ScalingUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Control Plays and Roles
 */
@Getter
public class AthenaControlPanel extends JPanel
{
	private static final long serialVersionUID = 8561402774656016979L;
	public static final int NUM_ROWS = 10;

	private final JTable table;
	private final JList<Integer> botIdList;
	private final InstanceablePanel instanceablePanel;

	private final JPanel buttonPanel = new JPanel();
	private final JButton btnAssign = new JButton("Assign");
	private final JButton btnUnassign = new JButton("Unassign");
	private final JButton btnClearRoles = new JButton("Clear");

	private final JButton btnRemovePlay = new JButton("Remove");
	private final JButton btnClearPlays = new JButton("Clear");


	public AthenaControlPanel()
	{
		setLayout(new MigLayout("filly", "", "[top]"));

		TableModel model = new TableModel(getColumnHeaders(), NUM_ROWS);
		table = new JTable(model);
		table.setEnabled(false);
		table.setRowHeight(ScalingUtil.getTableRowHeight());
		table.changeSelection(0, 0, false, false);

		var enums = EPlay.values();
		Arrays.sort(enums, (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.name(), o2.name()));
		instanceablePanel = new InstanceablePanel(enums, SumatraModel.getInstance().getUserSettings());
		instanceablePanel.setShowCreate(true);

		DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer();
		textRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(EColumn.BOTS.ordinal()).setCellRenderer(textRenderer);

		botIdList = new JList<>(IntStream.range(0, AObjectID.BOT_ID_MAX + 1).boxed().toArray(Integer[]::new));
		botIdList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		JScrollPane tableScrollPane = new JScrollPane(table);
		tableScrollPane.setPreferredSize(new Dimension(400, 0));

		JScrollPane botIdListScrollPane = new JScrollPane(botIdList);
		botIdListScrollPane.setPreferredSize(new Dimension(100, 0));

		JPanel controlPanel = new JPanel(new MigLayout());
		controlPanel.add(instanceablePanel, "wrap");
		controlPanel.add(buttonPanel, "wrap");

		add(tableScrollPane, "growy");
		add(botIdListScrollPane, "growy");
		add(controlPanel);

		buttonPanel.setLayout(new MigLayout());
		buttonPanel.add(new JLabel("Plays:"), "wrap");
		buttonPanel.add(btnRemovePlay);
		buttonPanel.add(btnClearPlays, "wrap");

		buttonPanel.add(new JLabel("Roles:"), "wrap");
		buttonPanel.add(btnAssign);
		buttonPanel.add(btnUnassign);
		buttonPanel.add(btnClearRoles);
	}


	private String[] getColumnHeaders()
	{
		return Arrays.stream(EColumn.values())
				.map(EColumn::getTitle)
				.collect(Collectors.toList())
				.toArray(new String[] {});
	}


	public void setAiControlState(final EAIControlState mode)
	{
		setEnabled(mode == EAIControlState.TEST_MODE);
	}


	@Override
	public void setEnabled(final boolean enabled)
	{
		super.setEnabled(enabled);
		table.setEnabled(enabled);
		instanceablePanel.setEnabled(enabled);
		botIdList.setEnabled(enabled);
		for (Component comp : buttonPanel.getComponents())
		{
			comp.setEnabled(enabled);
		}
	}


	@Getter
	@AllArgsConstructor
	public enum EColumn
	{
		PLAY("Play", String.class, ""),
		BOTS("Bots", String.class, ""),
		;

		private final String title;
		private final Class<?> columnClass;
		private final Object defValue;
	}

	private static class TableModel extends DefaultTableModel
	{
		private static final long serialVersionUID = -3700170198897729348L;


		private TableModel(final Object[] columnNames, final int rowCount)
		{
			super(columnNames, rowCount);
		}


		@Override
		public Class<?> getColumnClass(final int columnIndex)
		{
			return EColumn.values()[columnIndex].columnClass;
		}


		@Override
		public boolean isCellEditable(final int row, final int column)
		{
			return false;
		}
	}
}
