/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.views;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import net.infonode.docking.View;

import java.util.stream.Stream;


/**
 * Base class for any Sumatra View (the tabs in the GUI).
 */
@Log4j2
@Getter
@ToString(of = "type")
public abstract class ASumatraView
{
	private final ESumatraViewType type;
	private final View view;
	private ISumatraViewPresenter presenter;

	private boolean started = false;
	private boolean moduliStarted = false;


	protected ASumatraView(ESumatraViewType type)
	{
		this.type = type;
		view = new View(getType().getTitle(), new ViewIcon(), null);
	}


	/**
	 * Create presenter if not already done yet
	 */
	public final synchronized void ensureInitialized()
	{
		if (presenter == null)
		{
			log.trace("Creating presenter for view {}", this);
			presenter = createPresenter();
			getView().setComponent(presenter.getViewPanel());
			if (started)
			{
				log.trace("Notify start to view {}", this);
				presenter.onStart();
				presenter.getChildPresenters().forEach(ISumatraPresenter::onStart);
			}
			if (moduliStarted)
			{
				log.trace("Notify moduli start to view {}", this);
				presenter.onModuliStarted();
				presenter.getChildPresenters().forEach(ISumatraPresenter::onModuliStarted);
			}

			log.trace("Created presenter for view {}", this);
		}
	}


	public synchronized void start()
	{
		if (started)
		{
			log.warn("View {} already started", this);
			return;
		}
		started = true;

		if (presenter != null)
		{
			log.trace("Starting view {}", this);
			presenter.onStart();
			presenter.getChildPresenters().forEach(ISumatraPresenter::onStart);
		}
	}


	public synchronized void stop()
	{
		if (!started)
		{
			log.warn("View {} not started, cannot stop", this);
			return;
		}
		started = false;

		if (presenter != null)
		{
			log.trace("Stopping view {}", this);
			presenter.onStop();
			presenter.getChildPresenters().forEach(ISumatraPresenter::onStop);
		}
	}


	public synchronized void onModuliStarted()
	{
		if (moduliStarted)
		{
			log.warn("View {} already started moduli", this);
			return;
		}
		moduliStarted = true;

		if (presenter != null)
		{
			log.trace("Starting moduli for view {}", this);
			presenter.onModuliStarted();
			presenter.getChildPresenters().forEach(ISumatraPresenter::onModuliStarted);
		}
	}


	public synchronized void onModuliStopped()
	{
		if (!moduliStarted)
		{
			log.warn("View {} not started moduli, cannot stop", this);
			return;
		}
		moduliStarted = false;

		if (presenter != null)
		{
			log.trace("Stopping moduli for view {}", this);
			presenter.onModuliStopped();
			presenter.getChildPresenters().forEach(ISumatraPresenter::onModuliStopped);
		}
	}


	/**
	 * @return a stream of all presenters (the root presenter and all children)
	 */
	public final Stream<ISumatraPresenter> getPresenters()
	{
		ensureInitialized();
		return gatherPresenters(getPresenter());
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
