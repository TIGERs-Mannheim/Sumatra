/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.timer;

import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;


/**
 * Timer main panel
 */
public class TimerPanel extends JPanel implements ISumatraView
{
	private static final long		serialVersionUID	= -4840668605222003132L;

	private final TimerChartPanel	chartPanel;


	public TimerPanel()
	{
		setLayout(new MigLayout("fill, inset 0", "", ""));

		chartPanel = new TimerChartPanel();
		chartPanel.setVisible(false);

		add(chartPanel, "grow");
	}


	@Override
	public void onShown()
	{
		chartPanel.setVisible(true);
	}


	@Override
	public void onFocused()
	{
		chartPanel.setVisible(true);
	}


	/**
	 * @return
	 */
	public TimerChartPanel getChartPanel()
	{
		return chartPanel;
	}
}
