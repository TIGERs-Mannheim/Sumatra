/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.PlayScore;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;


/**
 * This panel can be used to control the actual plays.
 * This must be a child panel of {@link ModuleControlPanel}.
 * 
 * @author Oliver,Malte
 * 
 */
public class PlayControlPanel extends JPanel implements IChangeGUIMode
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long						serialVersionUID			= -2647962360775299686L;
	

	private JList										activePlaysList			= null;
	private DefaultListModel						activePlaysListModel		= null;
	
	private JTable										tableBestPlays				= null;
	
	private JPanel										activePlaysPanel;
	private JPanel										bestPlaysPanel;
	private JTabbedPane								tabbedPane;
	

	private JButton									forceNewDecisionButton	= null;
	
	private JButton									deletePlayButton			= null;
	private JButton									addPlayButton				= null;
	
	private JComboBox									selectPlayBox				= null;
	
	private JTextField								freeBots						= null;
	private static final int						INIT_FREE_BOTS				= -1;
	private int											lastFreeBots				= INIT_FREE_BOTS;
	
	private List<IPlayControlPanelObserver>	observers					= new LinkedList<IPlayControlPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public PlayControlPanel()
	{
		setLayout(new MigLayout());
		setBorder(BorderFactory.createTitledBorder("Play Control Panel"));
		
		activePlaysPanel = new JPanel();
		activePlaysPanel.setName("Active Plays");
		bestPlaysPanel = new JPanel(new MigLayout());
		bestPlaysPanel.setName("Best Plays");
		tabbedPane = new JTabbedPane();
		tabbedPane.add(activePlaysPanel);
		tabbedPane.add(bestPlaysPanel);
		
		activePlaysListModel = new DefaultListModel();
		// activePlaysListModel.addListDataListener(new PlayListChangeListener());
		activePlaysList = new JList(activePlaysListModel);
		activePlaysList.setCellRenderer(new PlayListRenderer());
		activePlaysList.setVisibleRowCount(5);
		activePlaysList.setPreferredSize(new Dimension(440, 50));
		JScrollPane scrollPaneActivePlays = new JScrollPane();
		scrollPaneActivePlays.getViewport().setView(activePlaysList);
		activePlaysPanel.add(activePlaysList);
		

		tableBestPlays = new JTable(8, 2);
		tableBestPlays.setEnabled(false);
		// table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		tableBestPlays.setPreferredSize(tabbedPane.getPreferredSize());
		tableBestPlays.getColumnModel().getColumn(1).setMaxWidth(30);
		// tableBestPlays.getColumnModel().getColumn(1).setPreferredWidth(20);
		// tableBestPlays.setRowHeight(50);
		// tableBestPlays.setGridColor(Color.blue);
		// tableBestPlays.setForeground(Color.yellow);
		

		bestPlaysPanel.add(tableBestPlays);
		

		forceNewDecisionButton = new JButton("Force new decision");
		forceNewDecisionButton.addActionListener(new ForceNewDecisionListener());
		forceNewDecisionButton.setEnabled(false);
		

		deletePlayButton = new JButton("delete play");
		deletePlayButton.addActionListener(new DeleteSelectedPlayListener());
		deletePlayButton.setEnabled(false);
		

		selectPlayBox = new JComboBox(EPlay.values());
		

		addPlayButton = new JButton("add play");
		addPlayButton.addActionListener(new AddSelectedPlayListener());
		addPlayButton.setEnabled(false);
		
		freeBots = new JTextField();
		freeBots.setEditable(false);
		freeBots.setBackground(Color.WHITE);
		JPanel freeBotsPanel = new JPanel(new MigLayout("fill", "[]10[40,fill]"));
		freeBotsPanel.add(new JLabel("Bots without Play:"));
		freeBotsPanel.add(freeBots);
		
		JPanel controlPanel = new JPanel(new MigLayout("fill"));
		controlPanel.add(forceNewDecisionButton, "wrap");
		controlPanel.add(freeBotsPanel, "wrap");
		controlPanel.add(deletePlayButton, "wrap");
		controlPanel.add(selectPlayBox, "span");
		controlPanel.add(addPlayButton);
		
		add(tabbedPane);
		add(controlPanel);
	}
	

	public void setPlayTestMode()
	{
		this.setEnabled(true);
		
		addPlayButton.setEnabled(true);
		deletePlayButton.setEnabled(true);
		
		forceNewDecisionButton.setEnabled(false);
	}
	

	public void setRoleTestMode()
	{
		this.setEnabled(false);
		
		addPlayButton.setEnabled(false);
		deletePlayButton.setEnabled(false);
		
		forceNewDecisionButton.setEnabled(false);
	}
	

	public void setMatchMode()
	{
		this.setEnabled(false);
		
		addPlayButton.setEnabled(false);
		deletePlayButton.setEnabled(false);
		
		forceNewDecisionButton.setEnabled(true);
	}
	

	@Override
	public void setEmergencyMode()
	{
		this.setEnabled(false);
		
		addPlayButton.setEnabled(false);
		deletePlayButton.setEnabled(false);
		
		forceNewDecisionButton.setEnabled(false);
	}
	

	/**
	 * Updates the play panel with the actual active plays. Depending on the active plays
	 * in last frame this function will add or remove plays from the list.
	 * 
	 * @param activePlays the actual play in aiFrame
	 */
	public void setActivePlays(final List<APlay> plays)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				// Insert
				int i = 0;
				while (activePlaysListModel.size() > i && plays.size() > i)
				{
					EPlay ePlay = plays.get(i).getType();
					if (!activePlaysListModel.get(i).equals(ePlay))
					{
						activePlaysListModel.insertElementAt(ePlay, i);
					}
					i++;
				}
				
				// Cut off
				final int modelSize = activePlaysListModel.size();
				int diff = modelSize - plays.size();
				if (diff > 0)
				{
					activePlaysListModel.removeRange(modelSize - diff, modelSize - 1);
				} else if (diff < 0)
				{
					for (int j = i; j < plays.size(); j++)
					{
						EPlay ePlay = plays.get(j).getType();
						activePlaysListModel.addElement(ePlay);
					}
				}
			}
		});
	}
	

	/**
	 * Called by AICenterPresenter on every NewAIInfoFrame!
	 * Sets the Textfield "Number Of Bots"
	 * 
	 * @param numberOfBotsWithoutRole
	 */
	public void setBotsWithoutRole(int numberOfBotsWithoutRole)
	{
		if (lastFreeBots != numberOfBotsWithoutRole)
		{
			freeBots.setText(Integer.toString(numberOfBotsWithoutRole));
			lastFreeBots = numberOfBotsWithoutRole;
		}
	}
	

	public void setBestPlays(List<PlayScore> scores)
	{
		if (!scores.isEmpty())
		{
			// Clear
			
			// Re-fill
			final int numScores = scores.size();
			for (int i = 0; i < numScores && i < tableBestPlays.getRowCount(); i++)
			{
				final PlayScore score = scores.get(i);
				String playString = "";
				for (EPlay play : score.tuple.getPlays())
				{
					playString += play.toString().toLowerCase() + " , ";
				}
				tableBestPlays.setValueAt(playString, i, 0);
				tableBestPlays.setValueAt(score.score, i, 1);
				
			}
		}
	}
	

	public void addObserver(IPlayControlPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	

	public void removeObserver(IPlayControlPanelObserver oddObserver)
	{
		synchronized (observers)
		{
			observers.remove(oddObserver);
		}
	}
	

	@Override
	public void onStart()
	{
		this.setEnabled(true);
		
		addPlayButton.setEnabled(false);
		deletePlayButton.setEnabled(false);
		
		forceNewDecisionButton.setEnabled(false);
	}
	

	@Override
	public void onStop()
	{
		this.setEnabled(false);
		
		addPlayButton.setEnabled(false);
		deletePlayButton.setEnabled(false);
		
		forceNewDecisionButton.setEnabled(false);
		
		freeBots.setText("");
		
		// Reset performance mopeds
		activePlaysListModel.clear();
		lastFreeBots = INIT_FREE_BOTS;
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- Actions --------------------------------------------------------
	// --------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- Listeners --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	public class DeleteSelectedPlayListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			ListSelectionModel lsm = activePlaysList.getSelectionModel();
			int firstSelected = lsm.getMinSelectionIndex();
			int lastSelected = lsm.getMaxSelectionIndex();
			List<EPlay> rmPlays = new LinkedList<EPlay>();
			
			if (firstSelected != -1 && lastSelected != -1)
			{
				for (int i = firstSelected; i <= lastSelected; i++)
				{
					rmPlays.add((EPlay) activePlaysListModel.elementAt(i));
				}
				// activePlaysListModel.removeRange(firstSelected, lastSelected);
				
				for (IPlayControlPanelObserver o : observers)
				{
					o.removePlay(rmPlays);
				}
				int size = activePlaysListModel.size();
				
				if (size != 0)
				{
					// deletePlayButton.setEnabled(false);
					// } else
					// {
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
	}
	
	
	public class ForceNewDecisionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			for (IPlayControlPanelObserver o : observers)
			{
				o.forceNewDecision();
			}
		}
	}
	

	private class AddSelectedPlayListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			EPlay play = (EPlay) selectPlayBox.getSelectedItem();
			for (IPlayControlPanelObserver o : observers)
			{
				o.addPlay(play);
			}
		}
	}
	
	
	private static class PlayListRenderer extends DefaultListCellRenderer
	{
		private static final long	serialVersionUID	= 4482431532438405782L;
		

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus)
		{
			final EPlay ePlay = (EPlay) value;
			final String newName = ePlay.toString();
			
			if (!getText().equals(newName))
			{
				setText(newName);
			}
			
			if (isSelected)
			{
				setBackground(list.getSelectionBackground());
		      setForeground(list.getSelectionForeground());
			} else
			{
		      setBackground(list.getBackground());
		      setForeground(list.getForeground());
			}
			
			return this;
		}
	}
}
