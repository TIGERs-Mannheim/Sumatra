/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.05.2011
 * Author(s): Oliver Steinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.skilltester;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
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
public class KickTester extends ABaseRole implements ISkillSystemObserver
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
		DRIBBLE,
		KICK,
		MOVE,
		MOVE_STOP,
		TIMEOUT2,
		FINISHED;
		
	}
	
	private State			state			= State.DRIBBLE;
	private boolean		completed	= false;
	private boolean		isObserver	= false;
	
	private Long			startTime	= null;
	private final long	timeout		= 3000;				// ms
	private Long			startTime2	= null;
	private final long	timeout2		= 7000;				// ms
																		
	private boolean		kicked		= false;
	
	private CSVExporter	exporter;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public KickTester()
	{
		super(ERole.KICK_TESTER);
		
		// register as a skill-system observer to observe skill-complete events
		try
		{
			((ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID)).addObserver(this);
			isObserver = true;
		} catch (ModuleNotFoundException err)
		{
			err.printStackTrace();
		}
		
		CSVExporter.createInstance("kick", "kick/kick_new", true);
		exporter = CSVExporter.getInstance("kick");
		exporter.setHeader("time", "dist", "ballvel", "kicked");
		
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
		
		TrackedTigerBot bot = wFrame.tigerBots.get(getBotID());
		
		if (!exporter.isClosed())
		{
			float length = bot.pos.subtractNew(wFrame.ball.pos).getLength2();
			exporter.addValues(System.nanoTime(), length / 1000, wFrame.ball.vel.getLength2(), kicked ? 1 : 0);
		}
		switch (state)
		{
			case MOVE:
				if (!completed)
				{
					skills.moveTo(new Vector2f(0, 200), AIMath.PI);
					completed = false;
				} else
				{
					state = State.DRIBBLE;
					completed = false;
				}
				
				break;
			case DRIBBLE:
				skills.dribble(true);
				state = State.TIMEOUT;
				completed = false;
				break;
			
			case TIMEOUT:
				if (startTime == null)
					startTime = System.nanoTime();
				
				if ((System.nanoTime() - startTime) / 1e6 >= timeout)
				{
					state = State.KICK;
					startTime = null;
				}
				completed = false;
				
				break;
			case KICK:
				if (!completed)
				{
					// skills.kickArmDirect(500);
					// skills.kickArm(10);
					skills.kickArm(2.0f, 1.5f);
					kicked = true;
					completed = false;
				} else
				{
					state = State.TIMEOUT2;
					completed = false;
				}
				
				break;
			
			case TIMEOUT2:
				if (startTime2 == null)
					startTime2 = System.nanoTime();
				
				if ((System.nanoTime() - startTime2) / 1e6 >= timeout2)
				{
					state = State.FINISHED;
					startTime2 = null;
				}
				completed = false;
				break;
			case FINISHED:
				skills.dribble(false);
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
