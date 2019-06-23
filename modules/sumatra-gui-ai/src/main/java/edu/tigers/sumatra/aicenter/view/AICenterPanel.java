/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * *********************************************************
 */
package edu.tigers.sumatra.aicenter.view;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JPanel;

import edu.tigers.sumatra.views.ISumatraView;


/**
 * This is the main panel of the ai view in sumatra.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class AICenterPanel extends JPanel implements ISumatraView
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long	serialVersionUID	= 8132550010453691515L;
	
	private ModuleControlPanel	modulesPanel		= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public AICenterPanel()
	{
		setLayout(new BorderLayout());
		
		modulesPanel = new ModuleControlPanel();
		add(modulesPanel, BorderLayout.CENTER);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
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
		return modulesPanel.getChkAiActive();
	}
}
