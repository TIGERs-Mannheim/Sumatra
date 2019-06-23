/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.05.2011
 * Author(s): Oliver Steinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.skilltester;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.csvexporter.CSVExporter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillSystemObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.moduli.exceptions.ModuleNotFoundException;


/**
 * Generic SkillTester for measurement propose.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public abstract class ASkillTester extends ABaseRole implements ISkillSystemObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long		serialVersionUID	= -848585529162236931L;
	
	private ESkillTestState			state					= ESkillTestState.PREPARE;
	@SuppressWarnings("unused")
	private boolean					completed			= false;
	private boolean					isObserver			= false;
	
	private Long						startTime			= null;
	/** waiting time before and after the executed skill [ms] */
	private final long				waitingTime;
	
	protected final CSVExporter	exporter;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	/**
	 * Ctor for a new SkillTester.
	 * 
	 * @param type of role
	 * @param exporterName the name of the CSV-Exporter
	 * @param waitingTime before and after skill execution
	 */
	public ASkillTester(ERole type, String exporterName, long waitingTime)
	{
		super(type);
		CSVExporter.createInstance(exporterName, exporterName, true);
		this.exporter = CSVExporter.getInstance(exporterName);
		this.waitingTime = waitingTime;
		

		try
		{
			((ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID)).addObserver(this);
			isObserver = true;
		} catch (ModuleNotFoundException err)
		{
			err.printStackTrace();
		}
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	@Override
	public void update(AIInfoFrame currentFrame)
	{
		// not used
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		switch (state)
		{
			case PREPARE:

				if (startTime == null)
					startTime = System.nanoTime();
				
				if ((System.nanoTime() - startTime) / 1e6 >= waitingTime)
				{
					state = ESkillTestState.START;
					startTime = null;
				}
				
				doPrepare(wFrame);
				break;
			

			case START:
				doStart(wFrame, skills);
				state = ESkillTestState.MEASURE;
				break;
			

			case MEASURE:
				if (startTime == null)
					startTime = System.nanoTime();
				
				if ((System.nanoTime() - startTime) / 1e6 >= waitingTime)
				{
					state = ESkillTestState.FINISHED;
					startTime = null;
				}
				
				doMeasure(wFrame);
				break;
			

			case FINISHED:
				// remove observer
				if (isObserver)
					try
					{
						((ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID)).removeObserver(this);
						isObserver = false;
					} catch (ModuleNotFoundException err)
					{
						err.printStackTrace();
					}
				
				exporter.close();
				break;
		}
		
	}
	

	@Override
	public void onSkillCompleted(ASkill skill, int botID)
	{
		if (botID == getBotID()) // check on skill type is not needed since we only enqueue one skill at a time
			completed = true;
		
	}
	

	@Override
	public void onSkillStarted(ASkill skill, int botID)
	{
		// do nothing
	}
	

	// --------------------------------------------------------------------------
	// --- abstract methods -----------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Do preparation before the skill.
	 * (see {@link ASkillTester#calculateSkills(WorldFrame, SkillFacade)} for
	 * more information about workflow)
	 * 
	 * @param wFrame
	 */
	protected abstract void doPrepare(WorldFrame wFrame);
	

	/**
	 * Starts the skill.
	 * (see {@link ASkillTester#calculateSkills(WorldFrame, SkillFacade)} for
	 * more information about workflow)
	 * 
	 * @param wFrame
	 * @param skill
	 */
	protected abstract void doStart(WorldFrame wFrame, SkillFacade skills);
	

	/**
	 * Measure values and export them to log.
	 * (see {@link ASkillTester#calculateSkills(WorldFrame, SkillFacade)} for
	 * more information about workflow)
	 * 
	 * @param wFrame
	 */
	protected abstract void doMeasure(WorldFrame wFrame);
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
