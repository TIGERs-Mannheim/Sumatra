/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.athena.roleassigner.IRoleAssigner;
import edu.tigers.sumatra.ai.athena.roleassigner.SimpleRoleAssigner;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.statemachine.IState;


/**
 * This class chooses a play according to the current situation and adds it to the current {@link AIInfoFrame}.
 * 
 * @author OliverS, DanielW, Gero
 */
public class Athena
{
	private static final Logger log = Logger.getLogger(Athena.class.getName());
	
	private final IRoleAssigner roleAssigner = new SimpleRoleAssigner();
	
	/** AI control from GUI */
	private AAthenaAdapter athenaAdapter = new MatchModeAthenaAdapter();
	
	private EAIControlState controlState = EAIControlState.MATCH_MODE;
	
	
	/**
	 * @param mode
	 */
	public void changeMode(final EAIControlState mode)
	{
		switch (mode)
		{
			case EMERGENCY_MODE:
				athenaAdapter = new EmergencyModeAthenaAdapter();
				break;
			case MATCH_MODE:
				athenaAdapter = new MatchModeAthenaAdapter();
				break;
			case TEST_MODE:
				athenaAdapter = new TestModeAthenaAdapter();
				break;
			default:
				throw new IllegalStateException();
		}
		controlState = mode;
	}
	
	
	/**
	 * @return the controlState
	 */
	public final EAIControlState getControlState()
	{
		return controlState;
	}
	
	
	/**
	 * perform play selection and call update on every play
	 * 
	 * @param metisAiFrame
	 * @return
	 */
	public AthenaAiFrame process(final MetisAiFrame metisAiFrame)
	{
		return process(metisAiFrame, new PlayStrategy.Builder());
	}
	
	
	/**
	 * Perform play selection. This is public for FrameFactory
	 * 
	 * @param metisAiFrame
	 * @param playStrategyBuilder
	 * @return
	 */
	public AthenaAiFrame process(final MetisAiFrame metisAiFrame, final PlayStrategy.Builder playStrategyBuilder)
	{
		AthenaAiFrame athenaAiFrame;
		athenaAdapter.process(metisAiFrame, playStrategyBuilder);
		IPlayStrategy playStrategy = playStrategyBuilder.build();
		athenaAiFrame = new AthenaAiFrame(metisAiFrame, playStrategy);
		
		roleAssigner.assignRoles(playStrategy);
		checkRoleCount(athenaAiFrame);
		
		updatePlays(athenaAiFrame);
		drawRoleNames(athenaAiFrame);
		return athenaAiFrame;
	}
	
	
	private void drawRoleNames(final AthenaAiFrame aiFrame)
	{
		List<IDrawableShape> shapes = aiFrame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.AI_ROLE_NAMES);
		for (ARole role : aiFrame.getPlayStrategy().getActiveRoles().values())
		{
			if (role.getBot() == null)
			{
				continue;
			}
			
			IVector2 pos = role.getPos();
			IState state = role.getCurrentState();
			String text = role.getType().name();
			if (state != null)
			{
				text += "\n" + state.getIdentifier();
			}
			DrawableAnnotation dTxtRole = new DrawableAnnotation(pos, text);
			dTxtRole.setColor(aiFrame.getTeamColor().getColor());
			dTxtRole.withFontHeight(50);
			dTxtRole.withOffset(Vector2.fromX(130));
			shapes.add(dTxtRole);
		}
	}
	
	
	private void checkRoleCount(final AthenaAiFrame frame)
	{
		if (controlState == EAIControlState.MATCH_MODE
				&& SumatraModel.getInstance().isTestMode()
				&& !frame.getGamestate().isIdleGame())
		{
			int numBots = frame.getWorldFrame().getTigerBotsAvailable().size();
			List<ARole> roles = frame.getPlayStrategy().getActivePlays().stream()
					.map(APlay::getRoles)
					.flatMap(Collection::stream)
					.collect(Collectors.toList());
			if (numBots != roles.size())
			{
				log.warn("Assigned role number does not match number of bots: numBots=" + numBots + ", numRoles="
						+ roles.size());
				log.warn("Roles: " + roles);
			}
		}
	}
	
	
	/**
	 * @param aiFrame
	 */
	public void onException(final AthenaAiFrame aiFrame)
	{
		for (ARole role : aiFrame.getPlayStrategy().getActiveRoles().values())
		{
			role.setCompleted();
		}
	}
	
	
	private void updatePlays(final AthenaAiFrame frame)
	{
		for (APlay play : frame.getPlayStrategy().getActivePlays())
		{
			try
			{
				play.getRoles().forEach(role -> role.updateBefore(frame));
				play.updateBeforeRoles(frame);
				play.getRoles().forEach(role -> role.update(frame));
				play.update(frame);
			} catch (Exception err)
			{
				log.error("Exception during play update!", err);
			}
		}
	}
	
	
	/**
	 * @return the athenaAdapter
	 */
	public final AAthenaAdapter getAthenaAdapter()
	{
		return athenaAdapter;
	}
}