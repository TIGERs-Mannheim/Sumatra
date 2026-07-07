package edu.tigers.sumatra.ai.pandora.plays.standard;

import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.bot.ERobotHealthState;
import edu.tigers.sumatra.botmanager.botskills.data.ESong;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class RobotInterchangePlay extends AMaintenancePlay
{
	private static final int LOOK_OUTSIDE_FIELD_ANGLE = 90;
	@Configurable(comment = "Distance between bots in interchange lineup", defValue = "250.0", unit = EConfigUnit.DISTANCE_MM)
	private static double distanceBetweenBotsInInterchangePosition = 250;

	@Configurable(comment = "Factor to reduce maxVelW for interchange behaviour.", defValue = "0.1", unit = EConfigUnit.FACTOR)
	private static double maxVelWFactor = 0.1;

	@Configurable(comment = "Factor to reduce maxAccW for interchange behaviour", defValue = "0.1", unit = EConfigUnit.FACTOR)
	private static double maxAccWFactor = 0.1;

	@Configurable(comment = "Max turning angle for interchange bots that have health state DEGRADED", defValue = "12.0", unit = EConfigUnit.ANGLE_DEG)
	private static double maxTurnAngle = 12;

	@Configurable(comment = "Distance from field line for interchangeable bots (may be negative)", defValue = "0.0", unit = EConfigUnit.DISTANCE_MM)
	private static double distanceToFieldLine = 0.0;

	@Configurable(comment = "Position for interchangeable bots in vision coordinates", defValue = "NEG_Y", unit = EConfigUnit.NO_UNIT)
	private static VisionSide positionInVision = VisionSide.NEG_Y;

	@Configurable(comment = "Offset factor to centerLine", defValue = "0.0", unit = EConfigUnit.FACTOR)
	private static double offsetFactorToCenterline = 0.0;

	@Configurable(comment = "Margin of places for bots in substitution area", defValue = "20", unit = EConfigUnit.DISTANCE_MM)
	private static int botMarginInSubstitutionArea = 20;

	@Configurable(comment = "Which interchange logic is used, false: drive to field line and wiggle, true: drive in substitution area", defValue = "true", unit = EConfigUnit.BOOLEAN)
	private static boolean interchangeInGoalSubstitutionArea = true;

	private IVector2 lineupDirectionVector = Vector2.fromXY(1, 0)
			.scaleTo(distanceBetweenBotsInInterchangePosition);

	private List<MoveRole> orderedBots = new ArrayList<>();

	private int currentTurnFactor = 1;


	/**
	 * Create a new interchange play
	 */
	public RobotInterchangePlay()
	{
		super(EPlay.INTERCHANGE);
	}


	@Override
	protected void doUpdateBeforeRoles()
	{
		super.doUpdateBeforeRoles();

		for (var role : findRoles(MoveRole.class))
		{
			if (Geometry.getGoalSubstitutionAreaOur().isPointInShape(role.getPos()))
			{
				role.setVelMax(1.00);
				role.setMoveCon(role.getMoveCon()
						.withIgnoredBots(getRoles().stream().map(ARole::getBotID).collect(Collectors.toSet())));
			}
		}
	}
	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();
		if (interchangeInGoalSubstitutionArea && substitutionAreaBigEnough())
		{
			calculateBotActionsGoalSubstitutionArea(
					computeSupportVectorForLineupSubstitutionArea()
			);
		} else
		{
			calculateBotActionsCenterSubstitutionArea(
					computeSupportVectorForLineup(getRoles().size()),
					lineupDirectionVector
			);
		}
	}


	private boolean substitutionAreaBigEnough()
	{
		return Geometry.getGoalSubstitutionAreaOur().xExtent() >= 2.5 * Geometry.getBotRadius();
	}


	private double computeWiggleOrientation(final double currentOrientation)
	{
		return (currentOrientation + (maxTurnAngle * currentTurnFactor));
	}


	private void moveBotActionsInterchange(IVector2 direction, MoveRole role, IVector2 dest)
	{
		role.setVelMaxW(role.getBot().getMoveConstraints().getVelMaxW() * maxVelWFactor);
		role.setAccMaxW(role.getBot().getMoveConstraints().getAccMaxW() * maxAccWFactor);
		role.disableMotorsWhenDestReached();

		if (interchangeInGoalSubstitutionArea && substitutionAreaBigEnough())
		{
			var tmpOrientation = AngleMath.PI;
			if (role.getBot().getRobotInfo().getHealthState()
					.equals(ERobotHealthState.DEGRADED)) //wiggle only when bot health degraded
			{
				tmpOrientation = role.isDestinationReached() ? computeWiggleOrientation(tmpOrientation) : tmpOrientation;
				getAiFrame().getBaseAiFrame().getMultimediaControl().get(role.getBotID()).setSong(ESong.FINAL_COUNTDOWN);
			}
			role.updateTargetAngle(tmpOrientation);
		} else
		{
			while (!pointChecker.allMatch(getAiFrame().getBaseAiFrame(), dest, role.getBotID()))
			{
				dest = dest.addNew(direction);
			}
			var tmpOrientation = LOOK_OUTSIDE_FIELD_ANGLE * Math.PI / 180 * positionInVision.factor * getMirrorFactor();
			tmpOrientation = role.isDestinationReached() ?
					computeWiggleOrientation(tmpOrientation) :
					tmpOrientation; //wiggle
			role.updateTargetAngle(tmpOrientation);
			getAiFrame().getBaseAiFrame().getMultimediaControl().get(role.getBotID()).setSong(ESong.FINAL_COUNTDOWN);
		}
		role.updateDestination(dest); //drive to new dest when not already there
	}


	/**
	 * This code ensures that currentBots contains only the roles present in roles.
	 * It adds any missing roles from roles to currentBots
	 * and then removes any roles from currentBots that are not in roles.
	 */
	private void sortPlacesIntoBotOrder(List<IVector2> substitutionAreaBotPlaces)
	{
		var roles = findRoles(MoveRole.class);
		for (MoveRole role : roles)
		{
			if (!orderedBots.contains(role))
			{
				orderedBots.add(role);
			}
		}
		orderedBots.removeIf(role -> !roles.contains(role));
		ArrayList<MoveRole> oBots = new ArrayList<>();
		var sAreas = substitutionAreaBotPlaces.stream().toList();
		for (IVector2 sArea : sAreas)
		{
			if (!orderedBots.isEmpty())
			{
				var minDistanceBot = orderedBots.stream()
						.min(Comparator.comparingDouble(r -> r.getPos().distanceTo(sArea)))
						.stream().toList().getFirst();
				oBots.add(minDistanceBot);
				orderedBots.remove(minDistanceBot);
			} else
			{
				break;
			}
		}
		orderedBots = oBots;
	}


	private void calculateBotActionsGoalSubstitutionArea(IVector2 startPos)
	{
		List<IVector2> substitutionAreaBotPlaces = new ArrayList<>();

		var availableSubstitutionAreaLength = (Geometry.getFieldWidth() / 2 - startPos.y());
		int numPlacesInSubstitutionArea = (int) (Math.floor(
				(availableSubstitutionAreaLength - botMarginInSubstitutionArea * 2)
						/ distanceBetweenBotsInInterchangePosition));
		for (int i = 0; i < numPlacesInSubstitutionArea; i++)
		{
			var point = Vector2.fromXY(
					Geometry.getFieldWBorders().minX() + Geometry.getBotRadius() / 2 + 40,
					positionInVision.factor * getMirrorFactor() * (
							startPos.y() + availableSubstitutionAreaLength - (
									botMarginInSubstitutionArea
											+ i * distanceBetweenBotsInInterchangePosition))
			);
			if (getAiFrame().getGameState().isStoppedGame())
			{
				if (pointChecker.allMatch(getAiFrame().getBaseAiFrame(), point, BotID.noBot()))
				{
					substitutionAreaBotPlaces.add(point);
				}
			} else
			{
				substitutionAreaBotPlaces.add(point);
			}
		}

		sortPlacesIntoBotOrder(substitutionAreaBotPlaces);

		boolean destsReached = true;
		for (int i = 0; i < orderedBots.size(); i++)
		{
			IVector2 direction = Vector2.fromXY(0, Math.signum(orderedBots.get(i).getPos().y()))
					.scaleTo(distanceBetweenBotsInInterchangePosition);

			moveBotActionsInterchange(direction, orderedBots.get(i), substitutionAreaBotPlaces.get(i));
			destsReached = destsReached && orderedBots.get(i).isTargetReached();
		}
		if (destsReached)
		{
			currentTurnFactor *= -1;
		}
	}


	/**
	 * Compute bot actions based on a line defined by start pos and direction vector
	 *
	 * @param startPos
	 * @param direction
	 */
	private void calculateBotActionsCenterSubstitutionArea(IVector2 startPos, IVector2 direction)
	{
		List<MoveRole> roles = findRoles(MoveRole.class);
		IVector2 dest = startPos.subtractNew(direction);

		for (MoveRole role : roles)
		{
			moveBotActionsInterchange(direction, role, dest);
		}
	}


	private int getMirrorFactor()
	{
		return getAiFrame().getRefereeMsg().getNegativeHalfTeam() != getAiFrame().getTeamColor() ? -1 : 1;
	}


	private IVector2 computeSupportVectorForLineup(final int nBots)
	{
		double y = (Geometry.getField().maxY() + distanceToFieldLine) * getMirrorFactor();
		double x = ((offsetFactorToCenterline * Geometry.getFieldLength()) / 2)
				+ ((nBots * distanceBetweenBotsInInterchangePosition) / 2);
		return Vector2.fromXY(-x, positionInVision.factor * y);
	}


	private IVector2 computeSupportVectorForLineupSubstitutionArea()
	{
		double y = Geometry.getPenaltyAreaOur().getRectangle().minY();
		double x = Geometry.getGoalSubstitutionAreaOur().center().x();
		return Vector2.fromXY(x, positionInVision.factor * y);
	}


	@AllArgsConstructor
	enum VisionSide
	{
		POS_Y(1),
		NEG_Y(-1),

		;
		private final double factor;
	}
}
