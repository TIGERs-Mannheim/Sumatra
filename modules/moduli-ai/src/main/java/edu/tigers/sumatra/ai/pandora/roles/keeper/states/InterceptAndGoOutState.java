/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.skillsystem.skills.CatchSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToTrajSkill;

/**
 * For OneOnOne Keeper. If ball velocity is directed to Goal, first intercept the shooting line and then go out.
 */
public class InterceptAndGoOutState extends AKeeperState {

    private boolean isCatchSkillActive = true;

    /**
     * @param parent
     */
    public InterceptAndGoOutState(final KeeperRole parent) {
        super(parent);
    }

    @Override
    public void doEntryActions() {
        CatchSkill catchSkill = new CatchSkill();
        catchSkill.getMoveCon().setDestinationOutsideFieldAllowed(true);
        catchSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
        catchSkill.getMoveCon().setBotsObstacle(false);
        setNewSkill(catchSkill);
    }


    @Override
    public void doExitActions() {
        // nothing to do here
    }


    @Override
    public void doUpdate() {
        ILine ballLine = Line.fromDirection(getWFrame().getBall().getPos(), getWFrame().getBall().getVel().multiplyNew(10));

        if (ballLine.distanceTo(getPos()) < Geometry.getBotRadius() / 2) {
            if (isCatchSkillActive && !getWFrame().getBall().isChipped() && parent.getBot().getVel().getLength2() < 0.1) {
                MoveToTrajSkill moveToTrajSkill = new MoveToTrajSkill();
                moveToTrajSkill.getMoveCon().setDestinationOutsideFieldAllowed(false);
                moveToTrajSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
                moveToTrajSkill.getMoveCon().setBallObstacle(false);
                moveToTrajSkill.getMoveCon().setBotsObstacle(false);
                moveToTrajSkill.getMoveCon().updateDestination(getWFrame().getBall().getPos());
                moveToTrajSkill.getMoveCon().updateLookAtTarget(getWFrame().getBall().getPos());
                setNewSkill(moveToTrajSkill);
                isCatchSkillActive = false;
            } else {
                super.parent.getCurrentSkill().getMoveCon().updateDestination(getWFrame().getBall().getPos());
                super.parent.getCurrentSkill().getMoveCon().updateLookAtTarget(getWFrame().getBall().getPos());
            }
        } else {
            if (!isCatchSkillActive) {
                CatchSkill catchSkill = new CatchSkill();
                catchSkill.getMoveCon().setDestinationOutsideFieldAllowed(true);
                catchSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
                catchSkill.getMoveCon().setBotsObstacle(false);
                setNewSkill(catchSkill);
                isCatchSkillActive = true;
            }
        }
    }
}
