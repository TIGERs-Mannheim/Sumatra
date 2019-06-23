/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.05.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.skilltester;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
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
 * this is a test role which can be used to test individual skills.
 * you can program a defined sequence of skill that should be executed one after another
 * each skill has to complete before the next gets started
 * 
 * This role has been developed to realize a test procedure for skills. It is recommended to use it
 * in conjunction with the {@link CSVExporter}.
 * 
 * @author DanielW
 * 
 */
public class MoveOnPathTester extends ABaseRole implements ISkillSystemObserver
{
	
	/**  */
	private static final long	serialVersionUID	= -8762886990719060829L;
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * each state represents a skill that should be executed. add as appropriate.
	 */
	private enum State
	{
		TIMEOUT,
		MOVE1,
		MOVE2,
		
		MOVE_STOP,
		TIMEOUT2,
		FINISHED;
		
	}
	
	private State			state			= State.TIMEOUT;
	private boolean		completed	= false;
	private boolean		isObserver	= false;
	
	private Long			startTime	= null;
	private final long	timeout		= 1000;				// ms
																		

	private CSVExporter	exporter;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public MoveOnPathTester()
	{
		super(ERole.MOVE_ON_PATH_TESTER);
		
		// register as a skill-system observer to observe skill-complete events
		try
		{
			((ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID)).addObserver(this);
			isObserver = true;
		} catch (ModuleNotFoundException err)
		{
			err.printStackTrace();
		}
		
		CSVExporter.createInstance("peter", "peter", true);
		exporter = CSVExporter.getInstance("peter");
		exporter.setHeader("time", "setx", "sety", "setv", "actx", "acty", "actv");
		
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
		
		// TrackedTigerBot bot = wFrame.tigerBots.get(getBotID());
		
		// IVector2 actVel = bot.vel.turnNew(AIMath.PI_HALF - bot.angle);
		// IVector2 actPos = bot.pos;
		// Header("time", "setx", "sety", "setv", "actx","acty","actv");
		// if (!exporter.isClosed())
		// {
		// exporter.addValues(System.nanoTime(), currentSetpoint.x(), currentSetpoint.y(), 0,
		// actPos.x() - initialPos.x(), actPos.y() - initialPos.y(), bot.angle);
		// }
		switch (state)
		{
			case TIMEOUT:
				if (startTime == null)
					startTime = System.nanoTime();
				
				if ((System.nanoTime() - startTime) / 1e6 >= timeout)
				{
					state = State.MOVE1;
					startTime = null;
				}
				

				break;
			case MOVE1:
				if (!completed)
				{
					skills.moveOnPath(new Vector2(0, 0), new Vector2(1000, 1000), new Vector2(1000, -1000));
					completed = false;
				} else
				{
					state = State.FINISHED;
					completed = false;
				}
				
				break;
			case MOVE2:
				if (!completed)
				{
					
					completed = false;
				} else
				{
					state = State.MOVE_STOP;
					completed = false;
				}
				
				break;
			

			case TIMEOUT2:
				if (startTime == null)
					startTime = System.nanoTime();
				
				if ((System.nanoTime() - startTime) / 1e6 >= timeout)
				{
					state = State.FINISHED;
					startTime = null;
				}
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
			
			default:
				break;
		}
	}
	

	@Override
	public void onSkillStarted(ASkill skill, int botID)
	{
		
	}
	

	@Override
	public void onSkillCompleted(ASkill skill, int botID)
	{
		if (botID == getBotID()) // check on skill type is not needed since we only enqueue one skill at a time
			completed = true;
	}
}
