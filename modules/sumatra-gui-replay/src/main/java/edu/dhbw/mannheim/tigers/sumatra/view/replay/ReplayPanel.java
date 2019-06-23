/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.replay;

import java.util.Collections;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JPanel;

import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReplayPanel extends JPanel implements ISumatraView
{
	private static final long			serialVersionUID	= 2558215591773007411L;
	
	private final ReplayControlPanel	controlPanel		= new ReplayControlPanel();
	
	
	/**
	 * 
	 */
	public ReplayPanel()
	{
		setLayout(new MigLayout("fill"));
		add(controlPanel, "wrap");
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		return Collections.emptyList();
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
	
	
	/**
	 * @return the controlPanel
	 */
	public final ReplayControlPanel getControlPanel()
	{
		return controlPanel;
	}
}
