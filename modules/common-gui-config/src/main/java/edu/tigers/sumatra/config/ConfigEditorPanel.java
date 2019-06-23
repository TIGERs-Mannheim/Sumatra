/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 26, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.config;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.apache.commons.configuration.HierarchicalConfiguration;

import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ConfigEditorPanel extends JPanel implements ISumatraView
{
	private static final long							serialVersionUID	= -7007103316635397718L;
																						
	private final JTabbedPane							tabpane;
	private final SortedMap<String, EditorView>	tabs					= new TreeMap<String, EditorView>();
																						
																						
	/**
	 */
	public ConfigEditorPanel()
	{
		setLayout(new MigLayout("fill, wrap 1, inset 0"));
		
		tabpane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
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
	
	
	@Override
	public void onShown()
	{
	
	}
	
	
	@Override
	public void onHidden()
	{
	
	}
	
	
	@Override
	public void onFocused()
	{
	
	}
	
	
	@Override
	public void onFocusLost()
	{
	
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		return null;
	}
}
