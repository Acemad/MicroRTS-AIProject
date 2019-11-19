package aes.misc;

import ai.RandomBiasedAI;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.InterruptibleAI;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import rts.GameState;
import rts.PlayerAction;

import java.util.List;

public class PlainUCTm extends AIWithComputationBudget implements InterruptibleAI {

    // Members :
    GameState initialGameState;
    EvaluationFunction evaluationFunction;
    AI playoutAI;
    PlainUCTmNode tree;

    int simulationTime;
    int depthLimit;

    int player;

    public PlainUCTm() {
        this(100,-1, 100, 10,
                new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3());
    }
    /**
     * Constructs the controller with the specified time and iterations budget
     *
     * @param timeBudget       time in milliseconds
     * @param iterationsBudget number of allowed iterations
     */
    public PlainUCTm(int timeBudget, int iterationsBudget, int simulationTime, int depthLimit, AI playoutAI,
                     EvaluationFunction evaluationFunction) {
        super(timeBudget, iterationsBudget);
        this.simulationTime = simulationTime;
        this.depthLimit = depthLimit;
        this.playoutAI = playoutAI;
        this.evaluationFunction = evaluationFunction;
    }

    @Override
    public PlayerAction getAction(int player, GameState gameState) throws Exception {
        if (gameState.canExecuteAnyAction(player)) {
            startNewComputation(player, gameState);
            computeDuringOneGameFrame();
            return getBestActionSoFar();
        } else
            return new PlayerAction();
    }

    @Override
    public void startNewComputation(int player, GameState gameState) throws Exception {
        tree = new PlainUCTmNode(player, null, gameState);
        initialGameState = gameState;
        this.player = player;
    }

    @Override
    public void computeDuringOneGameFrame() throws Exception {
        long startTime = System.currentTimeMillis();
        int nbPlayouts= 0;
        long cutOffTime = startTime + TIME_BUDGET;
        if (TIME_BUDGET <= 0) cutOffTime = 0;

        while (true) {
            if (cutOffTime > 0 && System.currentTimeMillis() > cutOffTime) break;
            if (ITERATIONS_BUDGET > 0 && nbPlayouts > ITERATIONS_BUDGET) break;
            monteCarloRun(cutOffTime);
            nbPlayouts++;
        }
    }

    public void monteCarloRun(long cutOffTime) throws Exception {

        PlainUCTmNode selected = tree.selectNode(player, depthLimit, cutOffTime, evaluationFunction.upperBound(initialGameState));

        if (selected != null) {
            GameState simGameState = selected.getGameState().clone();
            simulate(simGameState, simGameState.getTime() + simulationTime);

            double evaluation = evaluationFunction.evaluate(player, 1 - player, simGameState);
            int time = simGameState.getTime() - initialGameState.getTime();
            evaluation *= Math.pow(0.99, time/10.0);

            while (selected != null) {
                selected.updateAccumulatedEvaluation(evaluation);
                selected.incrementVisitCount();
                selected = selected.getParent();
            }
        }
    }

    public void simulate(GameState gameState, long simulationTime) throws Exception {

        boolean gameOver = false;

        do {
            if (gameState.isComplete())
                gameOver = gameState.cycle();
            else {
                gameState.issue(playoutAI.getAction(0, gameState));
                gameState.issue(playoutAI.getAction(1, gameState));
            }
        } while (!gameOver && gameState.getTime() < simulationTime);
    }


    @Override
    public PlayerAction getBestActionSoFar() throws Exception {

        if (tree.getChildren() == null)
            return new PlayerAction();

        PlainUCTmNode bestChild = null;

        for (PlainUCTmNode child : tree.getChildren()) {
            if (bestChild == null || child.getVisitCount() > bestChild.getVisitCount() ||
                (child.getVisitCount() == bestChild.getVisitCount() &&
                 child.getAccumulatedEvaluation() > bestChild.getAccumulatedEvaluation())) {
                bestChild = child;
            }
        }

        if (bestChild == null)
            return new PlayerAction();

        return tree.getActions().get(tree.getChildren().indexOf(bestChild));
    }

    @Override
    public void reset() {
        initialGameState = null;
        tree = null;
    }

    @Override
    public AI clone() {
        return new PlainUCTm(TIME_BUDGET, ITERATIONS_BUDGET, simulationTime, depthLimit, playoutAI, evaluationFunction);
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        return null;
    }
}
