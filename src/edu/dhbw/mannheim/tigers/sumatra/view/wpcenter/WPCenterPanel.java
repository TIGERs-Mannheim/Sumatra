/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.wpcenter;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;


// import edu.dhbw.mannheim.tigers.sumatra.view.wpcenter.internals.ABCPanel;


/**
 * This is the main panel of the ai view in sumatra.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 * 
 */
public class WPCenterPanel extends JPanel implements ISumatraView
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final long	serialVersionUID	= 8132550010453691515L;
	
	private JPanel					mainPanel			= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public WPCenterPanel()
	{
		setLayout(new MigLayout("fill, insets 0", "[]", ""));
		
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new MigLayout("fill"));
		mainPanel.add(new JPanel());
		
		add(mainPanel, "grow 200");
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Removes the current JPanel and adds the paramter.
	 * @param chart
	 */
	public void setMainPanel(JPanel chart)
	{
		mainPanel.remove(0);
		mainPanel.add(chart, "grow");
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
