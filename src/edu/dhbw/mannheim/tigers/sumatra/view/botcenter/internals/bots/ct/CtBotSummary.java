/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.08.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.ct;

import javax.swing.JPanel;

import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.BaseSummary;

import net.miginfocom.swing.MigLayout;

/**
 * CT bot summary for the OverviewPanel.
 * 
 * @author AndreR
 * 
 */
public class CtBotSummary extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= 7319986200026534239L;
	
	private BaseSummary basePanel;
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public CtBotSummary()
	{
		setLayout(new MigLayout("fill", ""));
		
		basePanel = new BaseSummary("CT Bot");
		add(basePanel, "grow");
		
//		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
//		setBorder(BorderFactory.createLineBorder(Color.BLACK));
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public BaseSummary getBaseSummary()
	{
		return basePanel;
	}
}
