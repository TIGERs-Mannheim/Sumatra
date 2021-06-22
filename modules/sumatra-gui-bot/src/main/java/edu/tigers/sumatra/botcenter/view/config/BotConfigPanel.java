/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botcenter.view.config;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import edu.tigers.sumatra.botcenter.presenter.config.ConfigFile;
import net.miginfocom.swing.MigLayout;


/**
 * Generic bot config panel.
 */
public class BotConfigPanel extends JPanel
{
	private static final long serialVersionUID = 7489653214102700549L;
	private final List<IBotConfigPanelObserver> observers = new CopyOnWriteArrayList<>();
	private final Map<Integer, ConfigFilePanel> panels = new HashMap<>();
	private final JTabbedPane tabs;


	/** Constructor. */
	public BotConfigPanel()
	{
		setLayout(new BorderLayout());

		JButton queryButton = new JButton("Query File List");
		queryButton.addActionListener(ae -> notifyQueryFileList());

		JButton clearButton = new JButton("Clear File List");
		clearButton.addActionListener(ae -> notifyClearFileList());

		JPanel btnPanel = new JPanel(new MigLayout("", "[100]10[100]10[100]"));
		btnPanel.add(queryButton);
		btnPanel.add(clearButton);

		tabs = new JTabbedPane();

		add(btnPanel, BorderLayout.NORTH);
		add(tabs, BorderLayout.CENTER);
	}


	/**
	 * @param file
	 */
	public void addConfigFile(final ConfigFile file)
	{
		ConfigFilePanel panel = panels.get(file.getConfigId());
		if (panel == null)
		{
			panel = new ConfigFilePanel(file);
			panels.put(file.getConfigId(), panel);
			tabs.add(file.getName(), panel);
			return;
		}

		panel.updateValues(file);
	}


	/**
	 * @param configId
	 */
	public void removeConfigFile(final int configId)
	{
		ConfigFilePanel p = panels.remove(configId);

		if (p != null)
		{
			tabs.remove(p);
		}
	}


	/**
	 * @param observer
	 */
	public void addObserver(final IBotConfigPanelObserver observer)
	{
		observers.add(observer);
	}


	/**
	 * @param observer
	 */
	public void removeObserver(final IBotConfigPanelObserver observer)
	{
		observers.remove(observer);
	}


	/** */
	private void notifyQueryFileList()
	{
		for (IBotConfigPanelObserver observer : observers)
		{
			observer.onQueryFileList();
		}
	}


	/** */
	private void notifyClearFileList()
	{
		for (IBotConfigPanelObserver observer : observers)
		{
			observer.onClearFileList();
		}
	}


	private class ConfigFilePanel extends JPanel
	{
		private static final long serialVersionUID = 3052423100929562898L;
		private final List<JTextField> fields = new ArrayList<>();
		private final ConfigFile file;


		public ConfigFilePanel(final ConfigFile file)
		{
			this.file = file;

			setLayout(new MigLayout("wrap 2", "[150][100,fill]"));

			JButton save = new JButton("Save");
			JButton saveToAll = new JButton("Save to All");
			JButton refresh = new JButton("Refresh");

			save.addActionListener(ae -> {
				parseValues();
				notifySave(file);
			});

			saveToAll.addActionListener(ae -> {
				if (JOptionPane.showConfirmDialog(null, "Really apply these values to all active bots?", "Confirm Action",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
				{
					return;
				}
				parseValues();
				notifySaveToAll(file);
			});

			refresh.addActionListener(ae -> notifyRefresh(file));

			add(save, "span 2, split 3");
			add(saveToAll);
			add(refresh);

			for (int i = 0; i < file.getNames().size(); i++)
			{
				JLabel label = new JLabel(file.getNames().get(i));
				JTextField field = new JTextField(file.getValues().get(i));

				add(label);
				add(field);

				fields.add(field);
			}
		}


		private void notifySave(final ConfigFile file)
		{
			for (IBotConfigPanelObserver observer : observers)
			{
				observer.onSave(file);
			}
		}


		private void notifySaveToAll(final ConfigFile file)
		{
			for (IBotConfigPanelObserver observer : observers)
			{
				observer.onSaveToAll(file);
			}
		}


		private void notifyRefresh(final ConfigFile file)
		{
			for (IBotConfigPanelObserver observer : observers)
			{
				observer.onRefresh(file);
			}
		}


		private void updateValues(final ConfigFile file)
		{
			SwingUtilities.invokeLater(() -> {
				for (int i = 0; i < file.getValues().size(); i++)
				{
					fields.get(i).setText(file.getValues().get(i));
				}
			});
		}


		private void parseValues()
		{
			for (int i = 0; i < fields.size(); i++)
			{
				file.getValues().set(i, fields.get(i).getText());
			}
		}
	}
}
