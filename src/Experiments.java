import aes.uct.PlainUCTNode;
import aes.uct.emptyactions.UCTProbaInactionPruningNode;
import ai.core.AI;
import ai.mcts.uct.UCTNode;
import gui.PhysicalGameStatePanel;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

import javax.swing.*;

/**
 * A class defining simple experiments for a single match and multiple matches.
 */
public class Experiments {

    UnitTypeTable unitTypeTable;
    AI maxPlayer, minPlayer;
    PhysicalGameState physicalGameState, physicalGameStateBackup;
    GameState gameState;

    int maxCycles = 3000;
    int period = 1;
    boolean gameOver = false;
    int maxWins = 0, minWins = 0, draws = 0; //First index is Max wins, second is Min's
//    boolean positionsSwitched = false;
    int maxID = 0, minID = 1;

    public Graph graph = new SingleGraph("Tree");


    public Experiments(AI maxPlayer, AI minPlayer, UnitTypeTable unitTypeTable, PhysicalGameState physicalGameState, int maxCycles) {
        this.maxPlayer = maxPlayer;
        this.minPlayer = minPlayer;
        this.physicalGameStateBackup = physicalGameState; // To be able to reset the GameState to its initial state.
        this.physicalGameState = physicalGameState.clone();
        this.unitTypeTable = unitTypeTable;
        this.maxCycles = maxCycles;
        gameState = new GameState(this.physicalGameState, unitTypeTable);
        resetStats();
    }

