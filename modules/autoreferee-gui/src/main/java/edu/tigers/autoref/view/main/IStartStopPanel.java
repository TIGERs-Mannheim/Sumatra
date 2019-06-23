/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.view.main;

import edu.tigers.autoref.view.main.IStartStopPanel.IStartStopPanelObserver;
import edu.tigers.autoreferee.engine.IAutoRefEngine.AutoRefMode;
import edu.tigers.autoreferee.module.AutoRefState;
import edu.tigers.sumatra.components.IBasePanel;


/**
 * @author "Lukas Magel"
 */
public interface IStartStopPanel extends IBasePanel<IStartStopPanelObserver>
{
	
	/**
	 * @author Lukas Magel
	 */
	interface IStartStopPanelObserver
	{
		/**
		 * When the start button is pressed
		 */
		void onStartButtonPressed();
		
		
		/**
		 * When the stop button is pressed
		 */
		void onStopButtonPressed();
		
		
		/**
		 * When the pause button is pressed
		 */
		void onPauseButtonPressed();
		
		
		/**
		 * When the resume button is pressed
		 */
		void onResumeButtonPressed();
		
	}
	
	
	/**
	 * @param state
	 */
	void setState(final AutoRefState state);
	
	
	/**
	 * @return
	 */
	AutoRefMode getModeSetting();
	
	
	/**
	 * @param mode
	 */
	void setModeSetting(AutoRefMode mode);
}
