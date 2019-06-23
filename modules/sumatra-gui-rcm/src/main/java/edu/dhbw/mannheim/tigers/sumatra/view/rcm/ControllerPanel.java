/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.11.2011
 * Author(s): Sven Frank
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.rcm;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.presenter.rcm.IRCMConfigChangedObserver;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.rcm.ERcmEvent;
import edu.tigers.sumatra.rcm.ExtIdentifier;
import edu.tigers.sumatra.rcm.ExtIdentifierParams;
import edu.tigers.sumatra.rcm.RcmAction;
import edu.tigers.sumatra.rcm.RcmAction.EActionType;
import edu.tigers.sumatra.rcm.RcmActionMap;
import edu.tigers.sumatra.rcm.RcmActionMapping;


/**
 * - Main panel of the module
 * 
 * @author Sven Frank
 */
public class ControllerPanel extends JPanel
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final String							NO_BOT_SELECTED	= "no bot selected";
	private static final long								serialVersionUID	= -4425620303404436160L;
	private static final Logger							log					= Logger
																									.getLogger(ControllerPanel.class
																											.getName());
	
	/** */
	private final JLabel										lblSelectedBot		= new JLabel(NO_BOT_SELECTED);
	private final JLabel										lblSelectedConfig	= new JLabel();
	private final JPanel										mappingPanel		= new JPanel();
	private final ControllerConfigPanel					configPanel			= new ControllerConfigPanel();
	
	private final List<IRCMConfigChangedObserver>	observers			= new CopyOnWriteArrayList<IRCMConfigChangedObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public ControllerPanel()
	{
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new SaveConfigActionListener());
		buttonPanel.add(btnSave);
		JButton btnSaveAs = new JButton("Save as");
		btnSaveAs.addActionListener(new SaveConfigAsActionListener());
		buttonPanel.add(btnSaveAs);
		JButton btnLoad = new JButton("Load");
		btnLoad.addActionListener(new LoadConfigActionListener());
		buttonPanel.add(btnLoad);
		JButton btnDefault = new JButton("Default");
		btnDefault.addActionListener(new LoadDefaultConfigActionListener());
		buttonPanel.add(btnDefault);
		JButton btnAddMapping = new JButton("Add Mapping");
		btnAddMapping.addActionListener(new AddMappingActionListener());
		buttonPanel.add(btnAddMapping);
		JButton btnAssistant = new JButton("Assistant");
		btnAssistant.addActionListener(new AssistantActionListener());
		buttonPanel.add(btnAssistant);
		
		add(buttonPanel);
		
		JPanel botIdPanel = new JPanel(new FlowLayout());
		JLabel botNumberLabel = new JLabel("BotID: ");
		botIdPanel.add(botNumberLabel);
		botIdPanel.add(lblSelectedBot);
		lblSelectedBot.addMouseListener(new BotSelectedListener());
		
		JLabel lblConfig = new JLabel("  Config: ");
		botIdPanel.add(lblConfig);
		botIdPanel.add(lblSelectedConfig);
		
		add(botIdPanel);
		add(configPanel);
		
		mappingPanel.setLayout(new BoxLayout(mappingPanel, BoxLayout.PAGE_AXIS));
		JScrollPane scrollPane = new JScrollPane(mappingPanel);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(1000, 2000));
		add(scrollPane);
		
		add(Box.createGlue());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IRCMConfigChangedObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IRCMConfigChangedObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	/**
	 * @param conf
	 */
	public void reloadConfig(final RcmActionMap conf)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				lblSelectedConfig.setText(conf.getConfigName());
				mappingPanel.removeAll();
				for (RcmActionMapping mapping : conf.getActionMappings())
				{
					addMapping(mapping);
				}
				configPanel.updateConfig(conf);
				revalidate();
			}
		});
	}
	
	
	private void addMapping(final RcmActionMapping mapping)
	{
		JPanel wrapPanel = new JPanel();
		wrapPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		ControllerMappingPanel cmPanel = new ControllerMappingPanel(mapping, observers);
		JButton btnRemove = new JButton("-");
		btnRemove.addActionListener(new RemoveMappingActionListener(mapping, wrapPanel));
		wrapPanel.add(btnRemove);
		wrapPanel.add(cmPanel);
		mappingPanel.add(wrapPanel);
	}
	
	
	private String createBotString(final BotID botId)
	{
		if (!botId.isBot())
		{
			return "disabled";
		}
		return botId.getNumber() + " - " + botId.getTeamColor();
	}
	
	
	/**
	 * @param botId
	 */
	public void setSelectedBot(final BotID botId)
	{
		lblSelectedBot.setText(createBotString(botId));
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	private class SaveConfigActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			synchronized (observers)
			{
				for (IRCMConfigChangedObserver observer : observers)
				{
					observer.onSaveConfig();
				}
			}
		}
	}
	
	private class SaveConfigAsActionListener implements ActionListener
	{
		private final JFileChooser		fc				= new JFileChooser();
		private final ConfFileFilter	confFilter	= new ConfFileFilter();
		private final File				dir			= new File("config/rcm");
		
		
		/**
		 */
		private SaveConfigAsActionListener()
		{
			fc.setFileFilter(confFilter);
			fc.setCurrentDirectory(dir);
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			File file;
			final int returnVal = fc.showSaveDialog(ControllerPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				file = fc.getSelectedFile();
				if (!file.getName().endsWith(confFilter.getFileSuffix()))
				{
					file = new File(file.getAbsolutePath() + confFilter.getFileSuffix());
				}
				if (file.exists())
				{
					final int answer = JOptionPane.showConfirmDialog(ControllerPanel.this, "Overwrite " + file.getName()
							+ "?");
					if (answer != JOptionPane.YES_OPTION)
					{
						actionPerformed(e);
						return;
					}
				}
				log.info("Saving config to \"" + file.getAbsolutePath() + "\"");
				synchronized (observers)
				{
					for (IRCMConfigChangedObserver observer : observers)
					{
						observer.onSaveConfigAs(file);
					}
				}
			}
		}
	}
	
	private class LoadConfigActionListener implements ActionListener
	{
		private final JFileChooser	fc		= new JFileChooser();
		private final File			dir	= new File("config/rcm");
		
		
		/**
		 */
		public LoadConfigActionListener()
		{
			fc.setFileFilter(new ConfFileFilter());
			fc.setCurrentDirectory(dir);
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			final int returnVal = fc.showOpenDialog(ControllerPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				final File file = fc.getSelectedFile();
				log.info("Opening: " + file.getName());
				synchronized (observers)
				{
					for (IRCMConfigChangedObserver observer : observers)
					{
						observer.onLoadConfig(file);
					}
				}
			}
		}
	}
	
	private class LoadDefaultConfigActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			synchronized (observers)
			{
				for (IRCMConfigChangedObserver observer : observers)
				{
					observer.onLoadDefaultConfig();
				}
			}
		}
	}
	
	private class AddMappingActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			RcmActionMapping mapping = new RcmActionMapping(new ArrayList<ExtIdentifier>(), new RcmAction(
					ERcmEvent.UNASSIGNED, EActionType.EVENT));
			mapping.getIdentifiers().add(new ExtIdentifier("", ExtIdentifierParams.createDefault()));
			addMapping(mapping);
			revalidate();
			
			synchronized (observers)
			{
				for (IRCMConfigChangedObserver observer : observers)
				{
					observer.onActionMappingCreated(mapping);
				}
			}
		}
	}
	
	private class RemoveMappingActionListener implements ActionListener
	{
		private final JPanel					panel;
		private final RcmActionMapping	mapping;
		
		
		/**
		 * @param mapping
		 * @param panel
		 */
		public RemoveMappingActionListener(final RcmActionMapping mapping, final JPanel panel)
		{
			this.panel = panel;
			this.mapping = mapping;
		}
		
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			mappingPanel.remove(panel);
			revalidate();
			synchronized (observers)
			{
				for (IRCMConfigChangedObserver observer : observers)
				{
					observer.onActionMappingRemoved(mapping);
				}
			}
		}
	}
	
	private class AssistantActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			synchronized (observers)
			{
				for (IRCMConfigChangedObserver observer : observers)
				{
					observer.onSelectionAssistant();
				}
			}
		}
	}
	
	private class BotSelectedListener implements MouseListener
	{
		@Override
		public void mouseClicked(final MouseEvent e)
		{
			for (IRCMConfigChangedObserver o : observers)
			{
				o.onUnassignBot();
			}
		}
		
		
		@Override
		public void mousePressed(final MouseEvent e)
		{
		}
		
		
		@Override
		public void mouseReleased(final MouseEvent e)
		{
		}
		
		
		@Override
		public void mouseEntered(final MouseEvent e)
		{
		}
		
		
		@Override
		public void mouseExited(final MouseEvent e)
		{
		}
	}
	
	
	/**
	 * @return the configPanel
	 */
	public ControllerConfigPanel getConfigPanel()
	{
		return configPanel;
	}
}
