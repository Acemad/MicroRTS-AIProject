package aes;

import rts.*;
import rts.units.Unit;
import util.Pair;

/**
 * FilteredPlayerActionGenerator : a wrapper class, wrapping PlayerActionGenerator and
 * provides action filtering facilities.
 *
 */
public class FilteredPlayerActionGenerator {

    private PlayerActionGenerator generator;
    private boolean oneWaitActionAllowed = false;
    private int allowedWaitActions = 0;

    public FilteredPlayerActionGenerator(GameState gameState, int player) throws Exception {
        generator = new PlayerActionGenerator(gameState, player);
    }

    public PlayerAction getNextUnfilteredAction(long cutOffTime) throws Exception {
        return generator.getNextAction(cutOffTime);
    }

    public void randomizeOrder() {
        generator.randomizeOrder();
    }

    public PlayerAction getNextFilteredAction(long cutOffTime, int allowedWaitActions) throws Exception {

        boolean actionAllowed = false;
        PlayerAction action;

        do {
            action = getNextUnfilteredAction(cutOffTime);
            if (allowedWaitActions == -1) return action;
            if (action != null) actionAllowed = isActionAllowed(action, allowedWaitActions);
        } while (!actionAllowed && action != null);

        return action;
    }

    private boolean isActionAllowed(PlayerAction playerAction, int allowedWaitActions) {

        // Filter out PlayerActions with a wait micro-action.

        for (Pair<Unit, UnitAction> unitActionPairs : playerAction.getActions()) {
            UnitAction action = unitActionPairs.m_b;
            if (action.getType() == UnitAction.TYPE_NONE) {
                if (this.allowedWaitActions < allowedWaitActions) {
                    this.allowedWaitActions++;
                    return true;
                } else
                    return false;
            }
        }
        return true;
    }

    private boolean allActionsAreWait(PlayerAction playerAction) {

        for (Pair<Unit, UnitAction> unitActionPairs : playerAction.getActions()) {
            UnitAction action = unitActionPairs.m_b;
            if (action.getType() != UnitAction.TYPE_NONE)
                return false;
        }
        return true;
    }


}
