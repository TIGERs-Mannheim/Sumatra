package edu.tigers.sumatra;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.moduli.ModulesState;
import edu.tigers.sumatra.views.SumatraView;

import java.util.List;


public abstract class AModuliMainPresenter<T extends AMainFrame> extends AMainPresenter<T>
{
	protected AModuliMainPresenter(T mainFrame, List<SumatraView> views, String name)
	{
		super(mainFrame, views, name);
	}


	private void moduliStateChanged(ModulesState oldState, ModulesState newState)
	{
		if (newState == ModulesState.ACTIVE)
		{
			onModuliStarted();
			getViews().forEach(SumatraView::onModuliStarted);
			getViews().forEach(SumatraView::start);
		} else if (newState == ModulesState.RESOLVED && oldState == ModulesState.ACTIVE)
		{
			onModuliStopped();
			getViews().forEach(SumatraView::onModuliStopped);
			getViews().forEach(SumatraView::stop);
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
