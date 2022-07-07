/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.views;

import edu.tigers.sumatra.model.ModuliStateAdapter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import net.infonode.docking.View;

import java.awt.Component;
import java.util.stream.Stream;


/**
 * Base class for any Sumatra View (the tabs in the GUI).
 */
@Log4j2
@Getter
@ToString
@RequiredArgsConstructor
public abstract class ASumatraView
{
	private final ESumatraViewType type;
	private ISumatraViewPresenter presenter;
	private View view = null;
	@Setter
	private EViewMode mode = EViewMode.NORMAL;


	/**
	 * Create presenter if not already done yet
	 */
	public final void ensureInitialized()
	{
		if (presenter == null)
		{
			log.trace("Creating presenter for view {}", type.getTitle());
			presenter = createPresenter();
			getView().setComponent(presenter.getViewPanel());
			if (mode == EViewMode.NORMAL)
			{
				ModuliStateAdapter stateAdapter = ModuliStateAdapter.getInstance();
				presenter.getChildPresenters().forEach(stateAdapter::addObserver);
				stateAdapter.addObserver(presenter);
			}
			log.trace("Presenter created for view {}", type.getTitle());
		}
	}


	/**
	 * The component which is to be displayed in the view panel.
	 * This can be anything that extends from Component (e.g. JPanel).
	 *
	 * @return View component.
	 */
	public final synchronized Component getComponent()
	{
		ensureInitialized();
		return presenter.getViewPanel();
	}


	public final ISumatraViewPresenter getPresenter()
	{
		ensureInitialized();
		return presenter;
	}


	/**
	 * @return the view
	 */
	public final synchronized View getView()
	{
		if (view == null)
		{
			view = new View(getType().getTitle(), new ViewIcon(), null);
		}
		return view;
	}


	/**
	 * Check if both, presenter and view were created
	 *
	 * @return
	 */
	public final synchronized boolean isInitialized()
	{
		return (presenter != null) && (view != null);
	}


	/**
	 * @return a stream of all presenters (the root presenter and all children)
	 */
	public final Stream<ISumatraPresenter> getPresenters()
	{
		ensureInitialized();
		return gatherPresenters(presenter);
	}


	private Stream<ISumatraPresenter> gatherPresenters(ISumatraPresenter viewPresenter)
	{
		Stream<ISumatraPresenter> childPresenterStream = viewPresenter.getChildPresenters().stream()
				.flatMap(this::gatherPresenters);
		return Stream.concat(Stream.of(viewPresenter), childPresenterStream);
	}


	/**
	 * Create your presenter here. This will be called at the right time and only once
	 *
	 * @return
	 */
	protected abstract ISumatraViewPresenter createPresenter();
}
