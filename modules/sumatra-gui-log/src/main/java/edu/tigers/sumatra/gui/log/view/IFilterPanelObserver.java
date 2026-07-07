package edu.tigers.sumatra.gui.log.view;

import java.util.List;


/**
 * Observes user filter changes.
 */
public interface IFilterPanelObserver
{
	/**
	 * @param allowed
	 */
	void onNewFilter(List<String> allowed);


	/**
	 * @param freeze
	 */
	void onFreeze(boolean freeze);
}
