/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.ExtendedPenaltyArea;
import edu.tigers.sumatra.statemachine.IState;


/**
 * @author JonasH
 */

public class MoveOnPenaltyAreaSkill extends AMoveSkill {

	@Configurable(comment = "when destination is within this distance, do not use an intermediatePos anymore", defValue = "600.0")
	private static double startBreakingDistance = 600.0;
    @Configurable(comment = "the distance the intermediatePos is prefixed from the offset maximum of an interfering bot", defValue = "300.0")
    private static double understeerWhenByPassingBot = 300.0;
    @Configurable(comment = "forget current interfering bot when this proportion of offset has been reached", defValue = "0.5")
    private static double proportionOfOffsetMustBeenReached = 0.5;

    private IVector2 destination;
    private ExtendedPenaltyArea extendedPenArea;


    /**
     * @param distanceToPenArea
     */
    public MoveOnPenaltyAreaSkill(final double distanceToPenArea) {
        super(ESkill.MOVE_ON_PENALTY_AREA);
        extendedPenArea = new ExtendedPenaltyArea(Geometry.getPenaltyAreaOur().getRadius() + distanceToPenArea);
        IState moveOnPenAreaState = new MoveOnPenaltyAreaState();
        setInitialState(moveOnPenAreaState);
    }


    /**
     * Setting destination
     *
     * @param destination
     */
    public void updateDestination(final IVector2 destination) {
        this.destination = destination;
    }


    /**
     * Creates new ExtendedPenaltyArea with extended Radius
     *
     * @param distanceToPenArea
     */
    public void updateDistanceToPenArea(final double distanceToPenArea) {
        extendedPenArea = new ExtendedPenaltyArea(Geometry.getPenaltyAreaOur().getRadius() + distanceToPenArea);
    }


    private class MoveOnPenaltyAreaState extends MoveToState {
        protected MoveOnPenaltyAreaState() {
            super(MoveOnPenaltyAreaSkill.this);
        }


        @Override
        public void doUpdate() {
            extendedPenArea.updatePenArea(getWorldFrame(), getShapes());
            IVector2 targetPos = getTargetPosByDest();

            MoveOnPenaltyAreaSkill.this.setTargetPose(targetPos, getAngleByOurGoal(targetPos));
        }


        private double getAngleByOurGoal(final IVector2 targetPos) {
            IVector2 projectedPoint = extendedPenArea.pointWithOffset(targetPos, 0);
            IVector2 projectedPointWithOffset = extendedPenArea.pointWithOffset(targetPos, 100);
            return Vector2.fromPoints(projectedPoint, projectedPointWithOffset).getAngle();
        }


        private IVector2 getTargetPosByDest() {
            final IVector2 currentPos = getPos();
            // required for test mode, redundant for match mode when destination is on penalty area anyway
            final IVector2 destinationProjection = extendedPenArea.projectPointOnPenaltyAreaLine(destination);
            final double distanceToTarget = Vector2.fromPoints(destinationProjection,
                    currentPos).getLength();

            // the point that will be returned as intermediate target pos
            IVector2 intermediatePos;

            // target further than step width
            if (Math.abs(distanceToTarget) > startBreakingDistance) {

                final IVector2 nextExtremum = nextExtremumButMax(startBreakingDistance);
                final double offsetNextExtremum = extendedPenArea.getOffset(
                        extendedPenArea.lengthToPointOnPenArea(nextExtremum));

                final double offsetCurrentPos = signedDistanceToPenArea(currentPos);
                // half of the offset has been reached (if there is no nextExtremum, 0 is also accepted because of >=)
                if ((Math.abs(offsetCurrentPos) >= (Math.abs((offsetNextExtremum) * proportionOfOffsetMustBeenReached)))
                        && ((offsetCurrentPos < 0) == (offsetNextExtremum < 0))) {
                    // ok, enough distance to penalty area, skip this obstacle
                    intermediatePos = stepTowardsDestination(currentPos, startBreakingDistance, true);
                }
                // use "prepositioned" point (maximum displaced towards bot) as target
                else {
                    final double distanceToRealMaximum = understeerWhenByPassingBot;
                    // call of stepTowardsDestination with false originally
                    final IVector2 prepositionedBase = stepTowardsDestination(nextExtremum, -distanceToRealMaximum,
                            true);
                    final IVector2 prepositionedOffset = extendedPenArea.pointWithOffset(prepositionedBase,
                            offsetNextExtremum);
                    // the bot is already further on penalty area than the prepositioned offset, but has not reached half of
                    // the offset value yet
                    final double lengthDifference = extendedPenArea.lengthToPointOnPenArea(currentPos)
                            - extendedPenArea.lengthToPointOnPenArea(prepositionedOffset);
                    // positive if prepo pos y val is more positive than currentPos y val
                    // destination dir
                    final int destinationDir = ((extendedPenArea.projectPointOnPenaltyAreaLine(destination).y()
                            - extendedPenArea.projectPointOnPenaltyAreaLine(currentPos).y()) > 0) ? -1 : 1;
                    // if lengthDifference's sign differs from destionationDir's sign, do not apply prepo
                    if ((destinationDir < 0) == (lengthDifference < 0)) {
                        intermediatePos = nextExtremum;
                    } else {
                        intermediatePos = prepositionedOffset;
                    }
                }
            }
            // target reached
            else {
				intermediatePos = destination;
            }

            DrawablePoint drawIntermediatePos = new DrawablePoint(intermediatePos);
            drawIntermediatePos.setSize(50);
			drawIntermediatePos.setColor(Color.RED);
            DrawablePoint drawDestinationExt = new DrawablePoint(destinationProjection);
            drawDestinationExt.setColor(Color.PINK);
            drawDestinationExt.setSize(50);
            getShapes().get(ESkillShapesLayer.PENALTY_AREA_DEFENSE).add(drawIntermediatePos);
            getShapes().get(ESkillShapesLayer.PENALTY_AREA_DEFENSE).add(drawDestinationExt);
            return intermediatePos;
        }


