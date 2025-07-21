/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.moduli.ModulesState;
import edu.tigers.sumatra.views.ASumatraView;

import java.util.List;


public abstract class AModuliMainPresenter<T extends AMainFrame> extends AMainPresenter<T>
{
	protected AModuliMainPresenter(T mainFrame, List<ASumatraView> views, String name)
	{
		super(mainFrame, views, name);
	}


	private void moduliStateChanged(ModulesState oldState, ModulesState newState)
	{
		if (newState == ModulesState.ACTIVE)
		{
			onModuliStarted();
			getViews().forEach(ASumatraView::onModuliStarted);
			getViews().forEach(ASumatraView::start);
		} else if (newState == ModulesState.RESOLVED && oldState == ModulesState.ACTIVE)
		{
			onModuliStopped();
			getViews().forEach(ASumatraView::onModuliStopped);
			getViews().forEach(ASumatraView::stop);
		}
	}


	protected void onModuliStarted()
	{
		// can be overridden by subclasses
	}


	protected void onModuliStopped()
	{
		// can be overridden by subclasses
	}


	protected void init()
	{
		SumatraModel.getInstance().getModulesState().subscribe(getClass().getCanonicalName(), this::moduliStateChanged);
	}
}
