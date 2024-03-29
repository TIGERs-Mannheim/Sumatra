/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.config;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.HierarchicalConfiguration;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.io.Serial;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * Config editor main panel.
 */
public class ConfigEditorPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = -7007103316635397718L;

	private final JTabbedPane tabpane;
	private final SortedMap<String, EditorView> tabs = new TreeMap<>();


	public ConfigEditorPanel()
	{
		setLayout(new MigLayout("fill, wrap 1, inset 0"));

		tabpane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		tabpane.addChangeListener(e -> {
			Component c = tabpane.getComponentAt(tabpane.getSelectedIndex());
			EditorView ev = (EditorView) c;
			ev.initialReload();
		});
		add(tabpane, "grow");
	}


	/**
	 * @param client
	 * @param observer
	 */
	public void addConfigModel(final String client, final IConfigEditorViewObserver observer)
	{
		if (tabs.containsKey(client))
		{
			return;
		}
		final EditorView newView = new EditorView(client, client, new HierarchicalConfiguration(),
				true);
		newView.addObserver(observer);

		String configKey = newView.getConfigKey();
		tabs.put(configKey, newView);

		int index = 0;
		for (String key : tabs.keySet())
		{
			if (key.equals(configKey))
			{
				break;
			}
			index++;
		}
		tabpane.insertTab(client, null, newView, null, index);

		revalidate();
		this.repaint();
	}


	/**
	 * @param name
	 * @param config
	 */
	public void refreshConfigModel(final String name, final HierarchicalConfiguration config)
	{
		final EditorView view = tabs.get(name);
		view.updateModel(config, true);
	}
}
