/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.athena;

import com.github.g3force.configurable.ConfigRegistration;
import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.statemachine.IState;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Set;


/**
 * This class chooses a play according to the current situation and adds it to the current {@link AIInfoFrame}.
 */
@Log4j2
public class Athena
{
	private static final String CONFIG_PLAYS = "plays";
	private static final String CONFIG_ROLES = "roles";

	static
	{
		for (EPlay ec : EPlay.values())
		{
			ConfigRegistration.registerClass(CONFIG_PLAYS, ec.getInstanceableClass().getImpl());
		}
		for (ERole ec : ERole.values())
		{
			ConfigRegistration.registerClass(CONFIG_ROLES, ec.getInstanceableClass().getImpl());
		}
	}

	@Getter
	private final AthenaGuiInput athenaGuiInput = new AthenaGuiInput();
	private IAthenaAdapter athenaAdapter = new MatchModeAthenaAdapter();
	@Getter
	private EAIControlState controlState = EAIControlState.MATCH_MODE;
	private boolean newControlState = false;


	/**
	 * @param mode
	 */
	public void changeMode(final EAIControlState mode)
	{
		controlState = mode;
		newControlState = true;
	}


	private void updateAdapter()
	{
		if (!newControlState)
		{
			return;
		}
		newControlState = false;
		athenaAdapter.stop(athenaGuiInput);
		switch (controlState)
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
	}


	/**
	 * perform play selection and call update on every play
	 *
	 * @param metisAiFrame
	 * @return
	 */
	public AthenaAiFrame process(final MetisAiFrame metisAiFrame)
	{
		updateAdapter();
		IPlayStrategy playStrategy = athenaAdapter.process(metisAiFrame, athenaGuiInput);
		AthenaAiFrame athenaAiFrame = AthenaAiFrame.builder()
				.baseAiFrame(metisAiFrame.getBaseAiFrame())
				.metisAiFrame(metisAiFrame)
				.playStrategy(playStrategy)
				.build();

		updatePlaysAndRoles(athenaAiFrame);
		drawRoleNames(athenaAiFrame);
		return athenaAiFrame;
	}


	private void drawRoleNames(final AthenaAiFrame aiFrame)
	{
		List<IDrawableShape> shapes = aiFrame.getShapeMap().get(EAiShapesLayer.AI_ROLE_NAMES);
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


	private void updatePlaysAndRoles(final AthenaAiFrame frame)
	{
		Set<APlay> plays = frame.getPlayStrategy().getActivePlays();
		try
		{
			plays.forEach(play -> play.getRoles().forEach(role -> role.updateBefore(frame)));
			plays.forEach(play -> play.updateBeforeRoles(frame));
			plays.forEach(play -> play.getRoles().forEach(role -> role.update(frame)));
			plays.forEach(APlay::updateAfterRoles);
		} catch (Exception err)
		{
			log.error("Exception during athena update!", err);
		}
	}
}
