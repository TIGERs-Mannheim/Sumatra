/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.views;

import edu.tigers.sumatra.model.ModuliStateAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.infonode.docking.View;

import java.awt.Component;


/**
 * Base class for any Sumatra View (the tabs in the GUI).
 */
@Log4j2
@RequiredArgsConstructor
public abstract class ASumatraView
{
	private final ESumatraViewType type;
	private ISumatraViewPresenter presenter = null;
	private View view = null;
	private EViewMode mode = EViewMode.NORMAL;

	/**
	 * The view mode
	 */
	public enum EViewMode
	{
		NORMAL,
		REPLAY,
	}


	/**
	 * Create presenter if not already done yet
	 */
	public final void ensureInitialized()
	{
		if (presenter == null)
		{
			log.trace("Creating presenter for view " + type.getTitle());
			presenter = createPresenter();
			getView().setComponent(presenter.getComponent());
			if (mode == EViewMode.NORMAL)
			{
				ModuliStateAdapter.getInstance().addObserver(presenter);
			}
			log.trace("Presenter created for view " + type.getTitle());
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
		return presenter.getComponent();
	}


	/**
	 * @return
	 */
	public final synchronized ISumatraView getSumatraView()
	{
		ensureInitialized();
		return presenter.getSumatraView();
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
	 * Create your presenter here. This will be called at the right time and only once
	 *
	 * @return
	 */
	protected abstract ISumatraViewPresenter createPresenter();


	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------


	/**
	 * @return the type
	 */
	public final ESumatraViewType getType()
	{
		return type;
	}


	/**
	 * @return the presenter
	 */
	public final ISumatraViewPresenter getPresenter()
	{
		return presenter;
	}


	// String builder more readable
	@SuppressWarnings("StringBufferReplaceableByString")
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ASumatraView [type=");
		builder.append(type);
		builder.append(", isInit=");
		builder.append(isInitialized());
		builder.append("]");
		return builder.toString();
	}


	/**
	 * @return the mode
	 */
	public final EViewMode getMode()
	{
		return mode;
	}


	/**
	 * @param mode the mode to set
	 */
	public final void setMode(final EViewMode mode)
	{
		this.mode = mode;
	}
}
