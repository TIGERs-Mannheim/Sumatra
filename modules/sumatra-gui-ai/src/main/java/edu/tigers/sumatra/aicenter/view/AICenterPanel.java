/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.aicenter.view;


import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import edu.tigers.sumatra.ai.athena.IAIModeChanged;
import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;


/**
 * This panel controls the ai subtract-modules.
 * 
 * @author Oliver, Malte
 */
public class AICenterPanel extends JPanel implements ISumatraView
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long serialVersionUID = -2509991904665753934L;
	
	
	private final JTabbedPane teamTabbedPane = new JTabbedPane();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Default
	 */
	public AICenterPanel()
	{
		setLayout(new BorderLayout());
		
		for (EAiTeam team : EAiTeam.values())
		{
			// construct team buttons from enum
			final TeamPanel teamPanel = new TeamPanel(team);
			
			teamTabbedPane.addTab(team.name(), teamPanel);
		}
		
		add(teamTabbedPane, BorderLayout.CENTER);
		
		setActive(false);
	}
	
	
	/**
	 * @param observer
	 * @param team
	 */
	public void addObserverForAi(final IAIModeChanged observer, EAiTeam team)
	{
		synchronized (getTeamPanelForTeam(team).observers)
		{
			getTeamPanelForTeam(team).observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IAIModeChanged observer)
	{
		synchronized (getActiveTeamPanel().observers)
		{
			getActiveTeamPanel().observers.remove(observer);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param team
	 * @return
	 */
	public RoleControlPanel getRolePanelForAi(EAiTeam team)
	{
		return getTeamPanelForTeam(team).rolePanel;
	}
	
	
	/**
	 * @param team
	 * @return
	 */
	public AthenaControlPanel getAthenaPanelForAi(EAiTeam team)
	{
		return getTeamPanelForTeam(team).athenaPanel;
	}
	
	
	/**
	 * @param team
	 * @return
	 */
	public MetisCalculatorsPanel getMetisCalculatorsPanelForAi(EAiTeam team)
	{
		return getTeamPanelForTeam(team).metisCalculatorsPanel;
	}
	
	
	private TeamPanel getActiveTeamPanel()
	{
		return (TeamPanel) teamTabbedPane.getSelectedComponent();
	}
	
	
	private TeamPanel getTeamPanelForTeam(EAiTeam team)
	{
		return (TeamPanel) teamTabbedPane.getComponentAt(team.ordinal());
	}
	
	
	/**
	 * @param mode
	 * @param team
	 */
	public void setAiControlStateForAi(final EAIControlState mode, final EAiTeam team)
	{
		final TeamPanel updatedPanel = getTeamPanelForTeam(team);
		
		EventQueue.invokeLater(() -> {
			updatedPanel.modeButtons.get(mode).setSelected(true);
			
			if (mode == EAIControlState.MATCH_MODE)
			{
				updatedPanel.tabbedPane.setSelectedComponent(updatedPanel.athenaPanel);
			}
		});
		
		updatedPanel.rolePanel.setAiControlState(mode);
		updatedPanel.athenaPanel.setAiControlState(mode);
	}
	
	
	public void setActive(final boolean enable)
	{
		EventQueue.invokeLater(() -> {
			for (EAiTeam team : EAiTeam.values())
			{
				TeamPanel teamPanel = getTeamPanelForTeam(team);
				
				teamPanel.modeButtons.values().forEach(b -> b.setEnabled(enable));
				
				teamPanel.rolePanel.setEnabled(enable);
				teamPanel.athenaPanel.setEnabled(enable);
				teamPanel.metisCalculatorsPanel.setEnabled(enable);
			}
			
			setEnabled(true);
		});
	}
	
	private class AiModeChangeListener implements ActionListener
	{
		private final EAIControlState newState;
		
		
		/**
		 * @param newState
		 */
		public AiModeChangeListener(final EAIControlState newState)
		{
			this.newState = newState;
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			for (IAIModeChanged o : getActiveTeamPanel().observers)
			{
				o.onAiModeChanged(newState);
			}
		}
	}
	
	private class TeamPanel extends JPanel
	{
		private final EAiTeam currentTeam;
		
		// construct mode buttons from enum
		private final JPanel modePanel = new JPanel(new MigLayout("insets 0 0 0 0", "", ""));
		private final Map<EAIControlState, JRadioButton> modeButtons = new EnumMap<>(EAIControlState.class);
		private final ButtonGroup modeGroup = new ButtonGroup();
		
		private RoleControlPanel rolePanel = new RoleControlPanel();
		private MetisCalculatorsPanel metisCalculatorsPanel = new MetisCalculatorsPanel();
		private AthenaControlPanel athenaPanel = new AthenaControlPanel();
		
		private final JTabbedPane tabbedPane = new JTabbedPane();
		
		private final transient List<IAIModeChanged> observers = new CopyOnWriteArrayList<>();
		
		
		private TeamPanel(EAiTeam team)
		{
			super(new MigLayout("wrap 1"));
			
			currentTeam = team;
			
			for (EAIControlState state : EAIControlState.values())
			{
				JRadioButton btn = new JRadioButton(state.toString());
				btn.addActionListener(new AiModeChangeListener(state));
				modePanel.add(btn);
				modeGroup.add(btn);
				modeButtons.put(state, btn);
			}
			
			final ButtonModel btnModelMode = new DefaultButtonModel();
			btnModelMode.setGroup(modeGroup);
			
			modeGroup.clearSelection();
			
			if (currentTeam.isActiveByDefault())
			{
				modeButtons.get(EAIControlState.EMERGENCY_MODE).setSelected(true);
			} else
			{
				modeButtons.get(EAIControlState.OFF).setSelected(true);
			}
			
			this.add(modePanel);
			
			this.add(rolePanel);
			this.add(metisCalculatorsPanel);
			this.add(athenaPanel);
			
			tabbedPane.addTab("Athena", athenaPanel);
			tabbedPane.addTab("Roles", rolePanel);
			tabbedPane.addTab("Metis Calcs", metisCalculatorsPanel);
			
			this.add(tabbedPane, "push, grow");
		}
	}
}
