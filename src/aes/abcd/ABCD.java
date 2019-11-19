package aes.abcd;

import ai.core.AI;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import rts.GameState;
import rts.PlayerAction;

import java.util.List;

public class ABCD extends AI {
    int leaves = 0;
    int node = 0;
    int maxDepth = 0;
    AI playoutAI = null;
    int maxPlayoutCycles = 100;
    EvaluationFunction evaluationFunction = null;
    int noneDuration = 8;


    public ABCD(int maxDepth, AI playoutAI, int maxPlayoutCycles, EvaluationFunction evaluationFunction) {
        this.maxDepth = maxDepth;
        this.playoutAI = playoutAI;
        this.maxPlayoutCycles = maxPlayoutCycles;
        this.evaluationFunction = evaluationFunction;
    }


    @Override
    public void reset() {
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        return null;
    }

    @Override
    public AI clone() {
        return new ABCD(maxDepth, playoutAI, maxPlayoutCycles, evaluationFunction);
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        return null;
    }
}
