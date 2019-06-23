/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 1, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.MotorEnhancedInputPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.MotorInputPanel;


/**
 * Includes enhanced motor input panel for bot control
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MovePanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long			serialVersionUID	= -1556982047975006457L;
	private MotorInputPanel				input					= null;
	private MotorEnhancedInputPanel	enhanced				= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public MovePanel()
	{
		input = new MotorInputPanel(false);
		enhanced = new MotorEnhancedInputPanel();
		
		setLayout(new MigLayout());
		add(enhanced, "wrap, aligny top");
		add(input);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
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
