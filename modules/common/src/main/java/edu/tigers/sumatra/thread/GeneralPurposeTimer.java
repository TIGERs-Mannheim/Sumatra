package edu.tigers.sumatra.thread;

import java.util.Timer;


/**
 * This general purpose timer is intended for tasks
 * that need to be executed regularly but without high
 * demands on the required precision.
 * Use this singleton to keep the overall thread count low.
 * 
 * @author AndreR
 */
public final class GeneralPurposeTimer extends Timer
{
	private static final GeneralPurposeTimer	instance	= new GeneralPurposeTimer();
	
	
	private GeneralPurposeTimer()
	{
		super("GeneralPurposeTimer");
	}
	
	
	/**
	 * @return
	 */
	public static GeneralPurposeTimer getInstance()
	{
		return instance;
	}
}
