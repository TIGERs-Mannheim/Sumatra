/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.rcm;

import edu.tigers.sumatra.rcm.RcmAction.EActionType;
import net.java.games.input.Component;


/**
 * Wrapper class with extended information on Component
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ExtComponent implements Component
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Component	baseComponent;
	private final RcmAction	mappedAction;
	private ExtComponent		dependentComp;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param baseComponent
	 * @param mappedAction
	 */
	public ExtComponent(final Component baseComponent, final RcmAction mappedAction)
	{
		this.baseComponent = baseComponent;
		this.mappedAction = mappedAction;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	public boolean isContinuesAction()
	{
		// can potentially be configured in more detail later
		if (mappedAction.getActionType() == EActionType.SIMPLE)
		{
			EControllerAction cAction = (EControllerAction) mappedAction.getActionEnum();
			return cAction.isContinuous();
			
		}
		return false;
	}
	
	
	@Override
	public String toString()
	{
		return baseComponent.getIdentifier().getName() + " - " + mappedAction;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public Identifier getIdentifier()
	{
		return baseComponent.getIdentifier();
	}
	
	
	@Override
	public boolean isRelative()
	{
		return baseComponent.isRelative();
	}
	
	
	@Override
	public boolean isAnalog()
	{
		return baseComponent.isAnalog();
	}
	
	
	@Override
	public float getDeadZone()
	{
		return baseComponent.getDeadZone();
	}
	
	
	@Override
	public float getPollData()
	{
		if (dependentComp != null)
		{
			return baseComponent.getPollData() * dependentComp.getPollData();
		}
		return baseComponent.getPollData();
	}
	
	
	@Override
	public String getName()
	{
		return baseComponent.getName();
	}
	
	
	/**
	 * @return the baseComponent
	 */
	public Component getBaseComponent()
	{
		return baseComponent;
	}
	
	
	/**
	 * @return the mappedAction
	 */
	public RcmAction getMappedAction()
	{
		return mappedAction;
	}
	
	
	/**
	 * @return the dependentComp
	 */
	public final ExtComponent getDependentComp()
	{
		return dependentComp;
	}
	
	
	/**
	 * @param dependentComp the dependentComp to set
	 */
	public final void setDependentComp(final ExtComponent dependentComp)
	{
		this.dependentComp = dependentComp;
	}
}
