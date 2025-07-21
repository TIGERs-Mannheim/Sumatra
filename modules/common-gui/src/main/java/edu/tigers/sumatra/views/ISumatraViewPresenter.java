/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.views;

import java.awt.Component;


/**
 * All presenters of SumatraViews must implement this
 */
public interface ISumatraViewPresenter extends ISumatraPresenter
{
	/**
	 * Get the sumatra view panel associated with this presenter
	 *
	 * @return
	 */
	Component getViewPanel();

	/**
	 * Called if the view is shown.
	 * You may use this function together with onHidden for
	 * e.g. controlling a rendering loop.
	 */
	default void onShown()
	{
	}


	/**
	 * Called if the view is hidden.
	 */
	default void onHidden()
	{
	}
}
