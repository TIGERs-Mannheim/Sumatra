/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.tigers.sumatra.aicenter.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import com.github.g3force.instanceables.IInstanceableObserver;
import com.github.g3force.instanceables.InstanceablePanel;

import edu.tigers.sumatra.ai.athena.IAIModeChanged;
import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.model.SumatraModel;
import net.miginfocom.swing.MigLayout;


/**
 * This panel can be used to control the actual plays.
 * This must be a child panel of {@link ModuleControlPanel}.
 * 
 * @author Oliver,Malte
 */
public class PlayControlPanel extends JPanel implements IAIModeChanged
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long								serialVersionUID		= -2647962360775299686L;
	
	private JList<APlay>										activePlaysList		= null;
	private DefaultListModel<APlay>						activePlaysListModel	= null;
	
	private JButton											deletePlayButton		= null;
	private JButton											clearPlaysButton		= null;
	private JButton											addRoleButton			= null;
	private JButton											removeRoleButton		= null;
	private InstanceablePanel								instanceablePanel		= null;
	private JTextField										freeBots					= null;
	
	private final List<JComponent>						components				= new ArrayList<JComponent>();
	
	private static final int								PLAYS_LIST_SIZE_X		= 250;
	private static final int								PLAYS_LIST_SIZE_Y		= 100;
	
	private final List<IPlayControlPanelObserver>	observers				= new LinkedList<IPlayControlPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public PlayControlPanel()
	{
		setLayout(new MigLayout("fill, insets 0", "[left][left]", "[top]"));
		
		activePlaysListModel = new DefaultListModel<APlay>();
		activePlaysList = new JList<APlay>(activePlaysListModel);
		// activePlaysList.setCellRenderer(new PlayListRenderer());
		activePlaysList.setPreferredSize(new Dimension(PLAYS_LIST_SIZE_X, PLAYS_LIST_SIZE_Y));
		activePlaysList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final JScrollPane scrollPaneActivePlays = new JScrollPane();
		scrollPaneActivePlays.getViewport().setView(activePlaysList);
		
		instanceablePanel = new InstanceablePanel(EPlay.values(), SumatraModel.getInstance().getUserSettings());
		instanceablePanel.addObserver(new AddSelectedPlayListener());
		
		addRoleButton = new JButton("Add role");
		addRoleButton.addActionListener(new AddRoleToPlayListener());
		addRoleButton.setEnabled(false);
		
		removeRoleButton = new JButton("Remove role");
		removeRoleButton.addActionListener(new RemoveRoleFromPlayListener());
		removeRoleButton.setEnabled(false);
		
		deletePlayButton = new JButton("Delete play");
		deletePlayButton.addActionListener(new DeleteSelectedPlayListener());
		deletePlayButton.setEnabled(false);
		
		clearPlaysButton = new JButton("clear");
		clearPlaysButton.addActionListener(new ClearPlaysListener());
		clearPlaysButton.setEnabled(false);
		
		freeBots = new JTextField();
		freeBots.setEditable(false);
		freeBots.setBackground(Color.WHITE);
		final JPanel freeBotsPanel = new JPanel(new MigLayout("fill", "[]10[40,fill]"));
		freeBotsPanel.add(new JLabel("Bots without Play:"));
		freeBotsPanel.add(freeBots);
		
		final JPanel controlPanel = new JPanel(new MigLayout("fill, insets 0"));
		controlPanel.add(instanceablePanel, "wrap, span 2, growx");
		controlPanel.add(addRoleButton, "growx");
		controlPanel.add(removeRoleButton, "growx, wrap");
		controlPanel.add(deletePlayButton, "growx");
		controlPanel.add(clearPlaysButton, "growx, wrap");
		controlPanel.add(freeBotsPanel, "wrap, span 2");
		
		add(scrollPaneActivePlays);
		add(controlPanel);
		
		components.add(activePlaysList);
		components.add(deletePlayButton);
		components.add(clearPlaysButton);
		components.add(addRoleButton);
		components.add(removeRoleButton);
		components.add(instanceablePanel);
		components.add(freeBots);
		components.add(this);
	}
	
	
	@Override
	public void onAiModeChanged(final EAIControlState mode)
	{
		clearPlays();
		switch (mode)
		{
			case EMERGENCY_MODE:
			case MATCH_MODE:
			case MIXED_TEAM_MODE:
				for (JComponent comp : components)
				{
					comp.setEnabled(false);
				}
				activePlaysListModel.clear();
				break;
			case TEST_MODE:
				for (JComponent comp : components)
				{
					comp.setEnabled(true);
				}
				
				String selectedPlayStr = SumatraModel.getInstance().getUserProperty(
						PlayControlPanel.class.getCanonicalName() + ".selectedPlay");
				try
				{
					// selectedPlayStr may be null, so convert
					EPlay selectedPlay = EPlay.valueOf(String.valueOf(selectedPlayStr));
					instanceablePanel.setSelectedItem(selectedPlay);
				} catch (IllegalArgumentException e)
				{
					// Could not read selected play from user properties: do not care, its not important.
					// maybe the user just starts Sumatra the first time
				}
				
				break;
			default:
				break;
		
		}
	}
	
	
	/**
	 * Updates the play panel with the actual active plays. Depending on the active plays
	 * in last frame this function will add or remove plays from the list.
	 * 
	 * @param plays the actual play in aiFrame
	 */
	public void setActivePlays(final List<APlay> plays)
	{
		// Insert
		int i = 0;
		while ((activePlaysListModel.size() > i) && (plays.size() > i))
		{
			final APlay play = plays.get(i);
			if (activePlaysListModel.get(i) != play)
			{
				activePlaysListModel.insertElementAt(play, i);
			}
			i++;
		}
		
		// Cut off
		final int modelSize = activePlaysListModel.size();
		final int diff = modelSize - plays.size();
		if (diff > 0)
		{
			activePlaysListModel.removeRange(modelSize - diff, modelSize - 1);
		} else if (diff < 0)
		{
			for (int j = i; j < plays.size(); j++)
			{
				activePlaysListModel.addElement(plays.get(j));
			}
		}
		
		if (activePlaysListModel.size() == 1)
		{
			activePlaysList.setSelectedIndex(activePlaysListModel.size() - 1);
		}
	}
	
	
	/**
	 * Called by AICenterPresenter on every NewAIInfoFrame!
	 * Sets the Textfield "Number Of Bots"
	 * 
	 * @param numberOfBotsWithoutRole
	 */
	public void setBotsWithoutRole(final int numberOfBotsWithoutRole)
	{
		String str = Integer.toString(numberOfBotsWithoutRole);
		if (!str.equals(freeBots.getText()))
		{
			freeBots.setText(str);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IPlayControlPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param oddObserver
	 */
	public void removeObserver(final IPlayControlPanelObserver oddObserver)
	{
		synchronized (observers)
		{
			observers.remove(oddObserver);
		}
	}
	
	/**
	 */
	public class DeleteSelectedPlayListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			final ListSelectionModel lsm = activePlaysList.getSelectionModel();
			// If no play is selected, always select the first one of the list.
			int firstSelected = lsm.getMinSelectionIndex() == -1 ? 0 : lsm.getMinSelectionIndex();
			final int lastSelected = lsm.getMaxSelectionIndex() == -1 ? firstSelected : lsm.getMaxSelectionIndex();
			
			
			if (activePlaysListModel.size() != 0)
			{
				for (int i = firstSelected; i <= lastSelected; i++)
				{
					for (final IPlayControlPanelObserver o : observers)
					{
						o.removePlay(activePlaysListModel.elementAt(i));
					}
				}
				
				// Adjust the selection.
				if (firstSelected == activePlaysListModel.getSize())
				{
					// Removed item in last position.
					firstSelected--;
				}
				activePlaysList.setSelectedIndex(firstSelected);
			}
		}
	}
	
	
	private class AddSelectedPlayListener implements IInstanceableObserver
	{
		@Override
		public void onNewInstance(final Object object)
		{
			APlay play = (APlay) object;
			for (final IPlayControlPanelObserver o : observers)
			{
				o.addPlay(play);
			}
			
			SumatraModel.getInstance().setUserProperty(PlayControlPanel.class.getCanonicalName() + ".selectedPlay",
					play.getType().name());
		}
	}
	
	/**
	 */
	public class ClearPlaysListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			clearPlays();
		}
	}
	
	
	private void clearPlays()
	{
		for (int i = 0; i < activePlaysListModel.size(); i++)
		{
			APlay play = activePlaysListModel.elementAt(i);
			for (final IPlayControlPanelObserver o : observers)
			{
				o.removePlay(play);
			}
		}
	}
	
	private class AddRoleToPlayListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			APlay play = activePlaysList.getSelectedValue();
			if (play != null)
			{
				for (final IPlayControlPanelObserver o : observers)
				{
					o.addRoles2Play(play, 1);
				}
			}
		}
	}
	
	private class RemoveRoleFromPlayListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			APlay play = activePlaysList.getSelectedValue();
			if (play != null)
			{
				for (final IPlayControlPanelObserver o : observers)
				{
					o.removeRolesFromPlay(play, 1);
				}
			}
		}
	}
}
