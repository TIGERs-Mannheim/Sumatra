/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.multiteammessage;

import static edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.Location;
import static edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.TeamPlan;
import edu.tigers.sumatra.ai.data.MultiTeamMessage;
import edu.tigers.sumatra.ai.data.MultiTeamPlan;
import edu.tigers.sumatra.ai.data.MultiTeamRobotPlan;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * @author JulianT
 */
public class MultiTeamMessageHandler
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(MultiTeamMessageHandler.class.getName());
	
	private final MultiTeamMessageReceiver receiver;
	private final MultiTeamMessageSender sender;
	private final EAiTeam aiTeam;
	
	@Configurable(spezis = { "YELLOW_PRIMARY", "YELLOW_SECONDARY", "BLUE_PRIMARY", "BLUE_SECONDARY" }, defValueSpezis = {
			"127.0.0.1", "127.0.0.1", "127.0.0.1", "127.0.0.1" })
	private String address = "";
	
	@Configurable(spezis = { "YELLOW_PRIMARY", "YELLOW_SECONDARY", "BLUE_PRIMARY", "BLUE_SECONDARY" }, defValueSpezis = {
			"10012", "10013", "10014", "10015" })
	private int portReceiving = 0;
	
	@Configurable(spezis = { "YELLOW_PRIMARY", "YELLOW_SECONDARY", "BLUE_PRIMARY", "BLUE_SECONDARY" }, defValueSpezis = {
			"10013", "10012", "10015", "10014" })
	private int portSendingTo = 0;
	
	static
	{
		ConfigRegistration.registerClass("user", MultiTeamMessageHandler.class);
	}
	
	
	/**
	 * @param aiTeam
	 */
	public MultiTeamMessageHandler(EAiTeam aiTeam)
	{
		ConfigRegistration.applySpezis(this, "user", aiTeam.name());
		receiver = new MultiTeamMessageReceiver(address, portReceiving);
		sender = new MultiTeamMessageSender(address, portSendingTo);
		this.aiTeam = aiTeam;
	}
	
	
	/**
	 * Start receiving
	 */
	public void start()
	{
		receiver.start();
		sender.start();
	}
	
	
	/**
	 * Stop and cleanup
	 */
	public void stop()
	{
		receiver.cleanup();
		sender.stop();
	}
	
	
	/**
	 * @return true, if this handler is active
	 */
	public boolean isActive()
	{
		return receiver.isReady();
	}
	
	
	/**
	 * @param multiTeamPlan the new team plan from the current AI
	 */
	public void sendTeamPlan(final MultiTeamPlan multiTeamPlan)
	{
		sender.send(mapToProtobufTeamPlan(multiTeamPlan));
	}
	
	
	/**
	 * @return the latest received team plan from the other team
	 */
	public MultiTeamMessage getMultiTeamMessage()
	{
		if (isActive())
		{
			return new MultiTeamMessage(mapToSumatraPlan(receiver.getTeamPlan()), true);
		}
		return MultiTeamMessage.DEFAULT;
	}
	
	
	private MultiTeamPlan mapToSumatraPlan(TeamPlan teamPlan)
	{
		MultiTeamPlan multiTeamPlan = new MultiTeamPlan();
		for (RobotPlan robotPlan : teamPlan.getPlansList())
		{
			BotID botID = BotID.createBotId(robotPlan.getRobotId(), aiTeam.getTeamColor());
			MultiTeamRobotPlan multiTeamRobotPlan = new MultiTeamRobotPlan(botID);
			multiTeamRobotPlan.setRole(sumatraRole(robotPlan.getRole()));
			multiTeamRobotPlan.setTargetPose(sumatraPose(robotPlan.getNavTarget()));
			multiTeamRobotPlan.setShootTarget(sumatraPos(robotPlan.getShotTarget()));
			multiTeamPlan.getRobotPlans().put(botID, multiTeamRobotPlan);
		}
		return multiTeamPlan;
	}
	
	
	private MultiTeamRobotPlan.EMultiTeamRobotRole sumatraRole(final RobotPlan.RobotRole role)
	{
		switch (role)
		{
			case Default:
				return MultiTeamRobotPlan.EMultiTeamRobotRole.DEFAULT;
			case Goalie:
				return MultiTeamRobotPlan.EMultiTeamRobotRole.GOALIE;
			case Defense:
				return MultiTeamRobotPlan.EMultiTeamRobotRole.DEFENSE;
			case Offense:
				return MultiTeamRobotPlan.EMultiTeamRobotRole.OFFENSE;
			default:
				throw new IllegalStateException();
		}
	}
	
	
	private Pose sumatraPose(final MultiTeamCommunication.Pose navTarget)
	{
		return Pose.from(sumatraPos(navTarget.getLoc()), (double) navTarget.getHeading());
	}
	
	
	private IVector2 sumatraPos(final Location shotTarget)
	{
		return Vector2.fromXY(shotTarget.getX(), shotTarget.getY());
	}
	
	
	private TeamPlan mapToProtobufTeamPlan(MultiTeamPlan multiTeamPlan)
	{
		TeamPlan.Builder teamPlan = TeamPlan.newBuilder();
		for (MultiTeamRobotPlan plan : multiTeamPlan.getRobotPlans().values())
		{
			RobotPlan.Builder robotPlan = RobotPlan.newBuilder();
			robotPlan.setRobotId(plan.getBotID().getNumber());
			robotPlan.setRole(role(plan.getRole()));
			if (plan.getTargetPose().isPresent())
			{
				robotPlan.setNavTarget(pose(plan.getTargetPose().get()));
			}
			if (plan.getShootTarget().isPresent())
			{
				robotPlan.setShotTarget(location(plan.getShootTarget().get()));
			}
			teamPlan.addPlans(robotPlan);
		}
		return teamPlan.build();
	}
	
	
	private Location location(IVector2 pos)
	{
		return Location.newBuilder()
				.setX((int) pos.x())
				.setY((int) pos.y())
				.build();
	}
	
	
	private MultiTeamCommunication.Pose pose(edu.tigers.sumatra.math.pose.Pose pose)
	{
		return MultiTeamCommunication.Pose.newBuilder()
				.setLoc(location(pose.getPos()))
				.setHeading((float) pose.getOrientation())
				.build();
	}
	
	
	private RobotPlan.RobotRole role(MultiTeamRobotPlan.EMultiTeamRobotRole role)
	{
		switch (role)
		{
			case DEFAULT:
				return RobotPlan.RobotRole.Default;
			case GOALIE:
				return RobotPlan.RobotRole.Goalie;
			case DEFENSE:
				return RobotPlan.RobotRole.Defense;
			case OFFENSE:
				return RobotPlan.RobotRole.Offense;
			default:
				throw new IllegalStateException();
		}
	}
}
