/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.clock;


import org.apache.log4j.Logger;


/**
 * <p>
 * [de] <b>Siebenschlaefer</b>
 * </p>
 * <p>
 * <i> Dormice are rodents of the family Gliridae. (This family is also variously called Myoxidae or Muscardinidae by
 * different taxonomists). Dormice are mostly found in Europe, although some live in Africa and Asia. <b>They are
 * particularly known for their long periods of hibernation.</b> Because only one species of dormouse is native to the
 * British Isles, in everyday English usage "dormouse" usually refers to this species (the Hazel Dormouse) rather than
 * to the family as a whole.<br/>
 * (from <a href="http://en.wikipedia.org/wiki/Dormouse">Wikipedia</a>) </i>
 * </p>
 * <p>
 * This class does nothing else then sleeping the whole time. And doing so, believe it or not, it stabilizes the system
 * clock on Windows systems, while interrupt time is held down on 1ms during its sleep, making calls to
 * <code>Thread.sleep(milli < 15ms)</code> possible/reasonable (and simulation stable, fluid and smooth!=) ).
 * </p>
 * <p>
 * I know, sounds a bit esoteric at first, but comes very handy while it's just working!!! Found <a
 * href="http://www.javamex.com/tutorials/threads/sleep_issues.shtml#bugs">here</a>
 * </p>
 * 
 * @author Gero
 */
public final class Dormouse implements Runnable
{
	// Logger
	private static final Logger		log		= Logger.getLogger(Dormouse.class.getName());
	
	private static volatile Dormouse	instance	= null;
	
	
	private Dormouse()
	{
		
	}
	
	
	/**
	 * @return
	 */
	public static Dormouse getInstance()
	{
		if (instance == null)
		{
			instance = new Dormouse();
		}
		return instance;
	}
	
	
	@Override
	public void run()
	{
		Thread.currentThread().setName("Dormouse");
		log.trace("Dormouse started...");
		try
		{
			Thread.sleep(Long.MAX_VALUE);
		} catch (final InterruptedException err)
		{
			Thread.currentThread().interrupt();
		}
		log.trace("Dormouse stopped.");
	}
}
