/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.11.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.ai.metis.fieldanalyser;


import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldanalysis.FieldAnalyserCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.WorldFrameFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.FrameFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.StopWatch;
import edu.dhbw.mannheim.tigers.sumatra.util.SumatraSetupHelper;


/**
 * Simple Benchmark for {@link FieldAnalyserCalc}.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class FieldAnalyzerPerf
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final WorldFrameFactory	wFrameFactory;
	private final FrameFactory			frameFactory;
	private final FieldAnalyserCalc	fieldAnalyser;
	
	private final StopWatch				watch;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public FieldAnalyzerPerf()
	{
		SumatraSetupHelper.setupSumatra();
		
		wFrameFactory = new WorldFrameFactory();
		wFrameFactory.createFrames(10);
		frameFactory = new FrameFactory();
		fieldAnalyser = new FieldAnalyserCalc();
		watch = new StopWatch();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	@Test
	public void test()
	{
		AIInfoFrame aiFrame = null;
		
		for (int i = 0; i < 100; i++)
		{
			for (final WorldFrame wFrame : wFrameFactory.getwFrames())
			{
				aiFrame = frameFactory.createMinimalAiInfoFrame(wFrame);
				
				final long start = System.nanoTime();
				fieldAnalyser.doCalc(new TacticalField(wFrame), aiFrame);
				watch.stop(start);
			}
		}
		System.out.println("Time: " + watch.mean() + "[ns]");
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
