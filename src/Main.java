import aes.uct.PlainUCT;
import aes.uct.emptyactions.PlainUCTNoEmptyAction;
import ai.RandomBiasedAI;
import ai.abstraction.LightRush;
import ai.abstraction.WorkerRush;
import ai.core.AI;
import ai.core.ContinuingAI;
import ai.mcts.informedmcts.InformedNaiveMCTS;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.mcts.uct.DownsamplingUCT;
import ai.mcts.uct.UCT;
import ai.mcts.uct.UCTFirstPlayUrgency;
import ai.mcts.uct.UCTUnitActions;
import ai.montecarlo.lsi.LSI;
import ai.portfolio.PortfolioAI;
import ai.portfolio.portfoliogreedysearch.PGSAI;
import ai.scv.SCV;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

public class Main {

    public static void main(String[] args) throws Exception {

        UnitTypeTable unitTypeTable = new UnitTypeTable();
        PhysicalGameState physicalGameState8x8 = PhysicalGameState.
                load("maps\\8x8\\basesWorkers8x8A.xml", unitTypeTable);
        PhysicalGameState physicalGameState10x10 = PhysicalGameState.
                load("maps\\10x10\\basesWorkers10x10.xml", unitTypeTable);
        PhysicalGameState physicalGameState12x12 = PhysicalGameState.
                load("maps\\12x12\\basesWorkers12x12.xml", unitTypeTable);
        PhysicalGameState physicalGameState16x16 = PhysicalGameState.
                load("maps\\16x16\\basesWorkers16x16.xml", unitTypeTable);
        PhysicalGameState melee8x8 = PhysicalGameState.
                load("maps\\melee14x12Mixed18.xml", unitTypeTable);


        AI maxPlayer = new PlainUCTNoEmptyAction(unitTypeTable); // Player at the top, Blue colored.
        AI minPlayer = new PlainUCT(unitTypeTable); // Player at the Bottom, Red colored.

        System.out.println("Blue (Player 0 Max) : " + maxPlayer.getClass().getSimpleName());
        System.out.println("Red  (Player 1 Min) : " + minPlayer.getClass().getSimpleName());

        Experiments experiment = new Experiments(maxPlayer, minPlayer, unitTypeTable, physicalGameState8x8);
        experiment.maxCycles = 3000;

//        experiment.runSingleMatch(false, true, true, true, false);

        // Send to Gephy.
//        JSONSender senderUCT = new JSONSender("localhost", 8080, "workspace1");
//        JSONSender senderPlainUCT = new JSONSender("localhost", 8080, "workspace2");
        // Tree Visualisation

//        senderUCT.setDebug(true);
//        senderPlainUCT.setDebug(true);
//
//        experiment.graph.addSink(senderUCT);
//        experiment.constructTree(((UCT)maxPlayer).getTree());
//        experiment.resetGraph();
//        Thread.sleep(1000);
//        experiment.graph.addSink(senderPlainUCT);
//        experiment.constructTree(((PlainUCT)minPlayer).getTree());
//        experiment.resetGraph();

        //experiment.constructTree(((UCT)minPlayer).getTree());
//        experiment.maxTree.addAttribute("ui.stylesheet", "graph { fill-color: grey; }");

//        Viewer maxView = experiment.maxTree.display();
        //Viewer minView = experiment.minTree.display();

        experiment.runMultipleMatches(20, false, true, false, false);
        experiment.runMultipleMatches(20,true, true, true, true);
        System.exit(0);
    }
}
