/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.statemachine.IState;

/**
 * Skill moves fast on a thread line defined by a targetPoint (e.g. foe) and a coverPoint (e.g. own goal)
 * and moves towards the target Point.
 *
 * @author Stefan Schneyer
 */
public class MoveOnThreatLineSkill extends AMoveSkill
{
    private ILine threatLine;

    public MoveOnThreatLineSkill() {
        super(ESkill.MOVE_ON_THREAT_LINE);
    }

    public MoveOnThreatLineSkill(IVector2 coverPoint, IVector2 targetPoint) {
        super(ESkill.MOVE_ON_THREAT_LINE);

        threatLine = Line.fromPoints(coverPoint, targetPoint);

        IState moveOnThreadLine = new MoveAlongThreadLine();
        setInitialState(moveOnThreadLine);
    }

    /**
     * Setter for ThreadLine
     * @param coverPoint
     * @param targetPoint
     */
    public void setThreatLine(IVector2 coverPoint, IVector2 targetPoint)
    {
        threatLine = Line.fromPoints(coverPoint, targetPoint);
    }

    /**
     * Setter for ThreadLine
     * @param line
     */
    public void setThreatLine(ILine line)
    {
        threatLine = line;
    }

    private class MoveAlongThreadLine extends MoveToState
    {

        protected MoveAlongThreadLine() {
            super(MoveOnThreatLineSkill.this);
        }

        @Override
        public void doEntryActions()
        {
            super.doEntryActions();

            getMoveCon().setPenaltyAreaAllowedOur(false);
            getMoveCon().setBallObstacle(false);
            getMoveCon().setPrimaryDirection(threatLine.directionVector());
        }

        @Override
        public void doUpdate()
        {
            super.doUpdate();

            getMoveCon().setPenaltyAreaAllowedOur(false);
            getMoveCon().setBallObstacle(false);
            getMoveCon().setPrimaryDirection(threatLine.directionVector());
            getMoveCon().updateDestination(threatLine.getEnd());
            getMoveCon().updateLookAtTarget(threatLine.getEnd().addNew(threatLine.directionVector()));
        }
    }

}

