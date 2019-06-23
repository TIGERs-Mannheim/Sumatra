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
import java.awt.FlowLayout;
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.PlayType;


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
	private static final long								serialVersionUID			= -2647962360775299686L;
	private static final Logger							log							= Logger.getLogger(PlayControlPanel.class
																											.getName());
	
	private JList<APlay>										activePlaysList			= null;
	private DefaultListModel<APlay>						activePlaysListModel		= null;
	
	private JButton											forceNewDecisionButton	= null;
	
	private JButton											deletePlayButton			= null;
	private JButton											addPlayButton				= null;
	private JButton											clearPlaysButton			= null;
	
	private JComboBox<EPlayType>							selectPlayTypeBox			= null;
	private JComboBox<EPlay>								selectPlayBox				= null;
	
	private JTextField										numRolesToAssign			= null;
	
	private JTextField										freeBots						= null;
	private static final int								INIT_FREE_BOTS				= -1;
	private static final int								PLAYS_LIST_SIZE_X			= 350;
	private static final int								PLAYS_LIST_SIZE_Y			= 120;
	private int													lastFreeBots				= INIT_FREE_BOTS;
	
	private final List<IPlayControlPanelObserver>	observers					= new LinkedList<IPlayControlPanelObserver>();
	
	
	private enum EPlayType
	{
		GAME("Game"),
		STANDARD("Standard"),
		KEEPER("Keeper"),
		TEST("Test"),
		CALIBRATE("Caliabrate"),
		DEPRECATED("Deprecated"),
		DEFECT("Defect"),
		MIXED_TEAM("Mixed Team"),
		CHALLENGE("Challenge"),
		DISABLED("Disabled"),
		ALL("All");
		
		private final String	text;
		
		
		private EPlayType(String text)
		{
			this.text = text;
		}
		
		
		/**
		 * @return the text
		 */
		public final String getText()
		{
			return text;
		}
		
		
		@Override
		public String toString()
		{
			return getText();
		}
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public PlayControlPanel()
	{
		setLayout(new MigLayout());
		setBorder(BorderFactory.createTitledBorder("Play Control Panel"));
		
		JPanel activePlaysPanel = new JPanel();
		activePlaysPanel.setName("Active Plays");
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add(activePlaysPanel);
		
		activePlaysListModel = new DefaultListModel<APlay>();
		activePlaysList = new JList<APlay>(activePlaysListModel);
		activePlaysList.setCellRenderer(new PlayListRenderer());
		activePlaysList.setPreferredSize(new Dimension(PLAYS_LIST_SIZE_X, PLAYS_LIST_SIZE_Y));
		final JScrollPane scrollPaneActivePlays = new JScrollPane();
		scrollPaneActivePlays.getViewport().setView(activePlaysList);
		activePlaysPanel.add(activePlaysList);
		
		forceNewDecisionButton = new JButton("Force new decision");
		forceNewDecisionButton.addActionListener(new ForceNewDecisionListener());
		forceNewDecisionButton.setEnabled(false);
		
		
		deletePlayButton = new JButton("delete play");
		deletePlayButton.addActionListener(new DeleteSelectedPlayListener());
		deletePlayButton.setEnabled(false);
		
		selectPlayTypeBox = new JComboBox<EPlayType>(EPlayType.values());
		selectPlayTypeBox.addActionListener(new SelectPlayTypeListener());
		selectPlayTypeBox.setEnabled(false);
		
		selectPlayBox = new JComboBox<EPlay>();
		
		addPlayButton = new JButton("add play");
		addPlayButton.addActionListener(new AddSelectedPlayListener());
		addPlayButton.setEnabled(false);
		
		clearPlaysButton = new JButton("clear");
		clearPlaysButton.addActionListener(new ClearPlaysListener());
		clearPlaysButton.setEnabled(false);
		
		freeBots = new JTextField();
		freeBots.setEditable(false);
		freeBots.setBackground(Color.WHITE);
		final JPanel freeBotsPanel = new JPanel(new MigLayout("fill", "[]10[40,fill]"));
		freeBotsPanel.add(new JLabel("Bots without Play:"));
		freeBotsPanel.add(freeBots);
		
		numRolesToAssign = new JTextField("0", 3);
		numRolesToAssign.setToolTipText("Number of roles to assign to next added play.");
		final JPanel numRolesToAssignPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		numRolesToAssignPanel.add(new JLabel("Roles to assign: "));
		numRolesToAssignPanel.add(numRolesToAssign);
		
		final JPanel controlPanel = new JPanel(new MigLayout("fill"));
		controlPanel.add(forceNewDecisionButton, "wrap");
		controlPanel.add(freeBotsPanel, "wrap");
		controlPanel.add(deletePlayButton);
		controlPanel.add(clearPlaysButton, "wrap");
		controlPanel.add(selectPlayTypeBox, "wrap");
		controlPanel.add(selectPlayBox, "span");
		controlPanel.add(addPlayButton);
		controlPanel.add(numRolesToAssignPanel);
		
		add(tabbedPane);
		add(controlPanel);
	}
	
	
	@Override
	public void setPlayTestMode()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				setEnabled(true);
				
				String selectedPlayTypeStr = SumatraModel.getInstance().getUserProperty(
						PlayControlPanel.class.getCanonicalName() + ".selectedPlayType");
				try
				{
					// selectedPlayStr may be null, so convert
					EPlayType selectedPlayType = EPlayType.valueOf(String.valueOf(selectedPlayTypeStr));
					selectPlayTypeBox.setSelectedItem(selectedPlayType);
				} catch (IllegalArgumentException e)
				{
					// Could not read selected play from user properties: do not care, its not important.
					// maybe the user just starts Sumatra the first time
				}
				
				setSelectPlayBox();
				String selectedPlayStr = SumatraModel.getInstance().getUserProperty(
						PlayControlPanel.class.getCanonicalName() + ".selectedPlay");
				try
				{
					// selectedPlayStr may be null, so convert
					EPlay selectedPlay = EPlay.valueOf(String.valueOf(selectedPlayStr));
					selectPlayBox.setSelectedItem(selectedPlay);
				} catch (IllegalArgumentException e)
				{
					// Could not read selected play from user properties: do not care, its not important.
					// maybe the user just starts Sumatra the first time
				}
				
				addPlayButton.setEnabled(true);
				deletePlayButton.setEnabled(true);
				clearPlaysButton.setEnabled(true);
				selectPlayTypeBox.setEnabled(true);
				selectPlayBox.setEnabled(true);
				
				forceNewDecisionButton.setEnabled(false);
			}
		});
	}
	
	
	private void setSelectPlayBox()
	{
		selectPlayBox.removeAllItems();
		switch ((EPlayType) selectPlayTypeBox.getSelectedItem())
		{
			case GAME:
				for (EPlay play : PlayType.getGamePlays())
				{
					selectPlayBox.addItem(play);
				}
				break;
			case STANDARD:
				for (EPlay play : PlayType.getStandardPlays())
				{
					selectPlayBox.addItem(play);
				}
				break;
			case KEEPER:
				for (EPlay play : PlayType.getKeeperPlays())
				{
					selectPlayBox.addItem(play);
				}
				break;
			case TEST:
				for (EPlay play : PlayType.getTestPlays())
				{
					selectPlayBox.addItem(play);
				}
				break;
			case CALIBRATE:
				for (EPlay play : PlayType.getCalibratePlays())
				{
					selectPlayBox.addItem(play);
				}
				break;
			case DEPRECATED:
				for (EPlay play : PlayType.getDeprecatedPlays())
				{
					selectPlayBox.addItem(play);
				}
				break;
			case DEFECT:
				for (EPlay play : PlayType.getDefectPlays())
				{
					selectPlayBox.addItem(play);
				}
				break;
			case MIXED_TEAM:
				for (EPlay play : PlayType.getMixedTeamPlays())
				{
					selectPlayBox.addItem(play);
				}
				break;
			case CHALLENGE:
				for (EPlay play : PlayType.getChallengePlays())
				{
					selectPlayBox.addItem(play);
				}
				break;
			case DISABLED:
				for (EPlay play : PlayType.getDisabledPlays())
				{
					selectPlayBox.addItem(play);
				}
				break;
			case ALL:
				for (EPlay play : PlayType.getAllPlays())
				{
					selectPlayBox.addItem(play);
				}
		}
	}
	
	
	@Override
	public void setRoleTestMode()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				setEnabled(false);
				
				addPlayButton.setEnabled(false);
				deletePlayButton.setEnabled(false);
				clearPlaysButton.setEnabled(false);
				selectPlayTypeBox.setEnabled(false);
				selectPlayBox.setEnabled(false);
				
				forceNewDecisionButton.setEnabled(false);
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
				setEnabled(false);
				
				addPlayButton.setEnabled(false);
				deletePlayButton.setEnabled(false);
				clearPlaysButton.setEnabled(false);
				selectPlayTypeBox.setEnabled(false);
				selectPlayBox.setEnabled(false);
				
				forceNewDecisionButton.setEnabled(true);
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
				setEnabled(false);
				
				addPlayButton.setEnabled(false);
				deletePlayButton.setEnabled(false);
				clearPlaysButton.setEnabled(false);
				selectPlayTypeBox.setEnabled(false);
				selectPlayBox.setEnabled(false);
				
				forceNewDecisionButton.setEnabled(false);
			}
		});
	}
	
	
	/**
	 * Updates the play panel with the actual active plays. Depending on the active plays
	 * in last frame this function will add or remove plays from the list.
	 * @param plays the actual play in aiFrame
	 */
	public void setActivePlays(final List<APlay> plays)
	{
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				// Insert
				int i = 0;
				while ((activePlaysListModel.size() > i) && (plays.size() > i))
				{
					final APlay play = plays.get(i);
					if (!activePlaysListModel.get(i).equals(play))
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
			}
		});
	}
	
	
	/**
	 * Called by AICenterPresenter on every NewAIInfoFrame!
	 * Sets the Textfield "Number Of Bots"
	 * 
	 * @param numberOfBotsWithoutRole
	 */
	public void setBotsWithoutRole(final int numberOfBotsWithoutRole)
	{
		if (lastFreeBots != numberOfBotsWithoutRole)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					freeBots.setText(Integer.toString(numberOfBotsWithoutRole));
					lastFreeBots = numberOfBotsWithoutRole;
				}
			});
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(IPlayControlPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param oddObserver
	 */
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
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				setEnabled(true);
				
				addPlayButton.setEnabled(false);
				deletePlayButton.setEnabled(false);
				clearPlaysButton.setEnabled(false);
				selectPlayBox.setEnabled(false);
				selectPlayBox.setEnabled(false);
				
				forceNewDecisionButton.setEnabled(false);
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
				
				addPlayButton.setEnabled(false);
				deletePlayButton.setEnabled(false);
				clearPlaysButton.setEnabled(false);
				selectPlayTypeBox.setEnabled(false);
				selectPlayBox.setEnabled(false);
				
				forceNewDecisionButton.setEnabled(false);
				
				freeBots.setText("");
				
				// Reset performance mopeds
				activePlaysListModel.clear();
				lastFreeBots = INIT_FREE_BOTS;
			}
		});
	}
	
	
	// --------------------------------------------------------------------------
	// --- Actions --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- Listeners --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public class DeleteSelectedPlayListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
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
	
	/**
	 */
	public class ForceNewDecisionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			for (final IPlayControlPanelObserver o : observers)
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
			final EPlay play = (EPlay) selectPlayBox.getSelectedItem();
			for (final IPlayControlPanelObserver o : observers)
			{
				try
				{
					final int roles = Integer.parseInt(numRolesToAssign.getText());
					o.addNewPlay(play, roles);
				} catch (final NumberFormatException err)
				{
					o.addNewPlay(play, EPlay.MAX_BOTS);
					log.error("Could not parse number, using default");
				}
			}
			
			SumatraModel.getInstance().setUserProperty(PlayControlPanel.class.getCanonicalName() + ".selectedPlay",
					play.name());
		}
	}
	
	/**
	 */
	public class ClearPlaysListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			for (int i = 0; i < activePlaysList.getModel().getSize(); i++)
			{
				for (final IPlayControlPanelObserver o : observers)
				{
					o.removePlay(activePlaysListModel.elementAt(i));
				}
			}
		}
	}
	
	private class SelectPlayTypeListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			setSelectPlayBox();
			SumatraModel.getInstance().setUserProperty(PlayControlPanel.class.getCanonicalName() + ".selectedPlayType",
					((EPlayType) selectPlayTypeBox.getSelectedItem()).name());
		}
	}
	
	private static class PlayListRenderer extends DefaultListCellRenderer
	{
		private static final long	serialVersionUID	= 4482431532438405782L;
		
		
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus)
		{
			final APlay play = (APlay) value;
			final String newName = play.getType().toString();
			
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
