/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.standard;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.standard.cheerings.ECheeringPlays;
import edu.tigers.sumatra.ai.pandora.plays.standard.cheerings.ICheeringPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.botmanager.botskills.data.ELedColor;
import edu.tigers.sumatra.botmanager.botskills.data.ESong;
import edu.tigers.sumatra.botmanager.data.MultimediaControl;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class CheeringPlay extends APlay
{
	private List<ICheeringPlay> plays = new ArrayList<>();
	private ICheeringPlay activePlay;
	private EPlayState state;
	private List<IVector2> firstDestinations;
	private Map<BotID, Integer> positionPermutation = new HashMap<>();
	private TimestampTimer timerBeforeStart = new TimestampTimer(0.3);
	@Setter
	private ECheeringPlays selectedPlay;


	public CheeringPlay()
	{
		super(EPlay.CHEERING);
	}


	public void setSong(BotID botID, ESong song)
	{
		MultimediaControl multimediaControl = getAiFrame().getBaseAiFrame().getMultimediaControl().get(botID);
		if (multimediaControl == null)
		{
			return;
		}

		multimediaControl.setSong(song);
	}


	public void setEyeColor(BotID botID, ELedColor color)
	{
		MultimediaControl multimediaControl = getAiFrame().getBaseAiFrame().getMultimediaControl().get(botID);
		if (multimediaControl == null)
		{
			return;
		}

		multimediaControl.setLedColor(color);
	}

	@Override
	protected void onNumberOfBotsChanged()
	{
		activePlay = null;
	}


	private List<MoveRole> getMoveRoles()
	{
		return getRoles().stream()
				.filter(MoveRole.class::isInstance)
				.map(MoveRole.class::cast)
				.toList();
	}


	public List<MoveRole> getPermutedRoles()
	{
		return getMoveRoles().stream()
				.sorted(Comparator.comparingInt(r -> positionPermutation.get(r.getBotID())))
				.toList();
	}


	public void addShape(IDrawableShape shape)
	{
		getShapes(EAiShapesLayer.TEST_CHEERING_PLAY_DEBUG).add(shape);
	}


	private void resetBotsAndPermutation()
	{
		var count = 0;
		positionPermutation.clear();
		for (var role : findRoles(MoveRole.class))
		{
			role.getMoveCon().physicalObstaclesOnly();
			role.getMoveCon().setBallObstacle(false);
			setSong(role.getBotID(), ESong.NONE);

			positionPermutation.put(role.getBotID(), count);
			++count;
		}
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();
		if (getRoles().size() < 3)
		{
			return;
		}

		if (activePlay == null || activePlay.isDone())
		{
			activePlay = getNextPlay();
			if (activePlay == null)
			{
				return;
			}
			firstDestinations = activateNewPlay(activePlay);
		}
		addShape(new DrawableBorderText(Vector2.fromXY(0, 10),
				plays.stream().map(ICheeringPlay::getType).toList().toString()));

		state = switch (state)
		{
			case DRIVING_TO_FIRST_POSITION -> updateDrivingToFirstDestination(firstDestinations);
			case ON_POSITION_WAITING -> updateOnPosition();
			case CHEERING -> updateCheering(activePlay);
		};
	}


	private ICheeringPlay getNextPlay()
	{
		if (plays.isEmpty())
		{
			plays = ECheeringPlays.getShuffledList();
		}

		ICheeringPlay play;
		if (selectedPlay != null)
		{
			play = (ICheeringPlay) selectedPlay.getInstanceableClass().newInstance();
		} else
		{
			play = plays.removeFirst();
		}
		if (play.requiredNumRobots() <= getMoveRoles().size())
		{
			return play;
		} else
		{
			return null;
		}
	}


	private List<IVector2> activateNewPlay(ICheeringPlay play)
	{
		resetBotsAndPermutation();

		getMoveRoles().forEach(role -> role.getMoveCon().setIgnoredBots(Set.of()));

		state = EPlayState.DRIVING_TO_FIRST_POSITION;
		play.initialize(this);
		var destinations = play.calcPositions();
		var roles = getMoveRoles();
		for (var role : roles)
		{
			role.updateDestination(destinations.get(positionPermutation.get(role.getBotID())));
		}
		return destinations;
	}


	private EPlayState updateOnPosition()
	{
		var now = getWorldFrame().getTimestamp();
		if (!timerBeforeStart.isRunning())
		{
			timerBeforeStart.start(now);
		}
		if (timerBeforeStart.isTimeUp(now))
		{
			timerBeforeStart.reset();
			return EPlayState.CHEERING;
		}
		return EPlayState.ON_POSITION_WAITING;
	}


	private EPlayState updateCheering(ICheeringPlay play)
	{
		play.doUpdate();
		var rolesToIgnore = getRoles().stream().map(ARole::getBotID).collect(Collectors.toUnmodifiableSet());
		getMoveRoles().forEach(role -> role.getMoveCon().setIgnoredBots(rolesToIgnore));
		return EPlayState.CHEERING;
	}


	private EPlayState hasReachedPosition()
	{
		var roles = getMoveRoles();
		for (var aRole : roles)
		{
			MoveRole role = aRole;
			if (!role.isDestinationReached())
			{
				return EPlayState.DRIVING_TO_FIRST_POSITION;
			}
		}
		return EPlayState.ON_POSITION_WAITING;
	}


	private EPlayState updateDrivingToFirstDestination(List<IVector2> firstDestinations)
	{
		var permutedBots = new HashSet<BotID>();
		for (var role : getMoveRoles())
		{
			if (role.isDestinationReached()
					|| permutedBots.contains(role.getBotID())
					|| role.getBot().getVel().getLength() > 0.5)
			{
				continue;
			}

			var firstBlockingRole = getMoveRoles().stream()
					.filter(MoveRole::isDestinationReached)
					.filter(standing -> isRoleInTheWay(role, standing))
					.min(Comparator.comparingDouble(standing -> standing.getPos().distanceToSqr(role.getPos())));

			firstBlockingRole.ifPresent(blocking -> {
				var tmpIndex = positionPermutation.get(blocking.getBotID());
				positionPermutation.compute(blocking.getBotID(), (id, i) -> positionPermutation.get(role.getBotID()));
				positionPermutation.compute(role.getBotID(), (id, i) -> tmpIndex);
				permutedBots.add(blocking.getBotID());
				permutedBots.add(role.getBotID());
			});
		}
		setDestinations(firstDestinations);

		return hasReachedPosition();
	}


	private void setDestinations(List<IVector2> destinations)
	{
		Validate.isTrue(destinations.size() == getRoles().size());
		for (var role : getMoveRoles())
		{
			var destinationIndex = positionPermutation.get(role.getBotID());
			role.updateDestination(destinations.get(destinationIndex));
		}
	}


	private boolean isRoleInTheWay(MoveRole movingRole, MoveRole standingRole)
	{
		return Lines.segmentFromPoints(movingRole.getPos(), movingRole.getDestination())
				.distanceTo(standingRole.getDestination()) < 2 * Geometry.getBotRadius();
	}


	private enum EPlayState
	{
		DRIVING_TO_FIRST_POSITION,
		ON_POSITION_WAITING,
		CHEERING,
	}
}
