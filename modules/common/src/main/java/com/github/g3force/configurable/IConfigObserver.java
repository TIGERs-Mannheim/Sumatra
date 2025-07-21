/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 25.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package com.github.g3force.configurable;

/**
 * Used to observe a config client.
 */
public interface IConfigObserver
{
	default void afterApply(final IConfigClient configClient)
	{
	}
}
