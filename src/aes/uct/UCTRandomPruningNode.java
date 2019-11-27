package aes.uct;

import rts.CustomPlayerActionGenerator;
import rts.GameState;
import rts.PlayerAction;

import java.util.ArrayList;
import java.util.List;

public class UCTRandomPruningNode {

    // Members : *********************************************************************************************

    public static long nodeCount = 0; // Total number of nodes created.
    private static float C = 0.05f;

    private GameState gameState;
    private UCTRandomPruningNode parent = null;
    private List<UCTRandomPruningNode> children = null;
    private List<PlayerAction> actions = null;
//    private PlayerActionGenerator actionGenerator = null;
    private CustomPlayerActionGenerator actionGenerator = null;

    private int type; // 0 : Max, 1 : Min, -1 : Terminal
    private int depth;
    private double accumulatedEvaluation = 0;
    private double visitCount = 0;

    private boolean hasMoreActions = true;
    private long nodeId = 0;




    // Constructors : ****************************************************************************************

    /**
     *
     * @param gameState
     * @param parent
     * @throws Exception
     */
    public UCTRandomPruningNode(GameState gameState, UCTRandomPruningNode parent, int player) throws Exception {
        // Associate the game state to the node, and set its parent.
        this.gameState = gameState;
        this.parent = parent;

        // Set the node's depth.
        if (this.parent == null) depth = 0;
        else depth = this.parent.depth + 1;

        // Forward the game state until at least one player can take action or the game is over.
        while (!this.gameState.gameover() && this.gameState.winner() == -1 &&
               !this.gameState.canExecuteAnyAction(player) && // Player.
               !this.gameState.canExecuteAnyAction(1 - player)) // Opponent.
            this.gameState.cycle();

        // Set the node's type.
        if (this.gameState.gameover() || this.gameState.winner() != -1) // Terminal Node.
            type = -1;
        else if (this.gameState.canExecuteAnyAction(player)) { // Player Node.
            type = 0;
            children = new ArrayList<>();
            actions = new ArrayList<>();
            actionGenerator = new CustomPlayerActionGenerator(this.gameState, player, 1f);
            actionGenerator.randomizeOrder();
        }
        else if (this.gameState.canExecuteAnyAction(1 - player)) { // Opponent Node.
            type = 1;
            children = new ArrayList<>();
            actions = new ArrayList<>();
            actionGenerator = new CustomPlayerActionGenerator(this.gameState, 1 - player, 1f);
            actionGenerator.randomizeOrder();
        } else
            // Should not happen.
            type = -1;

        nodeId = nodeCount;
        nodeCount++;
    }

    // Methods : ******************************************************************************************

    /**
     * Performs a depth-limited leaf selection/expansion
     *
     * @param cutOffTime maximum time allowed for selection.
     * @param depthLimit maximum depth allowed.
     * @return the selected leaf node.
     */
    public UCTRandomPruningNode selectLeaf(int player, long cutOffTime, int depthLimit, double evaluationBound, float pruneProbability) throws Exception {

        // Return this node in case the depth limit is reached.
        if (depth >= depthLimit)
            return this;

        //Get next action, and create a new node if not null.
        //In case all units are destroyed actionGenerator cannot exist
        if (actionGenerator != null) {
            //hasMoreActions is used to bypass having to execute getNextAction when there are no more actions.
            if (hasMoreActions) {
                PlayerAction action = actionGenerator.getNextActionRandomlyPruned(cutOffTime, pruneProbability); // Random Pruning
                if (action != null) { //Create a new node and add it to the tree. (Expansion)
                    GameState newGameState = gameState.cloneIssue(action);
                    UCTRandomPruningNode newNode = new UCTRandomPruningNode(newGameState.clone(), this, player);
                    children.add(newNode);
                    actions.add(action);
                    return newNode;
                } else
                    hasMoreActions = false;
            }
        } else
            return this;

        // Chose best child, using UCB1. i.e. Retrieve the element that maximizes UCB1
        double bestScore = 0;
        UCTRandomPruningNode bestChild = null;
        for (UCTRandomPruningNode child : children) {
            double value = childNodeUCBValue(child, evaluationBound);
            if (bestChild == null || bestScore < value) {
                bestScore = value;
                bestChild = child;
            }
        }

        if (bestChild == null) return this; //Node has no children. No more leafs.

        return bestChild.selectLeaf(player, cutOffTime, depthLimit, evaluationBound, pruneProbability); // Recursively chose best node in each ply.
    }


    /**
     * Returns the value of a node as calculated using the UCB formula.
     *
     * @param node the node to calculate evaluation for.
     * @return the evaluation score.
     */
    public double childNodeUCBValue(UCTRandomPruningNode node, double evaluationBound) {
        double exploitationTerm = node.accumulatedEvaluation / node.visitCount;
        double explorationTerm = Math.sqrt((Math.log(visitCount)) / node.visitCount);
        // exploitationTerm is bounded in [-1,1] (-1 favourable to min and +1 favourable to max).
        // The following will transform it to [0,1] bound depending on the node type (Max, Min).
        if (type == 0) // Max Node.
            exploitationTerm = (evaluationBound + exploitationTerm) / (2 * evaluationBound);
        else // Min node.
            exploitationTerm = (evaluationBound - exploitationTerm) / (2 * evaluationBound);

        return (exploitationTerm + C * explorationTerm);
    }

    public int getDepth() {
        return depth;
    }

    public int getType() {
        return type;
    }

    public GameState getGameState() {
        return gameState;
    }

    public double getAccumulatedEvaluation() {
        return accumulatedEvaluation;
    }

    public double getVisitCount() {
        return visitCount;
    }

    public void updateAccumulatedEvaluation(double evaluation) {
        this.accumulatedEvaluation += evaluation;
    }

    public void incrementVisitCount() {
        this.visitCount++;
    }

    public UCTRandomPruningNode getParent() {
        return parent;
    }

    public List<UCTRandomPruningNode> getChildren() {
        return children;
    }

    public List<PlayerAction> getActions() {
        return actions;
    }

    public long getNodeId() {
        return nodeId;
    }
}