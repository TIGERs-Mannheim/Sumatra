/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.05.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.kicker;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.KickerAutoloadPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.KickerAutoloadPanel.IKickerAutoloadPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.KickerPlotPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.kicker.KickerFirePanelV2.IKickerFirePanelObserverV2;


/**
 * Kicker panel
 * 
 * @author AndreR
 * 
 */
public class KickerPanelV2 extends JPanel
{
	/**
	 */
	public interface IKickerPanelV2Observer extends IKickerFirePanelObserverV2, IKickerAutoloadPanelObserver
	{
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long							serialVersionUID	= -8425791161859489657L;
	
	private final KickerStatusPanelV2				statusPanel;
	private final KickerPlotPanel						plotPanel;
	private final KickerFirePanelV2					firePanel;
	private final KickerAutoloadPanel				chargeAutoPanel;
	
	private final List<IKickerPanelV2Observer>	observers			= new ArrayList<IKickerPanelV2Observer>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public KickerPanelV2()
	{
		setLayout(new MigLayout("fill"));
		
		statusPanel = new KickerStatusPanelV2();
		plotPanel = new KickerPlotPanel(250.0f);
		firePanel = new KickerFirePanelV2();
		chargeAutoPanel = new KickerAutoloadPanel();
		
		add(firePanel);
		add(chargeAutoPanel, "gapleft 20");
		add(statusPanel, "gapleft 20");
		add(Box.createGlue(), "wrap, pushx");
		add(plotPanel, "grow, span, pushy");
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(IKickerPanelV2Observer observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
			firePanel.addObserver(observer);
			chargeAutoPanel.addObserver(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(IKickerPanelV2Observer observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
			firePanel.removeObserver(observer);
			chargeAutoPanel.removeObserver(observer);
		}
	}
	
	
	/**
	 * @return
	 */
	public KickerStatusPanelV2 getStatusPanel()
	{
		return statusPanel;
	}
	
	
	/**
	 * @return
	 */
	public KickerPlotPanel getPlotPanel()
	{
		return plotPanel;
	}
}
