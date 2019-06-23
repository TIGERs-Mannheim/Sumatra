/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 28, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.event;

import java.util.HashMap;
import java.util.Map;

import com.sleepycat.persist.model.Persistent;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
@Persistent
public class GameEvents
{
	/** This map contains all the registered events */
	public Map<EGameEvent, IGameEventStorage> storedEvents = new HashMap<>();
}
