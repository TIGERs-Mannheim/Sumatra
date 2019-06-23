/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.02.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.skilltester;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
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
public class DirectMoveTester extends ABaseRole implements ISkillSystemObserver
{
	/**  */
	private static final long	serialVersionUID	= 1833424209117756234L;
	
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
		MOVE3,
		MOVE4,
		MOVE5,
		MOVE6,
		MOVE7,
		MOVE_STOP,
		TIMEOUT2,
		FINISHED,
		MOVE10,
		MOVE8,
		MOVE11,
		MOVE9;
	}
	
	private State			state					= State.TIMEOUT;
	private boolean		completed			= false;
	private boolean		isObserver			= false;
	
	private Long			startTime			= null;
	private final long	timeout				= 1000;						// ms
																						
	private IVector2		initialPos			= null;
	@SuppressWarnings("unused")
	private IVector2		currentSetpoint	= Vector2.ZERO_VECTOR;
	
	private CSVExporter	exporter;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public DirectMoveTester()
	{
		super(ERole.DIRECTMOVE_TESTER);
		
		// register as a skill-system observer to observe skill-complete events
		try
		{
			((ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID)).addObserver(this);
			isObserver = true;
		} catch (ModuleNotFoundException err)
		{
			err.printStackTrace();
		}
		
		CSVExporter.createInstance("direct", "direct", true);
		exporter = CSVExporter.getInstance("direct");
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
		if (initialPos == null)
		{
			initialPos = wFrame.tigerBots.get(getBotID()).pos;
		}
		TrackedTigerBot bot = wFrame.tigerBots.get(getBotID());
		
		IVector2 actVel = bot.vel.turnNew(AIMath.PI_HALF - bot.angle);
		// IVector2 actPos = bot.pos;
		// Header("time", "setx", "sety", "setv", "actx","acty","actv");
		if (!exporter.isClosed())
		{
			exporter.addValues(System.nanoTime(), currentSetpoint.x(), currentSetpoint.y(), 0, actVel.x(), actVel.y(),
					bot.angle);
		}
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
					Vector2 move = new Vector2(0, 1).scaleTo(0.5f);
					currentSetpoint = move;
					skills.moveDirect(move, 0, 0, 500, true);
					completed = false;
				} else
				{
					state = State.MOVE2;
					completed = false;
				}
				
				break;
			case MOVE2:
				if (!completed)
				{
					Vector2 move = new Vector2(0, 1).scaleTo(1.0f);
					currentSetpoint = move;
					skills.moveDirect(move, 0, 0, 1000, true);
					completed = false;
				} else
				{
					state = State.MOVE4;
					completed = false;
				}
				
				break;
			case MOVE3:
				if (!completed)
				{
					Vector2 move = new Vector2(0, 1).scaleTo(1.5f);
					currentSetpoint = move;
					skills.moveDirect(move, 0, 0, 800);
					completed = false;
				} else
				{
					state = State.MOVE4;
					completed = false;
				}
				
				break;
			case MOVE4:
				if (!completed)
				{
					Vector2 move = new Vector2(0, 1).scaleTo(0.5f);
					currentSetpoint = move;
					skills.moveDirect(move, 0, 0, 1000);
					completed = false;
				} else
				{
					state = State.MOVE5;
					completed = false;
				}
				
				break;
			case MOVE5:
				if (!completed)
				{
					Vector2 move = new Vector2(0, -1).scaleTo(0.5f);
					currentSetpoint = move;
					skills.moveDirect(move, 0, 0, 1000);
					completed = false;
				} else
				{
					state = State.MOVE6;
					completed = false;
				}
				
				break;
			case MOVE6:
				if (!completed)
				{
					Vector2 move = new Vector2(0, -1).scaleTo(1.0f);
					currentSetpoint = move;
					skills.moveDirect(move, 0, 0, 2000);
					completed = false;
				} else
				{
					state = State.MOVE7;
					completed = false;
				}
				
				break;
			case MOVE7:
				if (!completed)
				{
					Vector2 move = new Vector2(0, -1).scaleTo(0.0f);
					currentSetpoint = move;
					skills.moveDirect(move, 0, 0, 1000);
					completed = false;
				} else
				{
					state = State.MOVE8;
					completed = false;
				}
				
				break;
			case MOVE8:
				if (!completed)
				{
					Vector2 move = new Vector2(1, 0).scaleTo(0.5f);
					currentSetpoint = move;
					skills.moveDirect(move, 0, 0, 1000);
					completed = false;
				} else
				{
					state = State.MOVE9;
					completed = false;
				}
				
				break;
			case MOVE9:
				if (!completed)
				{
					Vector2 move = new Vector2(1, 0).scaleTo(1.0f);
					currentSetpoint = move;
					skills.moveDirect(move, 0, 0, 1000);
					completed = false;
				} else
				{
					state = State.MOVE10;
					completed = false;
				}
				
				break;
			case MOVE10:
				if (!completed)
				{
					Vector2 move = new Vector2(1, 0).scaleTo(0.0f);
					currentSetpoint = move;
					skills.moveDirect(move, 0, 0, 1000);
					completed = false;
				} else
				{
					state = State.MOVE11;
					completed = false;
				}
				
				break;
			case MOVE11:
				if (!completed)
				{
					Vector2 move = new Vector2(-1, 0).scaleTo(0.5f);
					currentSetpoint = move;
					skills.moveDirect(move, 0, 0, 1000);
					completed = false;
				} else
				{
					state = State.MOVE_STOP;
					completed = false;
				}
				
				break;
			
			case MOVE_STOP:
				if (!completed)
				{
					Vector2 move = new Vector2(1, 0).scaleTo(0.0f);
					currentSetpoint = move;
					skills.moveDirect(move, 0, 0, 1000);
					completed = false;
				} else
				{
					state = State.FINISHED;
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
