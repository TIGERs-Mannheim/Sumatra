package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.data.MultimediaControl;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.pathfinder.PathFinderPrioMap;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public record SkillInput(
		WorldFrameWrapper wf,
		ShapeMap shapeMap,
		ABot currentBot,
		PathFinderPrioMap prioMap,
		MultimediaControl multimediaControl
)
{
}
