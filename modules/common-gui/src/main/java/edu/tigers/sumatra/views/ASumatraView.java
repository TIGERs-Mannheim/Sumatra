/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 19, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.views;

import java.awt.Component;

import org.apache.log4j.Logger;

import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.model.ModuliStateAdapter;
import edu.tigers.sumatra.model.SumatraModel;
import net.infonode.docking.View;


/**
 * Base class for any Sumatra View (the tabs in the GUI)
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ASumatraView
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger log = Logger.getLogger(ASumatraView.class.getName());
	private final ESumatraViewType type;
	private ISumatraViewPresenter presenter = null;
	private View view = null;
	private EViewMode mode = EViewMode.NORMAL;
	
	/**
	 * The view mode
	 */
	public enum EViewMode
	{
		/**  */
		NORMAL,
		/**  */
		REPLAY
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param type
	 */
	public ASumatraView(final ESumatraViewType type)
	{
		this.type = type;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
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
				// let the presenter update itself according to the current moduli state
				ModulesState currentState = SumatraModel.getInstance().getModulesState().get();
				presenter.onModuliStateChanged(currentState);
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
