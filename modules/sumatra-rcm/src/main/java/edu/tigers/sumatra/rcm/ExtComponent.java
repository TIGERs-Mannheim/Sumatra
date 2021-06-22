/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.rcm;

import edu.tigers.sumatra.rcm.RcmAction.EActionType;
import lombok.RequiredArgsConstructor;
import net.java.games.input.Component;


/**
 * Wrapper class with extended information on Component
 */
@RequiredArgsConstructor
public class ExtComponent implements Component
{
	private final Component baseComponent;
	private final RcmAction mappedAction;
	private ExtComponent dependentComp;


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
