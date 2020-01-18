package aes.uct.emptyactions;

import ai.RandomBiasedAI;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.InterruptibleAI;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import rts.GameState;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;

import java.util.List;

public class UCTDynamicFixedInactionPruning extends AIWithComputationBudget implements InterruptibleAI {

    // Members : *********************************************************************************

    private GameState initialGameState = null;
    private EvaluationFunction evaluationFunction;
    private AI playoutAI;
    private UCTFixedInactionPruningNode tree = null;

    private int simulationTime;
    private int depthLimit;

    private int player;
    private double evaluationBound;
    private int allowedInactionsOutnumbered;
    private int allowedInactionsOutnumbers;
    private int allowedInactions;

    // Used exclusively for calculating statistics.
    public long statTotalRuns = 0;
    public long statTotalRunsThisMove = 0;
    public long statTotalCycles = 0;
    public long statTotalActionsIssued = 0;

    // Constructors : ****************************************************************************

    /**
     * Constructs the controller with a set of predetermined parameters.
     */
    public UCTDynamicFixedInactionPruning(UnitTypeTable unitTypeTable) {
        this(100, -1, 100, 10, 100,500,
                new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3());
    }

    /**
     * Constructs the controller with the default parameters, with the exception of the allowed inactions count.
     *
     * @param unitTypeTable a unitTypeTable.
     * @param allowedInactionsOutnumbers The number of inactions allowed.
     */
    public UCTDynamicFixedInactionPruning(UnitTypeTable unitTypeTable, int allowedInactionsOutnumbers,
                                          int allowedInactionsOutnumbered) {
        this(unitTypeTable);
        this.allowedInactionsOutnumbers = allowedInactionsOutnumbers;
        this.allowedInactionsOutnumbered = allowedInactionsOutnumbered;
    }

    /**
     * Constructs the controller with the specified time and iterations budget
     *
     * @param timeBudget       time in milliseconds
     * @param iterationsBudget number of allowed iterations
     */
    public UCTDynamicFixedInactionPruning(int timeBudget, int iterationsBudget, int simulationTime, int depthLimit,
                                          int allowedInactionsOutnumbers, int allowedInactionsOutnumbered,
                                          AI playoutAI, EvaluationFunction evaluationFunction) {
        super(timeBudget, iterationsBudget);
        this.simulationTime = simulationTime;
        this.depthLimit = depthLimit;
        this.allowedInactionsOutnumbers = allowedInactionsOutnumbers;
        this.allowedInactionsOutnumbered = allowedInactionsOutnumbered;
        this.playoutAI = playoutAI;
        this.evaluationFunction = evaluationFunction;
    }


    // Methods : **********************************************************************************

    /**
     * The main method that is called to return a PlayerAction.
     *
     * @param player ID of the player to move. Use it to check whether units are yours or enemy's
     * @param gameState The current game state for which we seek to compute an action.
     * @return a computed player action or an empty player action.
     * @throws Exception
     */
    @Override
    public PlayerAction getAction(int player, GameState gameState) throws Exception {
        if (gameState.canExecuteAnyAction(player)) {
            startNewComputation(player, gameState.clone());
            computeDuringOneGameFrame();
            return getBestActionSoFar();
        } else
            return new PlayerAction();
    }

    /**
     * Prepares computation for the current cycle.
     *
     * @param player the index of the player have its action calculated
     * @param gameState the game state where a player action is required
     * @throws Exception
     */
    @Override
    public void startNewComputation(int player, GameState gameState) throws Exception {
        UCTFixedInactionPruningNode.nodeCount = 0; //Stats.
        tree = new UCTFixedInactionPruningNode(gameState, null, player);
        this.player = player;
        initialGameState = gameState;
        evaluationBound = evaluationFunction.upperBound(gameState);
        statTotalRunsThisMove = 0; //Stats.

        // Adapting the number of allowed inactions with respect to the number of units owned by the player.
        int[] nbUnits = getActiveUnitCountForEachPlayer();
//        System.out.println("P0 " + nbUnits[player] + "P1 " + nbUnits[1-player]);
        if (nbUnits[player] <= nbUnits[1 - player]) // if outnumbered
            allowedInactions = allowedInactionsOutnumbered;
        else // if outnumbers
            allowedInactions = allowedInactionsOutnumbers;
    }

    /**
     * Returns the number of active units for each player. By active units we mean all unit types except structures
     * such as Bases and Barracks.
     * @return an int[] containing the number of units for each player index.
     */
    private int[] getActiveUnitCountForEachPlayer() {
        int[] counts = new int[] {0,0};
        for (Unit unit : initialGameState.getUnits())
            if (unit.getPlayer() >= 0 &&
                    !unit.getType().name.equals("Base") && !unit.getType().name.equals("Barracks"))
                counts[unit.getPlayer()]++;
        return counts;
    }

    /**
     * Executes computation during one frame.
     *
     * @throws Exception
     */
    @Override
    public void computeDuringOneGameFrame() throws Exception {
        long startTime = System.currentTimeMillis();
        int playouts = 0;
        long cutOffTime = startTime + TIME_BUDGET;
        if (TIME_BUDGET <= 0) cutOffTime = 0;

        while (true) {
            if (cutOffTime > 0 && System.currentTimeMillis() > cutOffTime) break; //Overbudget
            if (ITERATIONS_BUDGET > 0 && playouts > ITERATIONS_BUDGET) break;
            monteCarloRun(cutOffTime);
            playouts++;
        }
        statTotalCycles++; //Stats.
    }

