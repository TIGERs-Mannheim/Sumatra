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

import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.view.aicenter.internals.moduleoverview.ModuleControlPanel;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;


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
	
	private static final long	serialVersionUID	= 8132550010453691515L;
	
	private final JCheckBox		chkAiActive;
	private ModuleControlPanel	modulesPanel		= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public AICenterPanel()
	{
		setLayout(new MigLayout("insets 0 0 0 0"));
		
		final JPanel combinePanel = new JPanel();
		combinePanel.setLayout(new MigLayout("insets 0 0 0 0", "", ""));
		
		chkAiActive = new JCheckBox("AI activated", false);
		modulesPanel = new ModuleControlPanel();
		
		combinePanel.add(chkAiActive, "wrap, top");
		combinePanel.add(modulesPanel, "wrap");
		
		add(combinePanel);
		
		UIManager.put("TabbedPane.tabAreaInsets", new Insets(0, 0, 0, 0));
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
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
	 */
	public void clearView()
	{
		modulesPanel.onStop();
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		final List<JMenu> menus = new ArrayList<JMenu>();
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
	
	
	/**
	 * @return the chkAiActive
	 */
	public final JCheckBox getChkAiActive()
	{
		return chkAiActive;
	}
}
