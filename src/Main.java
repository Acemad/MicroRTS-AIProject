import aes.uct.PlainUCT;
import aes.uct.UCTRandomPruning;
import aes.uct.emptyactions.PlainUCTNoEmptyAction;
import aes.uct.emptyactions.UCTFixedInactionPruning;
import aes.uct.emptyactions.UCTProbaInactionPruning;
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
import org.graphstream.stream.gephi.JSONSender;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

public class Main {

    static UnitTypeTable unitTypeTable = new UnitTypeTable();
    static PhysicalGameState physicalGameState;
    static Experiments experiment;

    static String[] mapLocations = new String[]
            {"maps\\8x8\\basesWorkers8x8A.xml",
            "maps\\10x10\\basesWorkers10x10.xml",
            "maps\\12x12\\basesWorkers12x12.xml",
            "maps\\16x16\\basesWorkers16x16.xml",
            "maps\\melee14x12Mixed18.xml"};

    static final int MAP8X8 = 0, MAP10X10 = 1, MAP12X12 = 2, MAP16X16 = 3, MAP14X12MELEE18X = 4;
    static final int CYCLES8X8 = 3000, CYCLES10x10 = 3250, CYCLES12x12 = 3500, CYCLES16x16 = 4000;

    static AI maxPlayer = new UCTProbaInactionPruning(unitTypeTable, 0.6f), // Player at the top, Blue colored.
              minPlayer = new PlainUCT(unitTypeTable); // Player at the Bottom, Red colored.

    public static void initialize(int mapLocationIndex, int maxCycles) throws Exception {
        physicalGameState = PhysicalGameState.load(mapLocations[mapLocationIndex], unitTypeTable);

        experiment = new Experiments(maxPlayer, minPlayer, unitTypeTable, physicalGameState);
        experiment.setMaxCycles(maxCycles);

        System.out.println("Blue (Player 0 Max) : " + maxPlayer.getClass().getSimpleName());
        System.out.println("Red  (Player 1 Min) : " + minPlayer.getClass().getSimpleName());
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Start Time : " + java.time.LocalTime.now());
        testRPPParameters(MAP12X12, CYCLES12x12, 50, false,false);
//        experiment.runSingleMatch(false, true, true, true, false);

        // Send to Gephy.
//        JSONSender sender = new JSONSender("localhost", 8080, "workspace1");

        // Tree Visualisation

        /*sender.setDebug(true);
        experiment.graph.addSink(sender);
        experiment.constructTree(((UCTProbaInactionPruning)maxPlayer).getTree());
        experiment.resetGraph();*/


//        experiment.runMultipleMatches(20,false,true, false,false);
//        experiment.runMultipleMatches(20,true, true, true, true);
//        experiment.runMultipleMatchesSymmetric(10,true,false);

        System.out.println("End Time : " + java.time.LocalTime.now());
        System.exit(0);
    }

    public static void testRPPParameters(int mapLocationIndex, int maxCycles, int totalNumberOfMatches,
                                         boolean visualize, boolean printAIStats) throws Exception {
        initialize(mapLocationIndex, maxCycles);
        float[] parameters = new float[] {0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f};

        for (float parameter : parameters) {
            ((UCTProbaInactionPruning)maxPlayer).setInactionAllowProbability(parameter);
            System.out.println("** Starting Experiment with p = " + parameter);
            experiment.runMultipleMatchesSymmetric(totalNumberOfMatches, visualize, printAIStats);
        }
    }
}
