/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.referee;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JPanel;

import edu.tigers.sumatra.components.BetterScrollPane;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.util.MigLayoutResizeListener;
import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;


/**
 * Referee view.
 */
public class RefereePanel extends JPanel implements ISumatraView
{
	private static final long serialVersionUID = 5362158568331526086L;

	private final ControlGameControllerPanel controlGameControllerPanel = new ControlGameControllerPanel();
	private final ShowRefereeMsgPanel showRefereeMsgPanel = new ShowRefereeMsgPanel();
	private final CommonCommandsPanel commonCommandsPanel = new CommonCommandsPanel();
	private final ChangeStatePanel changeStatePanel = new ChangeStatePanel();
	private final Map<ETeamColor, TeamPanel> teamsPanel = new EnumMap<>(ETeamColor.class);


	public RefereePanel()
	{
		setLayout(new BorderLayout());

		JPanel componentPanel = new JPanel();
		componentPanel.setLayout(new MigLayout("wrap 2", "[fill]10[fill]", ""));
		new MigLayoutResizeListener(this, componentPanel, 2);

		teamsPanel.put(ETeamColor.YELLOW, new TeamPanel(ETeamColor.YELLOW));
		teamsPanel.put(ETeamColor.BLUE, new TeamPanel(ETeamColor.BLUE));

		componentPanel.add(showRefereeMsgPanel, "spany 3, aligny top");
		componentPanel.add(controlGameControllerPanel);
		componentPanel.add(commonCommandsPanel);
		componentPanel.add(changeStatePanel);
		componentPanel.add(teamsPanel.get(ETeamColor.YELLOW));
		componentPanel.add(teamsPanel.get(ETeamColor.BLUE));

		BetterScrollPane scrollPane = new BetterScrollPane(componentPanel);
		add(scrollPane, BorderLayout.CENTER);

		setEnable(false);
	}


	/**
	 * @return the showRefereeMsgPanel
	 */
	public ShowRefereeMsgPanel getShowRefereeMsgPanel()
	{
		return showRefereeMsgPanel;
	}


	/**
	 * @param enable
	 */
	public void setEnable(final boolean enable)
	{
		controlGameControllerPanel.setEnabled(enable);
		showRefereeMsgPanel.setEnabled(enable);
		commonCommandsPanel.setEnable(enable);
		changeStatePanel.setEnable(enable);
		teamsPanel.values().forEach(t -> t.setEnable(enable));
	}


	@Override
	public List<JMenu> getCustomMenus()
	{
		return Collections.emptyList();
	}


	@Override
	public void onShown()
	{
		// nothing to do
	}


	@Override
	public void onHidden()
	{
		// nothing to do
	}


	@Override
	public void onFocused()
	{
		// nothing to do
	}


	@Override
	public void onFocusLost()
	{
		// nothing to do
	}


	/**
	 * @return the commonCommandsPanel
	 */
	public CommonCommandsPanel getCommonCommandsPanel()
	{
		return commonCommandsPanel;
	}


	/**
	 * @return the changeStatePanel
	 */
	public ChangeStatePanel getChangeStatePanel()
	{
		return changeStatePanel;
	}


	/**
	 * @return the teamsPanel
	 */
	public Map<ETeamColor, TeamPanel> getTeamsPanel()
	{
		return teamsPanel;
	}
}
