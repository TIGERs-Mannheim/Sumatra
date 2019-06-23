/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 17.07.2015
 * Author(s): JulianT
 * *********************************************************
 */
package edu.tigers.sumatra.multiteammessage;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.Location;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.Pose;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.TeamPlan;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.IAIObserver;
import edu.tigers.sumatra.ai.data.MultiTeamMessage;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author JulianT
 */
public class MultiTeamMessageHandler extends AMultiTeamMessage implements IAIObserver
{
	@SuppressWarnings("unused")
	private static final Logger				log			= Logger.getLogger(MultiTeamMessageHandler.class.getName());
																		
	private final MultiTeamMessageReceiver	receiver;
	private final MultiTeamMessageSender	multiTeamSender;
														
	@Configurable
	private static ETeamColor					teamColor	= ETeamColor.YELLOW;
																		
	private WorldFrame							worldFrame;
														
														
	/**
	 * @param subnodeConfig
	 */
	public MultiTeamMessageHandler(final SubnodeConfiguration subnodeConfig)
	{
		receiver = new MultiTeamMessageReceiver(this);
		multiTeamSender = new MultiTeamMessageSender();
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
	}
	
	
	@Override
	public void deinitModule()
	{
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		receiver.start();
		multiTeamSender.start();
		
		try
		{
			AAgent ab = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
			ab.addObserver(this);
			AAgent ay = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
			ay.addObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find agent", e);
		}
	}
	
	
	@Override
	public void stopModule()
	{
		receiver.cleanup();
		multiTeamSender.stop();
		try
		{
			AAgent ab = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
			ab.removeObserver(this);
			AAgent ay = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
			ay.removeObserver(this);
		} catch (ModuleNotFoundException e)
		{
			log.error("Could not find agent", e);
		}
	}
	
	
	protected void onNewMultiTeamMessage(final TeamPlan teamPlan)
	{
		
		final MultiTeamMessage message = new MultiTeamMessage(buildAiCoordinateTeamPlan(teamPlan));
		notifyNewMultiTeamMessage(message);
	}
	
	
	/**
	 * @param teamPlan
	 * @return
	 */
	private TeamPlan buildAiCoordinateTeamPlan(final TeamPlan teamPlan)
	{
		if (!worldFrame.isInverted())
		{
			return teamPlan;
		}
		TeamPlan.Builder builder = TeamPlan.newBuilder();
		for (RobotPlan plan : teamPlan.getPlansList())
		{
			Location navLoc = plan.getNavTarget().getLoc();
			float navHeading = plan.getNavTarget().getHeading();
			Location kickLoc = (Location) plan.getShotTargetOrBuilder();
			
			navLoc = Location.newBuilder().setX(navLoc.getX() * -1).setY(navLoc.getY() * -1).build();
			navHeading = (float) (Math.PI - navHeading);
			
			kickLoc = Location.newBuilder().setX(kickLoc.getX() * -1).setY(kickLoc.getY() * -1).build();
			builder.addPlans(
					RobotPlan.newBuilder(plan).setNavTarget(Pose.newBuilder().setLoc(navLoc).setHeading(navHeading).build())
							.setShotTarget(kickLoc).build());
		}
		return builder.build();
		
	}
	
	
	@Override
	public void onNewAIInfoFrame(final AIInfoFrame lastAIInfoframe)
	{
		if (lastAIInfoframe.getTeamColor() == teamColor)
		{
			worldFrame = lastAIInfoframe.getWorldFrame();
			multiTeamSender.send(lastAIInfoframe);
		}
	}
}
