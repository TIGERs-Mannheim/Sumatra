/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.views;

public abstract class ASumatraPresenter implements ISumatraPresenter
{
	private boolean initialized;


	@Override
	public void onStart()
	{
		if (!initialized)
		{
			initialized = true;
			onInit();
		}
		ISumatraPresenter.super.onStart();
	}


	protected void onInit()
	{
		// can be overwritten
	}
}
