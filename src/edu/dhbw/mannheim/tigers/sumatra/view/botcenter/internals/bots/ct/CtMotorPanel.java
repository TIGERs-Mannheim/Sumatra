/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 23.08.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.ct;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ct.CTPIDHistory;

/**
 * CT bot motor telemetry panel
 * 
 * @author AndreR
 * 
 */
public class CtMotorPanel extends JPanel
{
	private static final long	serialVersionUID	= 6394866812639870899L;
	
	private CtMotorPlot left;
	private CtMotorPlot right;
	
	public CtMotorPanel()
	{
		setLayout(new MigLayout("fill", "", ""));
		
		left = new CtMotorPlot("Motor left");
		right = new CtMotorPlot("Motor right");
		
		add(left, "grow");
		add(right, "grow");
	}

	public void setData(CTPIDHistory data)
	{
		if(data.getMotorId() > 1)
		{
			return;
		}
		
		CtMotorPlot plot = null;
		
		if(data.getMotorId() == 0)
		{
			plot = left;
		}
		
		if(data.getMotorId() == 1)
		{
			plot = right;
		}
		
		plot.addSetpoint(data.getTarget());
		plot.addCurrent(data.getCurrent());
		plot.addpValue(data.getError());
		plot.addiValue(data.getIntegral());
		plot.adddValue(data.getDerivative());
	}
}
