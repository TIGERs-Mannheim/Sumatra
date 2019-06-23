/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;


/**
 * This panel controls the ai sub-modules.
 * 
 * @author Oliver, Malte
 * 
 */
public class ModuleControlPanel extends JPanel implements IChangeGUIMode
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long								serialVersionUID	= -2509991904665753934L;
	private static final String							TITLE					= "Sub-Module Controller";
	
	private final JRadioButton								matchControl;
	private final JRadioButton								mixedTeamControl;
	private final JRadioButton								athenaControl;
	private final JRadioButton								lachesisControl;
	private final JRadioButton								emergencyControl;
	
	private final JTabbedPane								tabbedPane;
	private final PlayControlPanel						playPanel;
	private final RoleControlPanel						rolePanel;
	private final TacticalFieldControlPanel			tacticalFieldPanel;
	private final MetisCalculatorsPanel					metisCalculatorsPanel;
	private final ApollonControlPanel					learningPanel;
	private final ButtonGroup								modeGroup;
	
	private final List<IModuleControlPanelObserver>	observers			= new LinkedList<IModuleControlPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public ModuleControlPanel()
	{
		setLayout(new MigLayout("fill"));
		setBorder(BorderFactory.createTitledBorder(TITLE));
		
		
		final JPanel controlPanel = new JPanel(new MigLayout("fill", ""));
		matchControl = new JRadioButton("Match Mode");
		matchControl.addActionListener(new MatchModeControlListener());
		controlPanel.add(matchControl);
		
		mixedTeamControl = new JRadioButton("Mixed Team Mode");
		mixedTeamControl.addActionListener(new MixedTeamControlListener());
		controlPanel.add(mixedTeamControl);
		
		athenaControl = new JRadioButton("Play Test Mode");
		athenaControl.addActionListener(new AthenaControlListener());
		controlPanel.add(athenaControl);
		
		lachesisControl = new JRadioButton("Role Test Mode");
		lachesisControl.addActionListener(new LachesisControlListener());
		controlPanel.add(lachesisControl);
		
		emergencyControl = new JRadioButton("Emergency Mode");
		emergencyControl.addActionListener(new EmergencyControlListener());
		controlPanel.add(emergencyControl);
		
		modeGroup = new ButtonGroup();
		modeGroup.add(matchControl);
		modeGroup.add(mixedTeamControl);
		modeGroup.add(lachesisControl);
		modeGroup.add(athenaControl);
		modeGroup.add(emergencyControl);
		final ButtonModel btnModel = new DefaultButtonModel();
		btnModel.setGroup(modeGroup);
		// modeGroup.setSelected(btnModel, true);
		athenaControl.setSelected(true);
		
		
		final JPanel moduleStates = new JPanel(new MigLayout("fill"));
		moduleStates.add(controlPanel);
		
		playPanel = new PlayControlPanel();
		rolePanel = new RoleControlPanel();
		tacticalFieldPanel = new TacticalFieldControlPanel();
		metisCalculatorsPanel = new MetisCalculatorsPanel();
		learningPanel = new ApollonControlPanel();
		
		final JPanel lachesisPanel = new JPanel(new MigLayout("fill"));
		lachesisPanel.add(rolePanel);
		
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Athena", playPanel);
		tabbedPane.addTab("Lachesis", rolePanel);
		tabbedPane.addTab("Metis", tacticalFieldPanel);
		tabbedPane.addTab("Metis Calcs", metisCalculatorsPanel);
		tabbedPane.addTab("Apollon", learningPanel);
		
		
		add(moduleStates, "wrap");
		add(tabbedPane);
		
		
		// Initialize
		onStop();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(IModuleControlPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param oddObserver
	 */
	public void removeObserver(IModuleControlPanelObserver oddObserver)
	{
		synchronized (observers)
		{
			observers.remove(oddObserver);
		}
	}
	
	
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
	
	
	/**
	 * @return
	 */
	public TacticalFieldControlPanel getTacticalFieldControlPanel()
	{
		return tacticalFieldPanel;
	}
	
	
	/**
	 * @return
	 */
	public ApollonControlPanel getApollonControlPanel()
	{
		return learningPanel;
	}
	
	
	@Override
	public void setPlayTestMode()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				tabbedPane.setSelectedComponent(playPanel);
				
				playPanel.setPlayTestMode();
				rolePanel.setPlayTestMode();
			}
		});
	}
	
	
	@Override
	public void setRoleTestMode()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				tabbedPane.setSelectedComponent(rolePanel);
				
				playPanel.setRoleTestMode();
				rolePanel.setRoleTestMode();
			}
		});
	}
	
	
	@Override
	public void setMatchMode()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				tabbedPane.setSelectedComponent(playPanel);
				
				playPanel.setMatchMode();
				rolePanel.setMatchMode();
			}
		});
	}
	
	
	@Override
	public void setEmergencyMode()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				tabbedPane.setSelectedComponent(playPanel);
				
				emergencyControl.setSelected(true);
				
				playPanel.setEmergencyMode();
				rolePanel.setEmergencyMode();
			}
		});
	}
	
	
	@Override
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
				lachesisControl.setEnabled(true);
				emergencyControl.setEnabled(true);
				
				playPanel.onStop();
				rolePanel.onStop();
				
				setPlayTestMode();
			}
		});
	}
	
	
	@Override
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
				lachesisControl.setEnabled(false);
				emergencyControl.setEnabled(false);
				
				playPanel.onStop();
				rolePanel.onStop();
			}
		});
	}
	
	
	private class AthenaControlListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			synchronized (observers)
			{
				for (final IModuleControlPanelObserver o : observers)
				{
					o.onPlayTestMode();
				}
			}
		}
	}
	
	private class MatchModeControlListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			synchronized (observers)
			{
				for (final IModuleControlPanelObserver o : observers)
				{
					o.onMatchMode();
				}
			}
		}
	}
	
	private class MixedTeamControlListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			synchronized (observers)
			{
				for (final IModuleControlPanelObserver o : observers)
				{
					o.onMixedTeamMode();
				}
			}
		}
	}
	
	
	private class LachesisControlListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			synchronized (observers)
			{
				for (final IModuleControlPanelObserver o : observers)
				{
					o.onRoleTestMode();
				}
			}
		}
	}
	
	private class EmergencyControlListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			synchronized (observers)
			{
				for (final IModuleControlPanelObserver o : observers)
				{
					o.onEmergencyMode();
				}
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
	
}
