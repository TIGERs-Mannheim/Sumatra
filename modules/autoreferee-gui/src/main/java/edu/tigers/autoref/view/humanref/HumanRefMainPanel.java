/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 24, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.humanref;

import java.awt.*;

import javax.swing.*;

import edu.tigers.autoref.view.humanref.driver.BaseHumanRefViewDriver;
import edu.tigers.autoref.view.humanref.driver.IHumanRefViewDriver;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * @author "Lukas Magel"
 */
public class HumanRefMainPanel extends JPanel implements IHumanRefPanel, ISumatraView
{
	
	/**  */
	private static final long					serialVersionUID	= 6993511173113917503L;
	
	private EPanelType							currentPanelType;
	private transient IHumanRefViewDriver	driver;
	
	
	/**
	 * Create a new instance
	 */
	public HumanRefMainPanel()
	{
		BaseHumanRefPanel basePanel = new BaseHumanRefPanel();
		driver = new BaseHumanRefViewDriver(basePanel);
		
		setupGUI();
	}
	
	
	private void setupGUI()
	{
		setPanelType(EPanelType.BASE);
	}
	
	
	@Override
	public IHumanRefViewDriver getDriver()
	{
		return driver;
	}
	
	
	@Override
	public EPanelType getPanelType()
	{
		return currentPanelType;
	}
	
	
	@Override
	public void setPanelType(final EPanelType panelType)
	{
		BaseHumanRefPanel panel;
		
		if (driver != null)
		{
			driver.stop();
		}
		
		switch (panelType)
		{
			case ACTIVE:
				panel = new ActiveHumanRefPanel();
				break;
			
			case BASE:
				panel = new BaseHumanRefPanel();
				break;
			
			case PASSIVE:
				panel = new PassiveHumanRefPanel();
				break;
			
			default:
				throw new IllegalArgumentException("Please add enum constant " + panelType + " to this switch case!");
		}
		
		driver = panel.createDriver();
		
		removeAll();
		add(panel, BorderLayout.CENTER);
		
		driver.start();
		currentPanelType = panelType;
	}
	
}
