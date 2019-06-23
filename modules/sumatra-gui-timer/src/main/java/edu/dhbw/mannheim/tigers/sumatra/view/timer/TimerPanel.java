/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.09.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.timer;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JPanel;

import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;


/**
 * Timer main panel
 * 
 * @author Gero
 */
public class TimerPanel extends JPanel implements ISumatraView
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long		serialVersionUID	= -4840668605222003132L;
	
	private final TimerChartPanel	chartPanel;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TimerPanel()
	{
		setLayout(new MigLayout("fill, inset 0", "", ""));
		
		chartPanel = new TimerChartPanel();
		chartPanel.setVisible(false);
		
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
		// chartPanel.setVisible(false);
	}
	
	
	@Override
	public void onFocused()
	{
		chartPanel.setVisible(true);
	}
	
	
	@Override
	public void onFocusLost()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public TimerChartPanel getChartPanel()
	{
		return chartPanel;
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		return null;
	}
}
