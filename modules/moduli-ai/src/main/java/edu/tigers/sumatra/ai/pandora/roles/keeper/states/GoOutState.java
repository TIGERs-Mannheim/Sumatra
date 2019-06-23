/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import java.awt.*;
import java.util.List;
import java.util.Optional;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.MoveToTrajSkill;


/**
 * If there is an enemy near our penalty area with the ball, the Keeper should go out to block a shoot on the goal
 *
 * @author ChrisC
 */
public class GoOutState extends AKeeperState {

    private MoveToTrajSkill posSkill;


    /**
     * @param parent : the parent role
     */
    public GoOutState(KeeperRole parent) {
        super(parent);
    }

    /**
     * Calc the position within
     *
     * @param posToCover
     * @return the position
     */
    static IVector2 calcBestDefensivePositionInPE(IVector2 posToCover) {
        Optional<IVector2> poleCoveringPosition = calcPositionAtPoleCoveringWholeGoal(posToCover);
        IVector2 targetPosition;
        if (poleCoveringPosition.isPresent()) {
            targetPosition = poleCoveringPosition.get();
        } else {
            IVector2 posCoveringWholeGoal = calcPositionCoveringWholeGoal(posToCover);
            if (isPositionInPenaltyArea(posCoveringWholeGoal)) {
                targetPosition = posCoveringWholeGoal;
            } else if (isTargetPositionOutsideOfField(posCoveringWholeGoal)) {
                targetPosition = calcNearestGoalPostPosition(posToCover);
            } else {
                targetPosition = calcPositionBehindPenaltyArea(posToCover);
            }
        }
        return targetPosition;
    }

    private static IVector2 calcPositionBehindPenaltyArea(IVector2 posToCover) {
        IVector2 goalCenter = Geometry.getGoalOur().getCenter();
        ILine ballGoalCenter = Line.fromPoints(goalCenter, posToCover);

        List<IVector2> intersectionsList = Geometry.getPenaltyAreaOur().lineIntersections(ballGoalCenter);

        IVector2 intersection = intersectionsList.stream()
                .max((a, b) -> VectorMath.distancePP(a, posToCover) < VectorMath.distancePP(b, posToCover) ? 1 : -1)
                .orElse(posToCover);

        double stepSize = VectorMath.distancePP(goalCenter,
                intersection) - ((2 * Geometry.getBallRadius()) + Geometry.getBotRadius());
        return LineMath.stepAlongLine(goalCenter, intersection, stepSize);
    }

    private static IVector2 calcNearestGoalPostPosition(IVector2 posToCover) {
        IVector2 leftPole = Geometry.getGoalOur().getLeftPost();
        IVector2 rightPole = Geometry.getGoalOur().getRightPost();

        IVector2 nearestPole;
        if (posToCover.y() < 0) {
            nearestPole = rightPole;
        } else {
            nearestPole = leftPole;
        }
        return LineMath.stepAlongLine(nearestPole, Geometry.getCenter(),
                Geometry.getBotRadius() + Geometry.getBallRadius());
    }

    private static boolean isTargetPositionOutsideOfField(IVector2 targetPos) {
        return !Geometry.getField().isPointInShape(targetPos);
    }

