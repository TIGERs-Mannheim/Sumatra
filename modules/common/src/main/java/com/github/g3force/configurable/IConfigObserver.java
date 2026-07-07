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
