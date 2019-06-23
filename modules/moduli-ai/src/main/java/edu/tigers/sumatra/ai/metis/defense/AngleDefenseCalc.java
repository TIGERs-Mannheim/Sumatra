/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 27, 2016
 * Author(s): Felix Bayer <bayer.fel@googlemail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.defense;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.ITacticalField;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.defense.algorithms.ExtensiveFoeBotCalc;
import edu.tigers.sumatra.ai.metis.defense.algorithms.interfaces.IFoeBotCalc;
import edu.tigers.sumatra.ai.metis.defense.data.AngleDefenseData;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseAux;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotData;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotGroup;
import edu.tigers.sumatra.drawable.DrawableArc;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Arc;
import edu.tigers.sumatra.shapes.rectangle.IRectangle;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * @author Felix Bayer <bayer.fel@googlemail.com>
 * @brief Aims to create a line of defenders which provide a more coordinated defense than earlier defense calculators.
 *        Uses the angle between foe bots and goal center to generate defense points on these vectors.
 */
public class AngleDefenseCalc extends ACalculator
{
	
	private final IFoeBotCalc	foeBotCalc		= new ExtensiveFoeBotCalc();
	
	@Configurable(comment = "Defenders will not drive behind this x value")
	private static double		maxXDefender	= -(Geometry.getCenterCircleRadius() + (1.5f * Geometry.getBotRadius()));
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		AngleDefenseData angleDefenseData = newTacticalField.getAngleDefenseData();
		List<IDrawableShape> shapeList = newTacticalField.getDrawableShapes().get(EShapesLayer.ANGLE_DEFENSE);
		EGameStateTeam gameState = newTacticalField.getGameState();
		
		boolean invertPossessesBall = false;
		
		if (DefenseAux.neverBlockStaticDefensive
				&& DefenseAux.isAnyStaticDefensiveSituation(gameState))
		{
			invertPossessesBall = true;
		} else if (DefenseAux.isFoeCorner(gameState,
				baseAiFrame.getWorldFrame().getBall()) && DefenseAux.spareDirectShotDefActivated)
		{
			invertPossessesBall = true;
		}
		
		List<FoeBotData> foeBotDataList = foeBotCalc.getFoeBotData(newTacticalField, baseAiFrame);
		
		List<FoeBotData> filteredFoeBotDataList = null;
		
		if (DefenseAux.onlyDefendersInStaticSituations && DefenseAux.isStaticDefenseIncludingKickoff(gameState))
		{
			filteredFoeBotDataList = foeBotDataList;
		} else if (DefenseAux.maxDefendersAtKickoff && (gameState == EGameStateTeam.PREPARE_KICKOFF_THEY))
		{
			filteredFoeBotDataList = foeBotDataList;
		} else
		{
			filteredFoeBotDataList = foeBotDataList.stream()
					.filter(bot -> bot.getFoeBot().getPos().x() < DefenseAux.ignoreEnemyBotsLine)
					.collect(Collectors.toList());
		}
		
		List<FoeBotGroup> foeBotGroups = buildFoeGroups(baseAiFrame.getPrevFrame().getTacticalField(),
				filteredFoeBotDataList, AngleDefenseData.getBotWidthAngle(),
				invertPossessesBall);
		
		if ((EAIControlState.MIXED_TEAM_MODE == baseAiFrame.getPrevFrame().getPlayStrategy().getAIControlState())
				&& DefenseAux.doNotDefendBallInMixedTeamMode)
		{
			foeBotGroups = foeBotGroups.stream().filter(group -> !group.possessesBall()).collect(Collectors.toList());
		}
		
		if (foeBotGroups.size() == 0)
		{
			FoeBotGroup dummyGroup = new FoeBotGroup(invertPossessesBall);
			dummyGroup.setForcePossessesBall(true);
			
			foeBotGroups.add(dummyGroup);
		}
		
		angleDefenseData.addFoeBotGroups(foeBotGroups);
		drawFoeBotGroups(foeBotGroups, shapeList);
		
		newTacticalField.getDangerousFoeBots().addAll(filteredFoeBotDataList);
		
