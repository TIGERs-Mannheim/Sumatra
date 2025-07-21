/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.botcenter.view.config;

import edu.tigers.sumatra.botmanager.configs.ConfigFile;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;


/**
 * Generic bot config panel.
 */
public class BotConfigPanel extends JPanel
{
	private static final long serialVersionUID = 7489653214102700549L;
	private final List<IBotConfigPanelObserver> observers = new CopyOnWriteArrayList<>();
	private final List<IBotConfigObserver> configObservers = new CopyOnWriteArrayList<>();
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
	public void addConfigFile(final ConfigFile file, ConfigFile savedFile)
	{
		ConfigFilePanel panel = panels.get(file.getConfigId());
		if (panel == null)
		{
			panel = new ConfigFilePanel(file);
			panels.put(file.getConfigId(), panel);
			tabs.add(file.getName(), panel);
			if (savedFile != null)
			{
				updateSavedValues(savedFile);
			}
			return;
		}
		panel.updateValues(file, savedFile);
	}


	public void updateSavedValues(final ConfigFile file)
	{
		ConfigFilePanel panel = panels.get(file.getConfigId());
		if (panel != null && panel.file.getVersion() == file.getVersion())
		{
			IntStream.range(0, file.getValues().size())
					.forEach(i -> panel.savedFields.get(i).setText(file.getValues().get(i)));
		}
	}


	public void removeSavedValues(final int configId, final int version)
	{
		ConfigFilePanel panel = panels.get(configId);
		if (panel != null && panel.file.getVersion() == version)
		{
			panel.savedFields.forEach(f -> f.setText(""));
		}
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


	public void addConfigObserver(final IBotConfigObserver observer)
	{
		configObservers.add(observer);
	}


	public void removeConfigObserver(final IBotConfigObserver observer)
	{
		configObservers.remove(observer);
	}


	/**
	 *
	 */
	private void notifyQueryFileList()
	{
		observers.forEach(IBotConfigPanelObserver::onQueryFileList);
	}


	/** */
	private void notifyClearFileList()
	{
		observers.forEach(IBotConfigPanelObserver::onClearFileList);
	}


	private class ConfigFilePanel extends JPanel
	{
		private static final long serialVersionUID = 3052423100929562898L;
		private final List<JTextField> fields = new ArrayList<>();
		private final List<JLabel> savedFields = new ArrayList<>();
		private final ConfigFile file;


		public ConfigFilePanel(final ConfigFile file)
		{
			this.file = file;

			setLayout(new MigLayout("wrap 3", "[150][150,fill][100]"));

			JButton save = new JButton("Save");
			JButton saveToAll = new JButton("Save to All");
			JButton refresh = new JButton("Refresh");
			JButton saveToFile = new JButton("Save to File");

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

			saveToFile.addActionListener(ae -> notifySaveToFile(file));

			add(save, "span 3, split 4");
			add(saveToAll);
			add(refresh);
			add(saveToFile);

			add(new JLabel("Version"));
			add(new JLabel(Integer.toString(file.getVersion())), "wrap");


			for (int i = 0; i < file.getNames().size(); i++)
			{
				JLabel label = new JLabel(file.getNames().get(i));
				JTextField field = new JTextField(file.getValues().get(i));
				JLabel savedVal = new JLabel();

				add(label);
				add(field);
				add(savedVal);

				fields.add(field);
				savedFields.add(savedVal);
			}
		}


		private void notifySave(final ConfigFile file)
		{
			observers.forEach(o -> o.onSave(file));
		}


		private void notifySaveToAll(final ConfigFile file)
		{
			observers.forEach(o -> o.onSaveToAll(file));
		}


		private void notifyRefresh(final ConfigFile file)
		{
			observers.forEach(o -> o.onRefresh(file));
		}


		private void notifySaveToFile(final ConfigFile file)
		{
			configObservers.forEach(o -> o.onSaveToFile(file));
			updateValues(file, file);
		}


		private void updateValues(final ConfigFile file, ConfigFile savedFile)
		{
			for (int i = 0; i < file.getValues().size(); i++)
			{
				fields.get(i).setText(file.getValues().get(i));
				if (savedFile != null)
				{
					savedFields.get(i).setText(savedFile.getValues().get(i));
				}
			}
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
