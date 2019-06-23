/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.aicenter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.InformationPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.botoverview.BotFullOverviewPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.ModuleControlPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.commons.ConfigControlMenu;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;


/**
 * This is the main panel of the ai view in sumatra.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class AICenterPanel extends JPanel implements ISumatraView
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long			serialVersionUID	= 8132550010453691515L;
	
	private static final int			ID						= 3;
	private static final String		TITLE					= "AI Center";
	
	private InformationPanel			informationPanel	= null;
	private BotFullOverviewPanel		botPanel				= null;
	private ModuleControlPanel			modulesPanel		= null;
	
	private final ConfigControlMenu	aiConfigMenu		= new ConfigControlMenu("AI-Config", AAgent.KEY_AI_CONFIG);
	private final ConfigControlMenu	geomMenu				= new ConfigControlMenu("Geometry", AAgent.KEY_GEOMETRY_CONFIG);
	private final ConfigControlMenu	teamMenu				= new ConfigControlMenu("Team", AAgent.KEY_TEAM_CONFIG);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public AICenterPanel()
	{
		setLayout(new BorderLayout());
		
		final JPanel combinePanel = new JPanel();
		combinePanel.setLayout(new MigLayout("fill", "", ""));
		
		informationPanel = new InformationPanel();
		botPanel = new BotFullOverviewPanel();
		modulesPanel = new ModuleControlPanel();
		
		combinePanel.add(informationPanel, "wrap");
		combinePanel.add(modulesPanel, "wrap");
		combinePanel.add(botPanel);
		
		
		JScrollPane scrollPane = new JScrollPane(combinePanel);
		this.add(scrollPane, BorderLayout.CENTER);
		
		UIManager.put("TabbedPane.tabAreaInsets", new Insets(0, 0, 0, 0));
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @return
	 */
	public InformationPanel getInformationPanel()
	{
		return informationPanel;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ModuleControlPanel getModulesPanel()
	{
		return modulesPanel;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public BotFullOverviewPanel getBotOverviewPanel()
	{
		return botPanel;
	}
	
	
	/**
	 * 
	 */
	public void clearView()
	{
		informationPanel.clearView();
		modulesPanel.onStop();
	}
	
	
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
		final List<JMenu> menus = new ArrayList<JMenu>();
		
		menus.add(teamMenu.getConfigMenu());
		menus.add(aiConfigMenu.getConfigMenu());
		menus.add(geomMenu.getConfigMenu());
		
		return menus;
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
	public void onShown()
	{
	}
	
	
	@Override
	public void onHidden()
	{
	}
}
