/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.botcenter.bots;

import javax.swing.JPanel;

import edu.tigers.sumatra.view.botcenter.BcBotPingPanel;
import net.miginfocom.swing.MigLayout;


/**
 * Combines move, kick, and ping panels.
 * 
 * @author AndreR
 */
public class ManualControlPanel extends JPanel
{
	/**  */
	private static final long			serialVersionUID		= 7379644742395703062L;
	
	private MotorInputPanel				inputPanel				= null;
	private MotorEnhancedInputPanel	enhancedInputPanel	= null;
	private final KickerFirePanel		kickerFirePanel		= new KickerFirePanel();
	private BcBotPingPanel				pingPanel				= new BcBotPingPanel();
	
	
	/** Constructor. */
	public ManualControlPanel()
	{
		inputPanel = new MotorInputPanel();
		enhancedInputPanel = new MotorEnhancedInputPanel();
		
		setLayout(new MigLayout("", "[]50[]"));
		add(enhancedInputPanel, "aligny top, spany 2");
		add(kickerFirePanel, "wrap");
		add(pingPanel, "wrap");
		add(inputPanel);
	}
	
	
	/**
	 * @return the kickerFirePanel
	 */
	public KickerFirePanel getKickerFirePanel()
	{
		return kickerFirePanel;
	}
	
	
	/**
	 * @return the inputPanel
	 */
	public MotorInputPanel getInputPanel()
	{
		return inputPanel;
	}
	
	
	/**
	 * @return the enhancedInputPanel
	 */
	public MotorEnhancedInputPanel getEnhancedInputPanel()
	{
		return enhancedInputPanel;
	}
	
	
	/**
	 * @return the pingPanel
	 */
	public BcBotPingPanel getPingPanel()
	{
		return pingPanel;
	}
}