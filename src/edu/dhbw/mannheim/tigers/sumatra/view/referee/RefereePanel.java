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

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * Referee view.
 * 
 * @author Malte, DionH, FriederB
 * 
 */
public class RefereePanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 5362158568331526086L;


	private ShowRefereeMsgPanel showRefereeMsgPanel;
	private CreateRefereeMsgPanel createRefereeMsgPanel;

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public RefereePanel()
	{	
		this.setLayout(new MigLayout("wrap 2", "[grow, fill]", ""));
		showRefereeMsgPanel = new ShowRefereeMsgPanel();
		createRefereeMsgPanel = new CreateRefereeMsgPanel();
		this.add(showRefereeMsgPanel, "");
		this.add(createRefereeMsgPanel, "");
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void start()
	{
		showRefereeMsgPanel.start();
		createRefereeMsgPanel.start();
	}
	
	public void stop()
	{
		this.removeAll();
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
}
