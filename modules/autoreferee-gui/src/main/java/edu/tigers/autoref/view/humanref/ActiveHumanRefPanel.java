/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 26, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.humanref;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.tigers.autoref.view.humanref.components.FollowUpPanel;
import edu.tigers.autoref.view.humanref.components.GameEventPanel;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.events.IGameEvent;


/**
 * @author "Lukas Magel"
 */
public class ActiveHumanRefPanel extends BaseHumanRefPanel
{
	
	/**  */
	private static final long	serialVersionUID	= 6755612406384992226L;
	
	private GameEventPanel		eventPanel;
	private FollowUpPanel		followUpPanel;
	
	
	/**
	 * 
	 */
	public ActiveHumanRefPanel()
	{
		
	}
	
	
	@Override
	protected void setupGUI()
	{
		eventPanel = new GameEventPanel(largeFont, regularFont);
		followUpPanel = new FollowUpPanel(regularFont);
		
		super.setupGUI();
	}
	
	
	@Override
	protected void fillVerticalLayout()
	{
		super.fillVerticalLayout();
		fillPanel();
	}
	
	
	@Override
	protected void fillHorizontalLayout()
	{
		super.fillHorizontalLayout();
		fillPanel();
	}
	
	
	private void fillPanel()
	{
		eventPanel.clear();
		
		followUpPanel.clear();
		
		JPanel panel = getContentPanel();
		panel.setLayout(new MigLayout("fillx, ins 5", "[fill]"));
		panel.add(eventPanel, "wrap 3%");
		panel.add(followUpPanel, "wrap");
	}
	
	
	/**
	 * @param event
	 */
	public void setEvent(final IGameEvent event)
	{
		eventPanel.setEvent(event);
	}
	
	
	/**
	 * 
	 */
	public void clearEvent()
	{
		eventPanel.clear();
	}
	
	
	/**
	 * @param action
	 */
	public void setNextAction(final FollowUpAction action)
	{
		followUpPanel.setFollowUp(action);
	}
	
	
	/**
	 * 
	 */
	public void clearFollowUp()
	{
		followUpPanel.clear();
	}
}
