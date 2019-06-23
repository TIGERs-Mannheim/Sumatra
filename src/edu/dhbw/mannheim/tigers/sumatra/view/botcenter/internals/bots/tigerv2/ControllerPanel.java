/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.05.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;


/**
 * Configure and use the controller on board.
 * 
 * @author AndreR
 * 
 */
public class ControllerPanel extends JPanel
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long		serialVersionUID	= -1285004577489986788L;
	
	private FusionCtrlPanel			fusion				= null;
	private SelectControllerPanel	select				= null;
	private StructurePanel			structure			= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	  * 
	  */
	public ControllerPanel()
	{
		setLayout(new MigLayout("", "", ""));
		
		fusion = new FusionCtrlPanel();
		select = new SelectControllerPanel();
		structure = new StructurePanel();
		
		add(select, "wrap");
		add(structure, "wrap");
		add(fusion, "wrap");
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return the fusion
	 */
	public FusionCtrlPanel getFusionPanel()
	{
		return fusion;
	}
	
	
	/**
	 * @return the select
	 */
	public SelectControllerPanel getSelectControllerPanel()
	{
		return select;
	}
	
	
	/**
	 * @return the structure
	 */
	public final StructurePanel getStructurePanel()
	{
		return structure;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
