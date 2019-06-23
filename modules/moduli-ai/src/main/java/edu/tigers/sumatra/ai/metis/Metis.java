/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis;

import java.util.EnumMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;
import com.github.g3force.instanceables.InstanceableClass.NotCreateableException;

import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * This class does situation/field analysis. Metis coordinates all calculators to analyze the
 * {@link WorldFrame}.
 * She will eventually put all the gathered conclusions in the {@link AIInfoFrame}.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class Metis implements IConfigObserver
{
	private static final String CONFIG_METIS = "metis";
	private static final String CONFIG_PLAYS = "plays";
	private static final String CONFIG_ROLES = "roles";
	
	static
	{
		for (ECalculator ec : ECalculator.values())
		{
			ConfigRegistration.registerClass(CONFIG_METIS, ec.getInstanceableClass().getImpl());
		}
		for (EPlay ec : EPlay.values())
		{
			ConfigRegistration.registerClass(CONFIG_PLAYS, ec.getInstanceableClass().getImpl());
		}
		for (ERole ec : ERole.values())
		{
			ConfigRegistration.registerClass(CONFIG_ROLES, ec.getInstanceableClass().getImpl());
		}
	}
	
	private static final Logger log = Logger.getLogger(Metis.class.getName());
	
	private final Map<ECalculator, ACalculator> calculators = new EnumMap<>(ECalculator.class);
	private ETeamColor teamColor = ETeamColor.NEUTRAL;
	
	
	/**
	 * init new metis instance
	 */
	public Metis()
	{
		for (ECalculator eCalc : ECalculator.values())
		{
			if (eCalc.getInstanceableClass().getImpl() != null)
			{
				try
				{
					ACalculator inst = (ACalculator) eCalc.getInstanceableClass().newDefaultInstance();
					inst.setActive(eCalc.isInitiallyActive());
					calculators.put(eCalc, inst);
				} catch (NotCreateableException e)
				{
					log.error("Could not instantiate calculator: " + eCalc, e);
				}
			}
		}
		
		ConfigRegistration.registerConfigurableCallback(CONFIG_METIS, this);
		ConfigRegistration.registerConfigurableCallback(CONFIG_PLAYS, this);
		ConfigRegistration.registerConfigurableCallback(CONFIG_ROLES, this);
	}
	
	
	/**
	 * Process a frame
	 * 
	 * @param baseAiFrame
	 * @return
	 */
	public MetisAiFrame process(final BaseAiFrame baseAiFrame)
	{
		if (teamColor == ETeamColor.NEUTRAL)
		{
			teamColor = baseAiFrame.getTeamColor();
			afterApply(null);
		}
		TacticalField newTacticalField = new TacticalField();
		Map<ECalculator, Integer> metisCalcTimes = new EnumMap<>(ECalculator.class);
		Map<ECalculator, Boolean> metisExecutionStatus = new EnumMap<>(ECalculator.class);
		long time = System.nanoTime();
		for (Map.Entry<ECalculator, ACalculator> entry : calculators.entrySet())
		{
			ECalculator type = entry.getKey();
			ACalculator calc = entry.getValue();
			calc.calculate(newTacticalField, baseAiFrame);
			long aTime = System.nanoTime();
			int diff = (int) ((aTime - time) * 1e-3f);
			metisCalcTimes.put(type, diff);
			metisExecutionStatus.put(type, calc.getExecutionStatusLastFrame());
			time = aTime;
		}
		newTacticalField.setMetisCalcTimes(metisCalcTimes);
		newTacticalField.setMetisExecutionStatus(metisExecutionStatus);
		
		return new MetisAiFrame(baseAiFrame, newTacticalField);
	}
	
	
	@Override
	public void afterApply(final IConfigClient configClient)
	{
		// apply default spezi
		calculators.values().forEach(calc -> ConfigRegistration.applySpezis(calc, CONFIG_METIS, ""));
		
		// apply team spezies if not in match mode
		if (!SumatraModel.getInstance().isProductive())
		{
			calculators.values().forEach(calc -> ConfigRegistration.applySpezis(calc, CONFIG_METIS, teamColor.name()));
		}
	}
	
	
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
}
