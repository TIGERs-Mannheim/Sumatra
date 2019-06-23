/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.07.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

/**
 * @author JulianT
 */
public interface IMultiTeamMessageProducer
{
	/**
	 * @param consumer
	 */
	void addMultiTeamMessageConsumer(IMultiTeamMessageConsumer consumer);
}