    public String runSingleMatch(boolean visualize) throws Exception {

        JFrame window = null;
        if (visualize)
            window = PhysicalGameStatePanel.newVisualizer(gameState,640,640, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);



        long nextUpdateTime = System.currentTimeMillis() + period;
        do {
            if (System.currentTimeMillis() >= nextUpdateTime) {
                PlayerAction maxAction, minAction;

                maxAction = maxPlayer.getAction(maxID, gameState);
                minAction = minPlayer.getAction(minID, gameState);

                gameState.issueSafe(maxAction);
                gameState.issueSafe(minAction);

                gameOver = gameState.cycle();
                if (visualize)
                    window.repaint();
                else {
                    System.out.print("[Running] ");
                    printCurrentCycle();
                }
                nextUpdateTime += period;
            } else {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } while (!gameOver && gameState.getTime() < maxCycles);

        maxPlayer.gameOver(gameState.winner());
        minPlayer.gameOver(gameState.winner());

        if (visualize) window.dispose();

        if (gameState.winner() < 0) {
            draws++;
            return "No One (Draw)";
        }
        else if (gameState.winner() == maxID) {
            maxWins++;
            return maxPlayer.toString() + " (P0Max)";
        }
        else {
            minWins++;
            return minPlayer.toString() + " (P1Min)";
        }

    }

    public String runSingleMatchNoText(boolean visualize) throws Exception {

        JFrame window = null;
        if (visualize)
            window = PhysicalGameStatePanel.newVisualizer(gameState,640,640, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);


        long nextUpdateTime = System.currentTimeMillis() + period;
        do {
            if (System.currentTimeMillis() >= nextUpdateTime) {
                PlayerAction maxAction, minAction;

                maxAction = maxPlayer.getAction(maxID, gameState);
                minAction = minPlayer.getAction(minID, gameState);

                gameState.issueSafe(maxAction);
                gameState.issueSafe(minAction);

                gameOver = gameState.cycle();
                if (visualize)
                    window.repaint();
                else {
                    System.out.print("[Running] ");
                    printCurrentCycle();
                }
                nextUpdateTime += period;
            } else {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } while (!gameOver && gameState.getTime() < maxCycles);

        maxPlayer.gameOver(gameState.winner());
        minPlayer.gameOver(gameState.winner());

        if (visualize) window.dispose();

        if (gameState.winner() < 0) {
            draws++;
            return "No One (Draw)";
        }
        else if (gameState.winner() == maxID) {
            maxWins++;
            return maxPlayer.getClass().getSimpleName() + " (P0Max)";
        }
        else {
            minWins++;
            return minPlayer.getClass().getSimpleName() + " (P1Min)";
        }

    }

    public void runSingleMatch(boolean switchPos, boolean visualize, boolean clearStats, boolean printStats, boolean reset) throws Exception{

        if (switchPos) switchPositions();

        String outcome = runSingleMatch(visualize);
        System.out.println("Match Winner : " + outcome + " in " + gameState.getTime() + " cycle.");

        if (switchPos) switchPositions();
        if (printStats) printAIStats();
        if (clearStats) resetStats();
        if (reset) resetAll();
    }

    public void runSingleMatchNoText(boolean switchPos, boolean visualize, boolean clearStats, boolean printStats, boolean reset) throws Exception{

        if (switchPos) switchPositions();

        String outcome = runSingleMatch(visualize);

        if (switchPos) switchPositions();
        if (printStats) printAIStats();
        if (clearStats) resetStats();
        if (reset) resetAll();
    }

    public void printAIStats() {
        System.out.println("******** Stats ***************");
        System.out.println("*********** Player 0 Max > " + maxPlayer.getClass().getSimpleName());
        maxPlayer.printStats();
        System.out.println("*********** Player 1 Min > " + minPlayer.getClass().getSimpleName());
        minPlayer.printStats();
        System.out.println("******************************");
    }

    public void resetAll() {
        physicalGameState = physicalGameStateBackup.clone();
        gameState = new GameState(physicalGameState, unitTypeTable);
        maxPlayer.reset();
        minPlayer.reset();
        gameOver = false;
    }

    public void resetStats() {
        maxWins = 0; minWins = 0; draws = 0;
    }

    /**
     * Runs multiple matches and prints the resulting win rates and draws.
     * @param numMatches
     * @param visualize
     * @throws Exception
     */
    public void runMultipleMatches(int numMatches, boolean switchPos, boolean visualize, boolean clearStats, boolean printAIStats, boolean printFinalStats) throws Exception{

        if (switchPos) switchPositions();

        for (int i = 0; i < numMatches; i++) {
            String outcome = runSingleMatch(visualize);
            System.out.print("Match " + i + ": Winner " + outcome + " in " + gameState.getTime() + " cycle. ");
            printCurrentStats();
            if (printAIStats) printAIStats();
            resetAll();
        }

        if (switchPos) switchPositions();
        if (printFinalStats) printFinalStats();
        if (clearStats) resetStats();
    }

    public void runMultipleMatchesSymmetric(int totalNumber, boolean visualize, boolean printAIStats) throws Exception {
        if (totalNumber % 2 != 0)
            throw new Exception("The total number of matches must be even");

        runMultipleMatches(totalNumber / 2,false, visualize, false, printAIStats, false);
        System.out.println("** Switching Positions **");
        runMultipleMatches(totalNumber / 2,true, visualize, true, printAIStats, true);
    }

    public void printCurrentCycle() {
        System.out.print("Cycle : " + gameState.getTime() + "\r");
    }

    public void printFinalStats() {
        System.out.println("Final Stats : **************************************************************");
        System.out.println(maxPlayer.toString() + " (P0 Max) Wins: " + maxWins);
        System.out.println(minPlayer.toString() + " (P1 Min) Wins: " + minWins);
        System.out.println("Draws: " + draws);
        printCurrentStats();
    }

    public void printCurrentStats() {
        float score0 = ((float) maxWins + (float) draws / 2) / (maxWins + minWins + draws);
        float score1 = ((float) minWins + (float) draws / 2) / (maxWins + minWins + draws);

        System.out.print("[P0Wins: " + maxWins + ", P1Wins: " + minWins + ", Draws: " + draws + "]" +
                " (P0: " );
        System.out.format("%.3f", score0);
        System.out.print(")" + " (P1: ");
        System.out.format("%.3f", score1);
        System.out.print(")\n");
    }

    public void resetGraph() {
        graph = new SingleGraph("Tree");
    }

    public void constructTree(PlainUCTNode node) {
        if (node == null)
            return;

        String nodeId = String.valueOf(node.getNodeId());
        graph.addNode(nodeId).addAttribute("Visits", node.getVisitCount());
        graph.getNode(nodeId).addAttribute("Eval", String.valueOf(node.getAccumulatedEvaluation()));

        if (node.getParent() != null) {
            String parentId = String.valueOf(node.getParent().getNodeId());
            graph.addEdge(parentId + "->" + nodeId, parentId, nodeId, true);
        }

        if (!node.getChildren().isEmpty()) {
            for (PlainUCTNode child : node.getChildren()) {
                constructTree(child);
            }
        }
    }

    public void constructTree(UCTNode node) {
        if (node == null) return;
        
        String nodeId = String.valueOf(node.getNodeId());
        graph.addNode(nodeId).addAttribute("Visits", node.getVisit_count());
        graph.getNode(nodeId).addAttribute("Eval", String.valueOf(node.getAccum_evaluation()));

        if (node.getParent() != null)  {
            String parentId = String.valueOf(node.getParent().getNodeId());
            graph.addEdge(parentId + "->" + nodeId, parentId, nodeId, true);
        }

        if (!node.getChildren().isEmpty()) {
            for (UCTNode child : node.getChildren()) {
                constructTree(child);
            }
        }
    }

    public void constructTree(UCTProbaInactionPruningNode node) {
        if (node == null)
            return;

        String nodeId = String.valueOf(node.getNodeId());
        graph.addNode(nodeId).addAttribute("Visits", node.getVisitCount());
        graph.getNode(nodeId).addAttribute("Eval", String.valueOf(node.getAccumulatedEvaluation()));

        if (node.getParent() != null) {
            String parentId = String.valueOf(node.getParent().getNodeId());
            graph.addEdge(parentId + "->" + nodeId, parentId, nodeId, true);
        }

        if (!node.getChildren().isEmpty()) {
            for (UCTProbaInactionPruningNode child : node.getChildren()) {
                constructTree(child);
            }
        }
    }

    public void switchPositions() {
        int temp = maxID;
        maxID = minID;
        minID = temp;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setMaxCycles(int maxCycles) {
        this.maxCycles = maxCycles;
    }

    public int getMaxCycles() {
        return maxCycles;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getPeriod() {
        return period;
    }

    public void setMaxPlayer(AI maxPlayer) {
        this.maxPlayer = maxPlayer;
    }

    public AI getMaxPlayer() {
        return maxPlayer;
    }

    public void setMinPlayer(AI minPlayer) {
        this.minPlayer = minPlayer;
    }

    public AI getMinPlayer() {
        return minPlayer;
    }
}