		createUsefulDrawings(newTacticalField, baseAiFrame);
	}
	
	
	/**
	 * @param foeBotGroups
	 * @param shapeList
	 */
	private void drawFoeBotGroups(final List<FoeBotGroup> foeBotGroups, final List<IDrawableShape> shapeList)
	{
		for (FoeBotGroup group : foeBotGroups)
		{
			if (group.nMember() < 1)
			{
				continue;
			}
			
			List<FoeBotData> bots = group.getMember();
			double margin = Geometry.getBotRadius();
			
			FoeBotData mostNorthBot = bots.stream()
					.reduce((a, b) -> a.getFoeBot().getPos().x() > b.getFoeBot().getPos().x() ? a : b).get();
			FoeBotData mostEastBot = bots.stream()
					.reduce((a, b) -> a.getFoeBot().getPos().y() > b.getFoeBot().getPos().y() ? a : b).get();
			FoeBotData mostSouthBot = bots.stream()
					.reduce((a, b) -> a.getFoeBot().getPos().x() < b.getFoeBot().getPos().x() ? a : b).get();
			FoeBotData mostWestBot = bots.stream()
					.reduce((a, b) -> a.getFoeBot().getPos().y() < b.getFoeBot().getPos().y() ? a : b).get();
			
			IRectangle groupRect = new Rectangle(new Vector2(mostNorthBot.getFoeBot().getPos().x() + margin,
					mostWestBot.getFoeBot().getPos().y() - margin),
					new Vector2(mostSouthBot.getFoeBot().getPos().x() - margin,
							mostEastBot.getFoeBot().getPos().y() + margin));
			
			shapeList.add(new DrawableRectangle(groupRect, Color.RED));
		}
	}
	
	
	/**
	 * @param prevTacField
	 * @param foeBotDataList
	 * @param botWidthAngle
	 */
	private List<FoeBotGroup> buildFoeGroups(final ITacticalField prevTacField, final List<FoeBotData> foeBotDataList,
			final double botWidthAngle,
			final boolean invertPossesssesBall)
	{
		List<FoeBotGroup> foeGroups = new ArrayList<>();
		double modifiedBotWidthAngle = botWidthAngle;
		
		List<FoeBotGroup> prevGroups = prevTacField.getAngleDefenseData().getFoeBotGroups();
		
		foeBotDataList.sort(FoeBotData.ANGLE_COMPARATOR);
		
		FoeBotGroup curGroup = new FoeBotGroup(invertPossesssesBall);
		FoeBotData lastEntry = null;
		for (FoeBotData curFoeBot : foeBotDataList)
		{
			
			if (DefenseAux.flexibleGroupsActivated)
			{
				if ((lastEntry != null) && bothFoeBotDataInSameGroup(prevGroups, lastEntry, curFoeBot))
				{
					modifiedBotWidthAngle = DefenseAux.upperFlexibleGroupFactor * botWidthAngle;
				} else
				{
					modifiedBotWidthAngle = DefenseAux.lowerFlexibleGroupFactor * botWidthAngle;
				}
			} else
			{
				modifiedBotWidthAngle = botWidthAngle;
			}
			
			if ((lastEntry != null)
					&& (Math.abs(curFoeBot.getGoalAngle() - lastEntry.getGoalAngle()) > modifiedBotWidthAngle))
			{
				foeGroups.add(curGroup);
				curGroup = new FoeBotGroup(invertPossesssesBall);
			}
			
			curGroup.addMember(curFoeBot);
			lastEntry = curFoeBot;
		}
		if (curGroup.nMember() > 0)
		{
			foeGroups.add(curGroup);
		}
		
		foeGroups.sort(FoeBotGroup.ANGLE);
		
		if (foeGroups.size() > 0)
		{
			foeGroups.get(0).setFirst(true);
			foeGroups.get(foeGroups.size() - 1).setLast(true);
		}
		
		foeGroups.sort(FoeBotGroup.PRIORITY);
		
		return foeGroups;
	}
	
	
	private boolean bothFoeBotDataInSameGroup(final List<FoeBotGroup> groups, final FoeBotData botA,
			final FoeBotData botB)
	{
		for (FoeBotGroup group : groups)
		{
			List<BotID> botIDs = group.getMember().stream().map(data -> data.getFoeBot().getBotId())
					.collect(Collectors.toList());
			
			if (botIDs.contains(botA.getFoeBot().getBotId()) && botIDs.contains(botB.getFoeBot().getBotId()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	private void createUsefulDrawings(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		IVector2 goalCenter = Geometry.getGoalOur().getGoalCenter();
		TrackedBall ball = baseAiFrame.getWorldFrame().getBall();
		IVector2 ballPos = DefenseAux.getBallPosDefense(ball);
		
		List<IDrawableShape> shapeList = newTacticalField.getDrawableShapes().get(EShapesLayer.ANGLE_DEFENSE);
		final double pi = Math.PI;
		List<FoeBotData> foeBotDataList = newTacticalField.getDangerousFoeBots();
		
		Arc defenseArcMin = new Arc(goalCenter, AngleDefenseData.getMinDefRadius(), -pi / 2, pi);
		
		IDrawableShape goalCircleMin = new DrawableArc(defenseArcMin, Color.DARK_GRAY);
		
		shapeList.add(goalCircleMin);
		
		for (FoeBotData foeBotData : foeBotDataList)
		{
			ILine line = new Line(foeBotData.getFoeBot().getPos(), foeBotData.getBot2goal());
			IDrawableShape drawableLine = new DrawableLine(line, Color.LIGHT_GRAY);
			shapeList.add(drawableLine);
			
			List<IVector2> arcIntersectionsMin = defenseArcMin.lineIntersections(line);
			
			for (IVector2 intersec : arcIntersectionsMin)
			{
				IDrawableShape point = new DrawablePoint(intersec, Color.ORANGE);
				shapeList.add(point);
			}
		}
		
		ILine line = new Line(ballPos, goalCenter.subtractNew(ballPos));
		
		List<IVector2> arcIntersectionsMin = defenseArcMin.lineIntersections(line);
		
		for (IVector2 intersec : arcIntersectionsMin)
		{
			IDrawableShape point = new DrawablePoint(intersec, Color.CYAN);
			shapeList.add(point);
		}
		
		List<IVector2> arcIntersectionsMax = defenseArcMin.lineIntersections(line);
		
		for (IVector2 intersec : arcIntersectionsMax)
		{
			IDrawableShape point = new DrawablePoint(intersec, Color.CYAN);
			shapeList.add(point);
		}
		
	}
	
}
