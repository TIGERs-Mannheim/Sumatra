/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.botcenter.view.bots;

import edu.tigers.sumatra.gui.botcenter.view.BcBotPingPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;


/**
 * Combines move, kick, and ping panels.
 *
 * @author AndreR
 */
public class ManualControlPanel extends JPanel
{
	private static final long serialVersionUID = 7379644742395703062L;

	private BcBotPingPanel pingPanel = new BcBotPingPanel();


	public ManualControlPanel()
	{
		setLayout(new MigLayout("", "[]50[]"));
		add(pingPanel, "wrap");
	}


	/**
	 * @return the pingPanel
	 */
	public BcBotPingPanel getPingPanel()
	{
		return pingPanel;
	}
}
