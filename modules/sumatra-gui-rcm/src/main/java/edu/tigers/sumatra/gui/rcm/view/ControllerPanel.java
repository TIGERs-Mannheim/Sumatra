/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.rcm.view;

import edu.tigers.sumatra.gui.rcm.presenter.IRCMConfigChangedObserver;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.rcm.ERcmEvent;
import edu.tigers.sumatra.rcm.ExtIdentifier;
import edu.tigers.sumatra.rcm.ExtIdentifierParams;
import edu.tigers.sumatra.rcm.RcmAction;
import edu.tigers.sumatra.rcm.RcmAction.EActionType;
import edu.tigers.sumatra.rcm.RcmActionMap;
import edu.tigers.sumatra.rcm.RcmActionMapping;
import lombok.extern.log4j.Log4j2;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
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


/**
 * Main panel of the module
 */
@Log4j2
public class ControllerPanel extends JPanel
{
	private static final String NO_BOT_SELECTED = "no bot selected";

	private final JLabel lblSelectedBot = new JLabel(NO_BOT_SELECTED);
	private final JLabel lblSelectedConfig = new JLabel();
	private final JPanel mappingPanel = new JPanel();
	private final ControllerConfigPanel configPanel = new ControllerConfigPanel();
	private final JCheckBox chkEnabled = new JCheckBox("enabled", true);

	private final List<IRCMConfigChangedObserver> observers = new CopyOnWriteArrayList<>();


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

		chkEnabled.addActionListener(new EnableActionListener());

		JPanel botIdPanel = new JPanel(new FlowLayout());
		JLabel botNumberLabel = new JLabel("BotID: ");
		botIdPanel.add(botNumberLabel);
		botIdPanel.add(lblSelectedBot);
		lblSelectedBot.addMouseListener(new BotSelectedListener());

		JLabel lblConfig = new JLabel("  Config: ");
		botIdPanel.add(lblConfig);
		botIdPanel.add(lblSelectedConfig);

		add(chkEnabled);
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


	/**
	 * @param observer
	 */
	public void addObserver(final IRCMConfigChangedObserver observer)
	{
		observers.add(observer);
	}


	/**
	 * @param conf
	 */
	public void reloadConfig(final RcmActionMap conf)
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


	private class EnableActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			observers.forEach(o -> o.onEnabled(chkEnabled.isSelected()));
		}
	}

	private class SaveConfigActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IRCMConfigChangedObserver observer : observers)
			{
				observer.onSaveConfig();
			}
		}
	}

	private class SaveConfigAsActionListener implements ActionListener
	{
		private final JFileChooser fc = new JFileChooser();
		private final ConfFileFilter confFilter = new ConfFileFilter();


		private SaveConfigAsActionListener()
		{
			fc.setFileFilter(confFilter);
			fc.setCurrentDirectory(new File("config/rcm"));
		}


		@Override
		public void actionPerformed(final ActionEvent e)
		{
			final int returnVal = fc.showSaveDialog(ControllerPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = fc.getSelectedFile();
				if (!file.getName().endsWith(confFilter.getFileSuffix()))
				{
					file = new File(file.getAbsolutePath() + confFilter.getFileSuffix());
				}
				if (file.exists())
				{
					final int answer = JOptionPane.showConfirmDialog(
							ControllerPanel.this, "Overwrite " + file.getName()
									+ "?"
					);
					if (answer != JOptionPane.YES_OPTION)
					{
						actionPerformed(e);
						return;
					}
				}
				log.info("Saving config to \"" + file.getAbsolutePath() + "\"");
				for (IRCMConfigChangedObserver observer : observers)
				{
					observer.onSaveConfigAs(file);
				}
			}
		}
	}

	private class LoadConfigActionListener implements ActionListener
	{
		private final JFileChooser fc = new JFileChooser();


		public LoadConfigActionListener()
		{
			fc.setFileFilter(new ConfFileFilter());
			fc.setCurrentDirectory(new File("config/rcm"));
		}


		@Override
		public void actionPerformed(final ActionEvent e)
		{
			final int returnVal = fc.showOpenDialog(ControllerPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				final File file = fc.getSelectedFile();
				log.info("Opening: " + file.getName());
				for (IRCMConfigChangedObserver observer : observers)
				{
					observer.onLoadConfig(file);
				}
			}
		}
	}

	private class LoadDefaultConfigActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IRCMConfigChangedObserver observer : observers)
			{
				observer.onLoadDefaultConfig();
			}
		}
	}

	private class AddMappingActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			RcmActionMapping mapping = new RcmActionMapping(
					new ArrayList<>(), new RcmAction(
					ERcmEvent.UNASSIGNED, EActionType.EVENT)
			);
			mapping.getIdentifiers().add(new ExtIdentifier("", ExtIdentifierParams.createDefault()));
			addMapping(mapping);
			revalidate();

			for (IRCMConfigChangedObserver observer : observers)
			{
				observer.onActionMappingCreated(mapping);
			}
		}
	}

	private class RemoveMappingActionListener implements ActionListener
	{
		private final JPanel panel;
		private final RcmActionMapping mapping;


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
			for (IRCMConfigChangedObserver observer : observers)
			{
				observer.onActionMappingRemoved(mapping);
			}
		}
	}

	private class AssistantActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (IRCMConfigChangedObserver observer : observers)
			{
				observer.onSelectionAssistant();
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
			// ignore
		}


		@Override
		public void mouseReleased(final MouseEvent e)
		{
			// ignore
		}


		@Override
		public void mouseEntered(final MouseEvent e)
		{
			// ignore
		}


		@Override
		public void mouseExited(final MouseEvent e)
		{
			// ignore
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
