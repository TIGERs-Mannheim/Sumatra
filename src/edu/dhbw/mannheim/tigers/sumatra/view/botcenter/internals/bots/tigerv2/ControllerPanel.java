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
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.MotorEnhancedInputPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.MotorInputPanel;


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
	private static final long			serialVersionUID	= -1285004577489986788L;
	
	private FusionCtrlPanel				fusion				= null;
	private SelectControllerPanel		select				= null;
	private MotorInputPanel				input					= null;
	private MotorEnhancedInputPanel	enhanced				= null;
	
	
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
		input = new MotorInputPanel(false);
		enhanced = new MotorEnhancedInputPanel();
		
		add(fusion, "spany 3, aligny top");
		add(select, "wrap, aligny top");
		add(input, "wrap");
		add(enhanced);
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
	 * @return the input
	 */
	public MotorInputPanel getInputPanel()
	{
		return input;
	}
	
	
	/**
	 * @return the enhanced
	 */
	public MotorEnhancedInputPanel getEnhancedInputPanel()
	{
		return enhanced;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
