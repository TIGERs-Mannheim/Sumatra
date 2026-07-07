package edu.tigers.autoref.view.ballspeed;

/**
 * @author "Lukas Magel"
 */
public interface IBallSpeedPanelListener
{
	/**
	 * 
	 */
	void pauseButtonPressed();
	
	
	/**
	 * 
	 */
	void resumeButtonPressed();
	
	
	/**
	 * @param value
	 */
	void stopChartValueChanged(boolean value);
	
	
	/**
	 * @param value new value in [s]
	 */
	void timeRangeSliderValueChanged(int value);
}
