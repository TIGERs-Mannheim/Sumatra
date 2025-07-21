/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.views;

import java.util.List;


public interface ISumatraPresenter
{
	/**
	 * Moduli module (live mode) is started.
	 */
	default void onModuliStarted()
	{
	}


	/**
	 * Counterpart to {@link #onModuliStarted()}
	 */
	default void onModuliStopped()
	{
	}


	/**
	 * Initialize presenter without registering any moduli modules (for replay mode)
	 */
	default void onStart()
	{
	}

	/**
	 * Counterpart to {@link #onStart()}
	 */
	default void onStop()
	{
	}

	/**
	 * @return a list of child presenters
	 */
	default List<ISumatraPresenter> getChildPresenters()
	{
		return List.of();
	}
}
