/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperOneOnOneRole;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.MoveToTrajSkill;

/**
 * Drive direct towards the ball
 */
public class RamboState extends AKeeperState {

    private MoveToTrajSkill posSkill;

    /**
     * Create skill
     *
     * @param parent the parent Role
     */
    public RamboState(final KeeperRole parent) {
        super(parent);

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
        ILine ballGoalCenter = Line.fromPoints(Geometry.getGoalOur().getCenter(), getWFrame().getBall().getPos());
        boolean isKeeperBetweenBallAndGoal = ballGoalCenter.isPointOnLineSegment(getPos(), Geometry.getBotRadius());
        if (!isKeeperBetweenBallAndGoal) {
            posSkill.getMoveCon().updateDestination(getWFrame().getBall().getPos());
        } else {
            IVector2 destination = getWFrame().getBall().getTrajectory().getPosByTime(KeeperOneOnOneRole.getLookahead());
            posSkill.getMoveCon().updateDestination(destination);
            posSkill.getMoveCon().updateLookAtTarget(getWFrame().getBall().getPos());
        }
    }

}
