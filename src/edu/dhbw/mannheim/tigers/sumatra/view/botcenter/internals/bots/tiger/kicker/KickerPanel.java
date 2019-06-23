/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.09.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.KickerAutoloadPanel.IKickerAutoloadPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.KickerChargeManualPanel.IKickerChargeManualObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.KickerFirePanel.IKickerFirePanelObserver;

/**
 * Kicker panel
 * 
 * @author AndreR
 * 
 */
public class KickerPanel extends JPanel
{
	public interface IKickerPanelObserver extends IKickerFirePanelObserver,
		IKickerChargeManualObserver, IKickerAutoloadPanelObserver
	{
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -8425791161859489657L;

	private KickerStatusPanel statusPanel;
	private KickerPlotPanel plotPanel;
	private KickerFirePanel firePanel;
	private KickerChargeManualPanel chargeManualPanel;
	private KickerAutoloadPanel chargeAutoPanel;
	
	private final List<IKickerPanelObserver> observers = new ArrayList<IKickerPanelObserver>();
	
		// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public KickerPanel()
	{
		setLayout(new MigLayout("fill"));
		
		statusPanel = new KickerStatusPanel();
		plotPanel = new KickerPlotPanel();
		firePanel = new KickerFirePanel();
		chargeManualPanel = new KickerChargeManualPanel();
		chargeAutoPanel = new KickerAutoloadPanel();
		
		add(firePanel);
		add(chargeManualPanel, "gapleft 20");
		add(chargeAutoPanel, "gapleft 20");
		add(statusPanel, "gapleft 20");
		add(Box.createGlue(), "wrap, pushx");
		add(plotPanel, "grow, span, pushy");
	}	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void addObserver(IKickerPanelObserver observer)
	{
		synchronized(observers)
		{
			observers.add(observer);
			firePanel.addObserver(observer);
			chargeManualPanel.addObserver(observer);
			chargeAutoPanel.addObserver(observer);
		}
	}
	
	public void removeObserver(IKickerPanelObserver observer)
	{
		synchronized(observers)
		{
			observers.remove(observer);
			firePanel.removeObserver(observer);
			chargeManualPanel.removeObserver(observer);
			chargeAutoPanel.removeObserver(observer);
		}
	}

	public KickerStatusPanel getStatusPanel()
	{
		return statusPanel;
	}
	
	public KickerPlotPanel getPlotPanel()
	{
		return plotPanel;
	}
}
