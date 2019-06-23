/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.11.2010
 * Author(s): Administrator
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.wpcenter.internals;

/**
 * Interface for providing a Analasyis Chart Data Observer
 * 
 */
public interface IAnalysisChartsObserver
{
	/**
	 *
	 */
	void onShowX();
	
	
	/**
	 *
	 */
	void onShowY();
	
	
	/**
	 * 
	 */
	void onShowA();
	
	
	/**
	 *
	 */
	void onShowAbs();
	
	
	/**
	 * 
	 * @param id
	 */
	void onSetId(int id);
	
	
	/**
	 * 
	 */
	void onShowBallVel();
	
}
