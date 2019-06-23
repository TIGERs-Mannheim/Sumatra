/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.06.2011
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Rectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector3f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillGroup;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;

/**
 * This skill sends a single move command and logs the feedback from the vision.
 * Useful for trainings and automated learning.
 * 
 * @author AndreR
 * 
 */
public class MoveDataCollector extends ASkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	public class DataItem
	{
		public double absTime;
		public Vector3f realVelocity;
		public Vector3f commandedVeolcity;
		public Vector2f position;
	};
	
	private Vector2f velocity;
	private final float turnVelocity;
	
	private List<DataItem> data = new ArrayList<DataItem>();
	

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public MoveDataCollector(IVector2 dir, float rot)
	{
		super(ESkillName.MOVE_DATA_COLLECTOR, ESkillGroup.MOVE);
		
		if(dir == null)
		{
			this.velocity = new Vector2f(0, 1);
			this.turnVelocity = 0;
		}
		else
		{
			this.velocity = new Vector2f(dir);
			this.turnVelocity = rot;
		}
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public ArrayList<ACommand> calcEntryActions(ArrayList<ACommand> cmds)
	{
		cmds.add(new TigerMotorMoveV2(velocity, turnVelocity));

		return cmds;
	}
	
	public ArrayList<ACommand> calcExitActions(ArrayList<ACommand> cmds)
	{
		cmds.add(new TigerMotorMoveV2(new Vector2f(0, 0), 0));

		return cmds;
	}
	
	@Override
	public ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds)
	{
		TrackedTigerBot trackedBot = getBot();
		if(trackedBot == null)
		{
			complete();
			return cmds;
		}
		
		float currentW = trackedBot.aVel;
		Vector2 currentXY = new Vector2(trackedBot.vel);
		Vector2 localXY = new Vector2(currentXY.turnNew(-trackedBot.angle));
		localXY = new Vector2(-localXY.y, localXY.x);
		
		Vector2 vDiff = velocity.absNew().subtract(localXY.absNew());
		float tDiff = Math.abs(turnVelocity)-Math.abs(currentW);
		
		Vector2 okVDiff = velocity.absNew().multiply(0.1f);
		float okTDiff = Math.abs(turnVelocity)*0.1f;
		
		if(velocity.x == 0)
		{
			okVDiff.x = 100.0f;
		}
		
		if(velocity.y == 0)
		{
			okVDiff.y = 100.0f;
		}
		
		if(turnVelocity == 0)
		{
			okTDiff = 100.0f;
		}
		
		if(vDiff.x < okVDiff.x && vDiff.y < okVDiff.y && tDiff < okTDiff)
		{
			// log
			DataItem item = new DataItem();
			item.absTime = ((double)System.nanoTime())/10e9;
			item.commandedVeolcity = new Vector3f(velocity, turnVelocity);
			item.realVelocity = new Vector3f(localXY, currentW);
			item.position = new Vector2f(trackedBot.pos);
			
			data.add(item);
		}
		
		
		Rectangle stopBound = new Rectangle(AIConfig.getGeometry().getField());
		stopBound.shrink(500, 500);
		
		if(!stopBound.isPointInShape(trackedBot.pos))
		{
			complete();
		}
		
		return cmds;
	}
	
	
	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		return false;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public List<DataItem> getData()
	{
		return data;
	}
}
