/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.01.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.referee;

import java.awt.Component;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;


/**
 * Referee view.
 * 
 * @author Malte, DionH, FriederB
 * 
 */
public class RefereePanel extends JPanel implements ISumatraView
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long				serialVersionUID	= 5362158568331526086L;
	
	// constants
	private static final String			TITLE					= "Referee";
	private static final int				ID						= 6;
	
	private final ShowRefereeMsgPanel	showRefereeMsgPanel;
	private final CreateRefereeMsgPanel	createRefereeMsgPanel;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public RefereePanel()
	{
		setLayout(new MigLayout("wrap 1", "[grow, fill]", ""));
		JTabbedPane tabs = new JTabbedPane();
		showRefereeMsgPanel = new ShowRefereeMsgPanel();
		createRefereeMsgPanel = new CreateRefereeMsgPanel();
		tabs.addTab("Messages", showRefereeMsgPanel);
		tabs.addTab("Create own", createRefereeMsgPanel);
		this.add(tabs);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public void start()
	{
		showRefereeMsgPanel.init();
		createRefereeMsgPanel.init();
	}
	
	
	/**
	 */
	public void stop()
	{
		showRefereeMsgPanel.deinit();
		createRefereeMsgPanel.deinit();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the showRefereeMsgPanel
	 */
	public ShowRefereeMsgPanel getShowRefereeMsgPanel()
	{
		return showRefereeMsgPanel;
	}
	
	
	/**
	 * @return the createRefereeMsgPanel
	 */
	public CreateRefereeMsgPanel getCreateRefereeMsgPanel()
	{
		return createRefereeMsgPanel;
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
		return null;
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
}
