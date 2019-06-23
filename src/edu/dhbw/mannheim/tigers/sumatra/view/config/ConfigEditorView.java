/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.11.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.config;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigClient;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ManagedConfig;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;


/**
 * This view allows the user to view and edit the various XML configurations which are registered at the
 * ConfigEdtior-module. The changes made to the configurations are then instantly available to the runtime.
 * 
 * @author Gero
 * 
 */
public class ConfigEditorView extends JPanel implements ISumatraView
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long					serialVersionUID	= -7007103316635397718L;
	
	private static final String				TITLE					= "Config Editor";
	private static final int					ID						= 8;
	
	private final JTabbedPane					tabpane;
	private final Map<String, EditorView>	tabs					= new HashMap<String, EditorView>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public ConfigEditorView()
	{
		setLayout(new MigLayout("fill, wrap 1"));
		
		tabpane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		add(tabpane, "grow");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param newConfig
	 * @param observer
	 */
	public void addConfigModel(ManagedConfig newConfig, IConfigEditorViewObserver observer)
	{
		final IConfigClient client = newConfig.getClient();
		final EditorView newView = new EditorView(client.getName(), client.getConfigKey(), newConfig.getXmlConfig(),
				client.isEditable());
		newView.addObserver(observer);
		
		tabs.put(newView.getConfigKey(), newView);
		tabpane.addTab(client.getName(), newView);
		
		revalidate();
		this.repaint();
	}
	
	
	/**
	 * @param config
	 */
	public void refreshConfigModel(ManagedConfig config)
	{
		final IConfigClient client = config.getClient();
		final EditorView view = tabs.get(client.getConfigKey());
		view.updateModel(config.getXmlConfig(), client.isEditable());
		// this.revalidate();
		// this.repaint();
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
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public int getId()
	{
		return ID;
	}
	
	
	@Override
	public String getTitle()
	{
		return TITLE;
	}
	
	
	@Override
	public Component getViewComponent()
	{
		return this;
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		return null;
	}
	
	
}