    /**
     * Executes one Monte Carlo run
     *
     * @param cutOffTime The time at which search is halted.
     * @throws Exception
     */
    private void monteCarloRun(long cutOffTime) throws Exception {

        // (1) Start with selecting a node. (Expanded)
        UCTFixedInactionPruningNode selected = tree.selectLeaf(player, cutOffTime, depthLimit, evaluationBound, allowedInactions);

        if (selected != null) {
            // (2) Start a simulation from the new state.
            GameState simGameState = selected.getGameState().clone();
            simulate(simGameState, simGameState.getTime() + simulationTime);

            // (3) Evaluate the resulting state.
            double evaluation = evaluationFunction.evaluate(player, 1 - player, simGameState);
            // Apply a discount factor.
            int time = simGameState.getTime() - initialGameState.getTime();
            evaluation = evaluation * Math.pow(0.99, time/10.0); // Discount factor.

            // (4) Backpropagate scores and visit counts.
            while (selected != null) {
                selected.updateAccumulatedEvaluation(evaluation);
                selected.incrementVisitCount();
                selected = selected.getParent();
            }

            // Stats
            statTotalRuns++;
            statTotalRunsThisMove++;
        }
    }

    /**
     * Runs a simulation from the provided game state until reaching the time limit.
     * @param gameState
     * @param timeLimit
     * @throws Exception
     */
    private void simulate(GameState gameState, int timeLimit) throws Exception {

        boolean gameOver = false;

        do {
            if (gameState.isComplete())
                gameOver = gameState.cycle();
            else {
                gameState.issue(playoutAI.getAction(0, gameState));
                gameState.issue(playoutAI.getAction(1, gameState));
            }
        } while (!gameOver && gameState.getTime() < timeLimit);
    }


    /**
     * Determines the best action by iterating through the children of the root and
     * returning the most visited action.
     * @return
     * @throws Exception
     */
    @Override
    public PlayerAction getBestActionSoFar() throws Exception {
        statTotalActionsIssued++; //Stats.

        if (tree.getChildren() == null)
            return new PlayerAction();

        int bestChildIndex = -1;
        UCTFixedInactionPruningNode bestChild = null;
        for (int i = 0; i < tree.getChildren().size(); i++) {
            UCTFixedInactionPruningNode child = tree.getChildren().get(i);
            if (bestChild == null || child.getVisitCount() > bestChild.getVisitCount() ||
                (child.getVisitCount() == bestChild.getVisitCount() &&
                 child.getAccumulatedEvaluation() > bestChild.getAccumulatedEvaluation())) {
                bestChild = child;
                bestChildIndex = i;
            }
        }

        if (bestChildIndex == -1)
            return new PlayerAction();

        return tree.getActions().get(bestChildIndex);
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        return null;
    }

    /**
     * Clones the current AI instance
     *
     * @return returns the new cloned AI.
     */
    @Override
    public AI clone() {
        return new UCTDynamicFixedInactionPruning(TIME_BUDGET, ITERATIONS_BUDGET, simulationTime, depthLimit,
                allowedInactionsOutnumbers, allowedInactionsOutnumbered, playoutAI, evaluationFunction);
    }

    /**
     * Resets essential attributes of the AI.
     */
    @Override
    public void reset() {
        initialGameState = null;
        tree = null;
        resetAllStats();
    }

    /**
     * Print Stats to the standard output.
     */
    public void printStats() {
        System.out.println("Total Runs This Move : " + statTotalRunsThisMove);
        System.out.println("Total Runs : " + statTotalRuns);
        System.out.println("Total Cycles : " + statTotalCycles);
        System.out.println("Total Actions Issued : " + statTotalActionsIssued);
        System.out.println("Average Runs per Cycle : " + ((double)statTotalRuns/statTotalCycles));
        System.out.println("Average Runs per Action : " + ((double)statTotalRuns)/statTotalActionsIssued);
    }

    public void resetAllStats() {
        statTotalActionsIssued = 0;
        statTotalRuns = 0;
        statTotalCycles = 0;
        statTotalRunsThisMove = 0;
    }

    public UCTFixedInactionPruningNode getTree() {
        return tree;
    }

    public void setAllowedInactions(int allowedInactions) {
        this.allowedInactions = allowedInactions;
    }

    public int getAllowedInactions() {
        return allowedInactions;
    }

    public void setAllowedInactionsOutnumbers(int allowedInactionsOutnumbers) {
        this.allowedInactionsOutnumbers = allowedInactionsOutnumbers;
    }

    public int getAllowedInactionsOutnumbers() {
        return allowedInactionsOutnumbers;
    }

    public void setAllowedInactionsOutnumbered(int allowedInactionsOutnumbered) {
        this.allowedInactionsOutnumbered = allowedInactionsOutnumbered;
    }

    public int getAllowedInactionsOutnumbered() {
        return allowedInactionsOutnumbered;
    }
}