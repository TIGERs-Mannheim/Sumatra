/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 16, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.helpers;

import edu.dhbw.mannheim.tigers.sumatra.util.IInstanceableEnum;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass;


/**
 * TODO FelixB <bayer.fel@gmail.com>, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author FelixB <bayer.fel@gmail.com>
 */
public enum EPointOnLineGetter implements IInstanceableEnum
{
	
	/** */
	HEDGEHOG(new InstanceableClass(HedgehogPointCalc.class)),
	
	/**  */
	PASSINTERSECTION(new InstanceableClass(PassIntersectionPointCalc.class)),
	
	/**  */
	PASSIVEAGRESSIVE(new InstanceableClass(PassiveAgressivePointCalc.class)),
	
	/** */
	ZONEDEFENSE(new InstanceableClass(ZoneDefensePointCalc.class)),
	
	/** */
	RECEIVERBLOCK(new InstanceableClass(ReceiverBlockPointCalc.class))
	
	;
	
	
	private final InstanceableClass	clazz;
	
	
	/**
	 */
	private EPointOnLineGetter(final InstanceableClass clazz)
	{
		this.clazz = clazz;
	}
	
	
	/**
	 * @return the paramImpls
	 */
	@Override
	public final InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
}