    private static boolean isPositionInPenaltyArea(IVector2 targetPos) {
        return Geometry.getPenaltyAreaOur().isPointInShape(targetPos,
                -((2 * Geometry.getBallRadius()) + Geometry.getBotRadius()));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static IVector2 calcPositionCoveringWholeGoal(IVector2 posToCover) {
        IVector2 goalCenter = Geometry.getGoalOur().getCenter();
        IVector2 leftPole = Geometry.getGoalOur().getLeftPost();
        IVector2 rightPole = Geometry.getGoalOur().getRightPost();

        IVector2 ballGoalOrthoDirection = Line.fromPoints(posToCover, goalCenter).directionVector().getNormalVector();
        ILine ballGoalLineOrtho = Line.fromDirection(goalCenter, ballGoalOrthoDirection);
        ILine ballLeftPose = Line.fromPoints(posToCover, leftPole);
        ILine ballRightPose = Line.fromPoints(posToCover, rightPole);

        double distLM = VectorMath.distancePP(goalCenter,
                LineMath.intersectionPoint(ballGoalLineOrtho, ballLeftPose).get());

        double distRM = VectorMath.distancePP(goalCenter,
                LineMath.intersectionPoint(ballGoalLineOrtho, ballRightPose).get());

        double relativeRadius = (2 * Geometry.getBotRadius() * distLM) / (distLM + distRM);

        double alpha = ballLeftPose.directionVector().angleToAbs(ballGoalOrthoDirection).orElse(0.0);
        // angle should be less than 90° = pi/2
        if ((alpha > (AngleMath.PI / 2)) && (alpha < AngleMath.PI)) {
            alpha = AngleMath.PI - alpha;
        }

        IVector2 optimalDistanceToBallPosDirectedToGoal = LineMath.stepAlongLine(posToCover,
                goalCenter, relativeRadius * AngleMath.tan(alpha));

        IVector2 optimalDistanceToBallPosDirectedToRightPose = LineMath.intersectionPoint(ballRightPose,
                Line.fromDirection(optimalDistanceToBallPosDirectedToGoal, ballGoalOrthoDirection)).get();

        return LineMath.stepAlongLine(optimalDistanceToBallPosDirectedToRightPose, optimalDistanceToBallPosDirectedToGoal, Geometry.getBotRadius());
    }

    private static Optional<IVector2> calcPositionAtPoleCoveringWholeGoal(IVector2 posToCover) {
        IVector2 leftPole = Geometry.getGoalOur().getLeftPost();
        IVector2 rightPole = Geometry.getGoalOur().getRightPost();

        double distanceRightPole = Line.fromPoints(posToCover, leftPole).distanceTo(rightPole);
        double distanceLeftPole = Line.fromPoints(posToCover, rightPole).distanceTo(leftPole);

        boolean isPositionAtLeftPoleCoverGoal = distanceLeftPole <= Geometry.getBotRadius() * 2 && posToCover.y() > 0;
        boolean isPositionAtRightPoleCoverGoal = distanceRightPole <= Geometry.getBotRadius() * 2 && posToCover.y()<0;

        Optional<IVector2> pole = Optional.empty();

        if (isPositionAtLeftPoleCoverGoal) {
            pole = Optional.of(leftPole);
        } else if (isPositionAtRightPoleCoverGoal) {
            pole = Optional.of(rightPole);
        }

        Optional<IVector2> coveringPosition = Optional.empty();
        if (pole.isPresent()) {
            coveringPosition = Optional.of(LineMath.stepAlongLine(pole.get(), Geometry.getCenter(),
                    Geometry.getBotRadius() + Geometry.getBallRadius()));
        }
        return coveringPosition;
    }

    @Override
    public void doEntryActions() {
        posSkill = new MoveToTrajSkill();
        posSkill.getMoveCon().getMoveConstraints().setAccMax(KeeperRole.getKeeperAcc());
        posSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
        posSkill.getMoveCon().setDestinationOutsideFieldAllowed(true);
        posSkill.getMoveCon().setBotsObstacle(false);
        posSkill.getMoveCon().setBallObstacle(false);
        posSkill.getMoveCon().setArmChip(false);
        setNewSkill(posSkill);
    }

    @Override
    public void doUpdate() {
        IVector2 targetPosition;
        if (isBallInsidePenaltyArea()) {
            targetPosition = calcPositionBehindBall();
        } else {
            targetPosition = calcBestDefensivePositionInPE(getWFrame().getBall().getPos());
        }
        updateKeeperPosition(targetPosition, calcPositionKeeperLookAt());
        drawKeeperShapes(targetPosition);
    }

    private boolean isBallInsidePenaltyArea() {
        return Geometry.getPenaltyAreaOur().isPointInShape(getWFrame().getBall().getPos(), -Geometry.getBotRadius() * 2);
    }

    private IVector2 calcPositionBehindBall() {
        IVector2 ballPos = getWFrame().getBall().getPos();
        IVector2 goalCenter = Geometry.getGoalOur().getCenter();
        double distanceToBall = goalCenter.distanceTo(ballPos);
        return LineMath.stepAlongLine(goalCenter, ballPos, distanceToBall - Geometry.getBotRadius());
    }

    private void updateKeeperPosition(IVector2 targetPosition, IVector2 lookAt) {
        posSkill.getMoveCon().updateDestination(targetPosition);
        posSkill.getMoveCon().updateLookAtTarget(lookAt);


    }

    private IVector2 calcPositionKeeperLookAt() {
        return LineMath.stepAlongLine(Geometry.getGoalOur().getCenter(),
				getPos(), Geometry.getPenaltyAreaOur().getRadius() * 3);
    }

    private void drawKeeperShapes(IVector2 targetPose) {
        getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.KEEPER)
                .add(new DrawableLine(Line.fromPoints(getAiFrame().getWorldFrame().getBall().getPos(), Geometry.getGoalOur().getLeftPost())));
        getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.KEEPER)
                .add(new DrawableLine(Line.fromPoints(getAiFrame().getWorldFrame().getBall().getPos(), Geometry.getGoalOur().getRightPost())));
        getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.KEEPER)
                .add(new DrawableCircle(targetPose, 30, Color.green));
    }

}
