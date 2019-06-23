/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.testplays;

/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
@FunctionalInterface
public interface ITestPlayDataObserver {

	/**
	 * Called every time the managed data changes.
	 */
	void onTestPlayDataUpdate();

}
