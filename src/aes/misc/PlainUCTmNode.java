package aes.misc;

import rts.GameState;
import rts.PlayerAction;
import rts.PlayerActionGenerator;

import java.util.ArrayList;
import java.util.List;

public class PlainUCTmNode {

    private static final float C = 0.05f;

    private GameState gameState;
    private PlainUCTmNode parent;
    private List<PlainUCTmNode> children;
    private List<PlayerAction> actions;
    private PlayerActionGenerator actionGenerator;

    private int type;
    private int depth;
    private double accumulatedEvaluation;
    private int visitCount;

    private boolean hasMoreActions = true;


    public PlainUCTmNode(int player, PlainUCTmNode parent, GameState gameState) throws Exception {
        this.parent = parent;
        this.gameState = gameState;

        if (this.parent == null) depth = 0;
        else depth = this.parent.depth + 1;

        while (!this.gameState.gameover() && this.gameState.winner() == -1 &&
               !this.gameState.canExecuteAnyAction(player) &&
               !this.gameState.canExecuteAnyAction(1 - player))
            this.gameState.cycle();

        if (this.gameState.gameover() || this.gameState.winner() != -1)
            type = -1; // Terminal Node
        else if (this.gameState.canExecuteAnyAction(player)) { // Player Node
            type = 0;
            children = new ArrayList<>();
            actions = new ArrayList<>();
            actionGenerator = new PlayerActionGenerator(this.gameState, player);
            actionGenerator.randomizeOrder();
        }
        else if (this.gameState.canExecuteAnyAction(1 - player)) { // Opponent Node
            type = 1;
            children = new ArrayList<>();
            actions = new ArrayList<>();
            actionGenerator = new PlayerActionGenerator(this.gameState, 1 - player);
            actionGenerator.randomizeOrder();
        }
        else
            type = -1;
    }

    public PlainUCTmNode selectNode(int player, int depthLimit, long cutOffTime, double evaluationBound) throws Exception {

        if (depth >= depthLimit) return this;

        if (actionGenerator != null) {
            if (hasMoreActions) {
                PlayerAction action = actionGenerator.getNextAction(cutOffTime);
                if (action != null) {
                    GameState newGameState = gameState.cloneIssue(action);
                    PlainUCTmNode newNode = new PlainUCTmNode(player, this, newGameState.clone()); //Try without clone.
                    children.add(newNode);
                    actions.add(action);
                    return newNode;
                } else
                    hasMoreActions = false;
            }
        } else
            return this;

        double bestValue = 0;
        PlainUCTmNode bestChild = null;
        for (PlainUCTmNode child : children) {
            double value = childValue(child, evaluationBound);
            if (bestChild == null || bestValue < value) {
                bestChild = child;
                bestValue = value;
            }
        }

        if (bestChild == null) return this;

        return bestChild.selectNode(player, depthLimit, cutOffTime, evaluationBound);
    }

    private double childValue(PlainUCTmNode child, double evaluationBound) {
        double exploitation = child.accumulatedEvaluation / child.visitCount;
        double exploration = Math.sqrt(Math.log(visitCount) / child.visitCount);
        if (type == 0)
            exploitation = (evaluationBound + exploitation) / (2 * evaluationBound);
        else
            exploitation = (evaluationBound - exploitation) / (2 * evaluationBound);
        return (C * exploitation + exploration);
    }

    public GameState getGameState() {
        return gameState;
    }

    public void incrementVisitCount() {
        visitCount++;
    }

    public void updateAccumulatedEvaluation(double evaluation) {
        accumulatedEvaluation += evaluation;
    }

    public PlainUCTmNode getParent() {
        return parent;
    }

    public List<PlainUCTmNode> getChildren() {
        return children;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public double getAccumulatedEvaluation() {
        return accumulatedEvaluation;
    }

    public List<PlayerAction> getActions() {
        return actions;
    }
}
