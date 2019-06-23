/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.05.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.skilltester;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.csvexporter.CSVExporter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillSystemObserver;


/**
 * this is a test role which can be used to test individual skills.
 * you can program a defined sequence of skill that should be executed one after another
 * each skill has to complete before the next gets started
 * 
 * This role has been developed to realize a test procedure for skills. It is recommended to use it
 * in conjunction with the {@link CSVExporter}.
 * 
 * @author DanielW
 * 
 */
public class WPTester_init extends WPTester implements ISkillSystemObserver
{
	/**  */
	private static final long	serialVersionUID	= 183342420117756234L;
	

	public WPTester_init()
	{
		float waypoints[][] = {
				{2200.0f,    1500.0f, 0.0f},
				
		};
		
		this.waypoints = waypoints;
	}
	


}
