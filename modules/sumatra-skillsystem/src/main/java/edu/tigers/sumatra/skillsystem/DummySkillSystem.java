package edu.tigers.sumatra.skillsystem;

import java.util.Collections;
import java.util.List;

import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public class DummySkillSystem extends ASkillSystem
{
	@Override
	public void execute(final BotID botId, final ISkill skill)
	{
		// empty
	}
	
	
	@Override
	public void reset(final BotID botId)
	{
		// empty
	}
	
	
	@Override
	public void reset(final ETeamColor color)
	{
		// empty
	}
	
	
	@Override
	public List<ISkill> getCurrentSkills(final ETeamColor teamColor)
	{
		return Collections.emptyList();
	}
	
	
	@Override
	public ShapeMap process(final WorldFrameWrapper wfw, final ETeamColor teamColor)
	{
		return new ShapeMap();
	}
	
	
	@Override
	public void emergencyStop()
	{
		// empty
	}
	
	
	@Override
	public void emergencyStop(final ETeamColor teamColor)
	{
		// empty
	}
}
