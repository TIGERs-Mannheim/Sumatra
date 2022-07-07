/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.views;

import javax.swing.JMenu;
import java.awt.Component;
import java.util.List;


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
	 * You may return a list of JMenus here. They are dynamically added
	 * to the main MenuBar if the view is shown.
	 *
	 * @return Optional JMenus or null if not used.
	 */
	default List<JMenu> getCustomMenus()
	{
		return List.of();
	}

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


	/**
	 * Called if the view got focused.
	 */
	default void onFocused()
	{
	}


	/**
	 * Called if the view lost the focus.
	 */
	default void onFocusLost()
	{
	}
}
