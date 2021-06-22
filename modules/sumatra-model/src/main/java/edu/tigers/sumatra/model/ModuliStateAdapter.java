/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.model;

import edu.tigers.moduli.IModuliStateObserver;
import edu.tigers.moduli.Moduli;
import edu.tigers.moduli.listenerVariables.ModulesState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Little helper singleton to observer the moduli state.
 * Useful in setting up a presenter.
 *
 * @author AndreR
 */
public final class ModuliStateAdapter implements PropertyChangeListener
{
	private static final Logger log = LogManager.getLogger(ModuliStateAdapter.class.getName());
	private final Moduli model = SumatraModel.getInstance();
	private static ModuliStateAdapter instance = null;
	private final List<IModuliStateObserver> observers = new CopyOnWriteArrayList<>();


	private ModuliStateAdapter()
	{
		model.getModulesState().addChangeListener(this);
	}


	public static synchronized ModuliStateAdapter getInstance()
	{
		if (instance == null)
		{
			instance = new ModuliStateAdapter();
		}

		return instance;
	}


	public void addObserver(final IModuliStateObserver o)
	{
		observers.add(o);
		o.onModuliStateChanged(SumatraModel.getInstance().getModulesState().get());
	}


	public void removeObserver(final IModuliStateObserver o)
	{
		observers.remove(o);
	}


	/**
	 * Receive an event if property "modulesState" will be changed.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt)
	{
		// --- property "stateApplication" ---
		if (evt.getSource() == model.getModulesState())
		{

			final ModulesState newState = (ModulesState) evt.getNewValue();

			observers.stream().parallel().forEach(o -> {
				try
				{
					log.trace("Notify {}", o.getClass());
					o.onModuliStateChanged(newState);
				} catch (Exception err)
				{
					log.error("Exception while changing moduli state in class " + o.getClass().getName(), err);
				}
			});
		}
	}
}
