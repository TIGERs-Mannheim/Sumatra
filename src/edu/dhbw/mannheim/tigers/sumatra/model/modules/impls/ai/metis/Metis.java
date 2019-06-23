/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.09.2010
 * Author(s):
 * Gunther Berthold <gunther.berthold@gmx.net>
 * Oliver Steinbrecher
 * Daniel Waigand
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ECalculator;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass.NotCreateableException;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;


/**
 * This class does situation/field analysis. Metis coordinates all calculators to analyze the
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame}.
 * She will eventually put all the gathered conclusions in the {@link AIInfoFrame}.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class Metis
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger						log			= Logger.getLogger(Metis.class.getName());
	
	private final Map<ECalculator, ACalculator>	calculators	= new LinkedHashMap<ECalculator, ACalculator>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public Metis()
	{
		log.trace("Creating");
		
		for (ECalculator eCalc : ECalculator.values())
		{
			if (eCalc.getImpl().getImpl() != null)
			{
				try
				{
					ACalculator inst = (ACalculator) eCalc.getImpl().newDefaultInstance();
					inst.setType(eCalc);
					inst.setActive(eCalc.isInitiallyActive());
					calculators.put(eCalc, inst);
				} catch (NotCreateableException e)
				{
					log.error("Could not instantiate calculator: " + eCalc, e);
				}
			}
		}
		
		log.trace("Created");
	}
	
	
	/**
	 * Process a frame
	 * 
	 * @param baseAiFrame
	 * @return
	 */
	public MetisAiFrame process(final BaseAiFrame baseAiFrame)
	{
		TacticalField newTacticalField = new TacticalField(baseAiFrame.getWorldFrame());
		long time = SumatraClock.nanoTime();
		for (ACalculator calc : calculators.values())
		{
			calc.calculate(newTacticalField, baseAiFrame);
			long aTime = SumatraClock.nanoTime();
			int diff = (int) ((aTime - time) * 1e-3f);
			newTacticalField.getMetisCalcTimes().put(calc.getType(), diff);
			time = aTime;
		}
		
		return new MetisAiFrame(baseAiFrame, newTacticalField);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * sets the active state of a calculator
	 * 
	 * @param calc
	 * @param active
	 */
	public void setCalculatorActive(final ECalculator calc, final boolean active)
	{
		ACalculator calculator = calculators.get(calc);
		if (calculator != null)
		{
			calculator.setActive(active);
		}
	}
	
	
	/**
	 * @param activeCalculators
	 */
	public void setActiveCalculators(final List<ECalculator> activeCalculators)
	{
		for (ECalculator calc : ECalculator.values())
		{
			if (activeCalculators.contains(calc))
			{
				setCalculatorActive(calc, true);
			} else
			{
				setCalculatorActive(calc, false);
			}
		}
	}
	
	
	/**
	 */
	public void stop()
	{
		for (ACalculator calc : calculators.values())
		{
			calc.deinit();
		}
	}
	
	
	/**
	 * @param eCalc
	 * @return
	 */
	public ACalculator getCalculator(final ECalculator eCalc)
	{
		return calculators.get(eCalc);
	}
}
