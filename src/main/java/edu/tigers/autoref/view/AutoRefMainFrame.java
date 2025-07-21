/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.view;

import edu.tigers.sumatra.AMainFrame;
import edu.tigers.sumatra.model.SumatraModel;

import javax.swing.WindowConstants;


public class AutoRefMainFrame extends AMainFrame
{
	public AutoRefMainFrame()
	{
		setTitle("TIGERs AutoReferee " + SumatraModel.getVersion());
		setIconImage("/whistle.png");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		addMenuItems();
	}
}
