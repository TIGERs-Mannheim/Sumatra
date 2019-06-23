/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.09.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.timer;

import java.awt.Component;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JPanel;

import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;

import net.miginfocom.swing.MigLayout;


/**
 * Timer main panel
 * 
 * @author Gero
 * 
 */
public class TimerPanel extends JPanel implements ISumatraView
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long		serialVersionUID	= -4840668605222003132L;
	
	private static final int		ID						= 5;
	private static final String	TITLE					= "Timer Info";
	

	private final TimerChartPanel	chartPanel;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TimerPanel()
	{
		setLayout(new MigLayout("fill", "", ""));
		
		chartPanel = new TimerChartPanel();
		
		add(chartPanel, "grow");
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onShown()
	{
		chartPanel.setVisible(true);
	}
	

	@Override
	public void onHidden()
	{
		chartPanel.setVisible(false);
	}
	

	@Override
	public void onFocused()
	{
	}
	

	@Override
	public void onFocusLost()
	{
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public TimerChartPanel getChartPanel()
	{
		return chartPanel;
	}
	

	@Override
	public int getID()
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
}
