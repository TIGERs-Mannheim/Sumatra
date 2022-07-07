/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.views;

import edu.tigers.moduli.IModuliStateObserver;
import edu.tigers.moduli.listenerVariables.ModulesState;

import java.util.List;


public interface ISumatraPresenter extends IModuliStateObserver
{
	@Override
	default void onModuliStateChanged(ModulesState state)
	{
		if (state == ModulesState.ACTIVE)
		{
			onStart();
			onStartModuli();
		} else if (state == ModulesState.RESOLVED)
		{
			onStopModuli();
			onStop();
		}
	}

	/**
	 * Moduli module (live mode) is started.
	 */
	default void onStartModuli()
	{
	}


	/**
	 * Counterpart to {@link #onStartModuli()}
	 */
	default void onStopModuli()
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
