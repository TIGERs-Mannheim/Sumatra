/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.tigers.sumatra.aicenter.view;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import edu.tigers.sumatra.ai.athena.IAIModeChanged;
import edu.tigers.sumatra.ai.data.EAIControlState;
import net.miginfocom.swing.MigLayout;


/**
 * This panel controls the ai sub-modules.
 * 
 * @author Oliver, Malte
 */
public class ModuleControlPanel extends JPanel implements IAIModeChanged
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long				serialVersionUID	= -2509991904665753934L;
	
	private final JCheckBox					chkAiActive;
	
	private final JRadioButton				matchControl;
	private final JRadioButton				mixedTeamControl;
	private final JRadioButton				athenaControl;
	private final JRadioButton				emergencyControl;
	
	private final JTabbedPane				tabbedPane;
	private final PlayControlPanel		playPanel;
	private final RoleControlPanel		rolePanel;
	private final AthenaControlPanel		athenaPanel;
	private final MetisCalculatorsPanel	metisCalculatorsPanel;
	private final ButtonGroup				modeGroup;
	private final SupporterGridPanel		supporterGridPanel;
	
	private final List<IAIModeChanged>	observers			= new CopyOnWriteArrayList<IAIModeChanged>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public ModuleControlPanel()
	{
		setLayout(new BorderLayout());
		
		final JPanel controlPanel = new JPanel(new MigLayout("insets 0 0 0 0", "", ""));
		chkAiActive = new JCheckBox("AI activated", false);
		controlPanel.add(chkAiActive);
		
		matchControl = new JRadioButton("Match");
		matchControl.addActionListener(new MatchModeControlListener());
		controlPanel.add(matchControl);
		
		mixedTeamControl = new JRadioButton("Mixed");
		mixedTeamControl.addActionListener(new MixedTeamControlListener());
		controlPanel.add(mixedTeamControl);
		
		athenaControl = new JRadioButton("Test");
		athenaControl.addActionListener(new AthenaControlListener());
		controlPanel.add(athenaControl);
		
		emergencyControl = new JRadioButton("Emergency");
		emergencyControl.addActionListener(new EmergencyControlListener());
		controlPanel.add(emergencyControl);
		
		modeGroup = new ButtonGroup();
		modeGroup.add(matchControl);
		modeGroup.add(mixedTeamControl);
		modeGroup.add(athenaControl);
		modeGroup.add(emergencyControl);
		final ButtonModel btnModel = new DefaultButtonModel();
		btnModel.setGroup(modeGroup);
		matchControl.setSelected(true);
		
		playPanel = new PlayControlPanel();
		rolePanel = new RoleControlPanel();
		metisCalculatorsPanel = new MetisCalculatorsPanel();
		athenaPanel = new AthenaControlPanel();
		supporterGridPanel = new SupporterGridPanel();
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Athena", athenaPanel);
		tabbedPane.addTab("Roles", rolePanel);
		tabbedPane.addTab("Metis Calcs", metisCalculatorsPanel);
		tabbedPane.addTab("Supporter Grid", supporterGridPanel);
		
		add(controlPanel, BorderLayout.NORTH);
		add(tabbedPane, BorderLayout.CENTER);
		
		
		// Initialize
		onStop();
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IAIModeChanged observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IAIModeChanged observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return
	 */
	public PlayControlPanel getPlayPanel()
	{
		return playPanel;
	}
	
	
	/**
	 * @return
	 */
	public RoleControlPanel getRolePanel()
	{
		return rolePanel;
	}
	
	
	@Override
	public void onAiModeChanged(final EAIControlState mode)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				switch (mode)
				{
					case EMERGENCY_MODE:
						emergencyControl.setSelected(true);
						break;
					case MATCH_MODE:
						matchControl.setSelected(true);
						tabbedPane.setSelectedComponent(athenaPanel);
						break;
					case MIXED_TEAM_MODE:
						mixedTeamControl.setSelected(true);
						break;
					case TEST_MODE:
						athenaControl.setSelected(true);
						break;
					default:
						break;
				
				}
				// playPanel.onAiModeChanged(mode);
				rolePanel.onAiModeChanged(mode);
				athenaPanel.onAiModeChanged(mode);
			}
		});
	}
	
	
	/**
	 */
	public void onStart()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				setEnabled(true);
				
				matchControl.setEnabled(true);
				mixedTeamControl.setEnabled(true);
				athenaControl.setEnabled(true);
				emergencyControl.setEnabled(true);
				playPanel.setEnabled(true);
				rolePanel.setEnabled(true);
				athenaPanel.setEnabled(true);
				metisCalculatorsPanel.setEnabled(true);
				supporterGridPanel.setEnabled(true);
			}
		});
	}
	
	
	/**
	 */
	public void onStop()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				setEnabled(false);
				matchControl.setEnabled(false);
				mixedTeamControl.setEnabled(false);
				athenaControl.setEnabled(false);
				emergencyControl.setEnabled(false);
				playPanel.setEnabled(false);
				rolePanel.setEnabled(false);
				athenaPanel.setEnabled(false);
				metisCalculatorsPanel.setEnabled(false);
				supporterGridPanel.setEnabled(false);
			}
		});
	}
	
	
	private class AthenaControlListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IAIModeChanged o : observers)
			{
				o.onAiModeChanged(EAIControlState.TEST_MODE);
			}
		}
	}
	
	private class MatchModeControlListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IAIModeChanged o : observers)
			{
				o.onAiModeChanged(EAIControlState.MATCH_MODE);
			}
		}
	}
	
	private class MixedTeamControlListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IAIModeChanged o : observers)
			{
				o.onAiModeChanged(EAIControlState.MIXED_TEAM_MODE);
			}
		}
	}
	
	
	private class EmergencyControlListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			for (IAIModeChanged o : observers)
			{
				o.onAiModeChanged(EAIControlState.EMERGENCY_MODE);
			}
		}
	}
	
	
	/**
	 * @return the metisCalculatorsPanel
	 */
	public final MetisCalculatorsPanel getMetisCalculatorsPanel()
	{
		return metisCalculatorsPanel;
	}
	
	
	/**
	 * @return the athenaPanel
	 */
	public final AthenaControlPanel getAthenaPanel()
	{
		return athenaPanel;
	}
	
	
	/**
	 * @return the chkAiActive
	 */
	public final JCheckBox getChkAiActive()
	{
		return chkAiActive;
	}
	
	
	/**
	 * @return the supporterGridPanel
	 */
	public final SupporterGridPanel getSupporterGridPanel()
	{
		return supporterGridPanel;
	}
	
}
