/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.views;

import edu.tigers.moduli.listenerVariables.ModulesState;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ASumatraViewPresenter implements ISumatraViewPresenter
{
	@Override
	public void onModuliStateChanged(ModulesState state)
	{
		if (state == ModulesState.ACTIVE)
		{
			onStart();
		} else if (state == ModulesState.RESOLVED)
		{
			onStop();
		}
	}
}
