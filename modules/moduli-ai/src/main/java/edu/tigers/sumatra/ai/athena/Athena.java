/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.PlayStrategy;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.lachesis.Lachesis;
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
	
	/** Role-assigner instance */
	private final Lachesis lachesis;
	
	/** AI control from GUI */
	private AAthenaAdapter athenaAdapter;
	
	private EAIControlState controlState = EAIControlState.MATCH_MODE;
	
	
	/**
	 * Default
	 */
	public Athena()
	{
		lachesis = new Lachesis();
		athenaAdapter = new MatchModeAthenaAdapter();
	}
	
	
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
			case MIXED_TEAM_MODE:
				athenaAdapter = new MixedTeamModeAthenaAdapter();
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
		final PlayStrategy.Builder playStrategyBuilder = new PlayStrategy.Builder();
		return process(metisAiFrame, playStrategyBuilder);
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
		playStrategyBuilder.setAIControlState(controlState);
		athenaAiFrame = new AthenaAiFrame(metisAiFrame, playStrategyBuilder.build());
		
		processRoleAssignment(athenaAiFrame);
		if ((controlState == EAIControlState.MATCH_MODE) || (controlState == EAIControlState.MIXED_TEAM_MODE))
		{
			checkRoleCount(athenaAiFrame);
		}
		
		updatePlays(athenaAiFrame);
		drawRoleNames(athenaAiFrame);
		return athenaAiFrame;
	}
	
	
	private void drawRoleNames(final AthenaAiFrame aiFrame)
	{
		List<IDrawableShape> shapes = aiFrame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.ROLE_NAMES);
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
			dTxtRole.setFontHeight(50);
			dTxtRole.setOffset(Vector2.fromX(130));
			shapes.add(dTxtRole);
		}
	}
	
	
	private void checkRoleCount(final AthenaAiFrame frame)
	{
		if (SumatraModel.getInstance().isTestMode() && !frame.getGamestate().isIdleGame())
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
	
	
	private void processRoleAssignment(final AthenaAiFrame frame)
	{
		lachesis.assignRoles(frame);
	}
	
	
	private void updatePlays(final AthenaAiFrame frame)
	{
		// update all plays with the new frame and remove plays which failed or succeeded
		for (APlay play : new ArrayList<>(frame.getPlayStrategy().getActivePlays()))
		{
			
			try
			{
				for (ARole role : play.getRoles())
				{
					role.updateBefore(frame);
				}
				
				play.updateBeforeRoles(frame);
				
				for (ARole role : play.getRoles())
				{
					role.update(frame);
				}
				
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