        private double signedDistanceToPenArea(final IVector2 point) {
            final IVector2 projectedPoint = extendedPenArea.projectPointOnPenaltyAreaLine(point);
            double distance = Vector2.fromPoints(point, projectedPoint).getLength();
            if (extendedPenArea.isPointInShape(point, 0)) {
                distance *= -1;
            }
            return distance;
        }


        private IVector2 nextExtremumButMax(final double maxLength) {
            IVector2 currentPos = getPos();
            IVector2 fakeStartPoint = stepTowardsDestination(currentPos, Geometry.getBotRadius(), true);
            // the point that is used as target point if there is no obstacle in between
            IVector2 furthestTargetPoint = stepTowardsDestination(currentPos, maxLength, true);
            int direction = (extendedPenArea.projectPointOnPenaltyAreaLine(destination).y()
                    - extendedPenArea.projectPointOnPenaltyAreaLine(currentPos).y()) > 0 ? 1 : -1;
            IVector2 nextMaximum = extendedPenArea
                    .getNextMaximum(extendedPenArea.lengthToPointOnPenArea(fakeStartPoint), direction);

            DrawablePoint drawFakeStartPoint = new DrawablePoint(fakeStartPoint);
            drawFakeStartPoint.setColor(Color.cyan);
            drawFakeStartPoint.setSize(50);
            getShapes().get(ESkillShapesLayer.PENALTY_AREA_DEFENSE).add(drawFakeStartPoint);

            if (nextMaximum == null) {
                return furthestTargetPoint;
            }
            // maximum is outside of relevant distance (e.g. behind destination)
            if (Math.abs(extendedPenArea.lengthToPointOnPenArea(nextMaximum)
                    - extendedPenArea.lengthToPointOnPenArea(currentPos)) > maxLength) {
                return furthestTargetPoint;
            }
            return nextMaximum;
        }


        private IVector2 stepTowardsDestination(final IVector2 currentPos, final double stepwidth,
                                                final boolean considerOffset) {
            IVector2 resultPos;
            double signedStepWidth = stepwidth;

            if ((extendedPenArea.projectPointOnPenaltyAreaLine(destination).y()
                    - extendedPenArea.projectPointOnPenaltyAreaLine(currentPos).y()) > 0) {
                // set the direction where to move
                signedStepWidth *= -1;
            }
            final double totalLength = extendedPenArea.getLength();
            final double lengthAppliedStepwidth = extendedPenArea.lengthToPointOnPenArea(currentPos) + signedStepWidth;
            if (lengthAppliedStepwidth > totalLength) {
                resultPos = extendedPenArea.stepAlongPenArea(totalLength);
            } else if (lengthAppliedStepwidth < 0) {
                resultPos = extendedPenArea.stepAlongPenArea(0);
            } else {

                resultPos = considerOffset ? extendedPenArea.stepAlongPenaltyAreaWithOffset(currentPos, signedStepWidth)
                        : extendedPenArea.stepAlongPenArea(currentPos, stepwidth);
            }
            return resultPos;
        }
    }

}
