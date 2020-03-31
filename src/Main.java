import Standard.ImprovedStrategyTactics;
import Standard.StrategyTactics;
import aes.nmcts.*;
import aes.uct.PlainUCT;
import aes.uct.UCTRandomPruningFixed;
import aes.uct.UCTRandomPruningProba;
import aes.uct.emptyactions.*;
import ai.BranchingFactorCalculatorBigInteger;
import ai.BranchingFactorCalculatorDouble;
import ai.BranchingFactorCalculatorLong;
import ai.GNS.Droplet;
import ai.GNS.GuidedGreedyNaiveMCTS;
import ai.JZ.MixedBot;
import ai.PVAI.PVAIML_ED;
import ai.RandomBiasedAI;
import ai.abstraction.LightRush;
import ai.abstraction.WorkerRush;
import ai.abstraction.partialobservability.POLightRush;
import ai.abstraction.partialobservability.POWorkerRush;
import ai.abstraction.pathfinding.PathFinding;
import ai.ahtn.AHTNAI;
import ai.asymmetric.SSS.SSSmRTS;
import ai.ccg.MicroCCG_v2;
import ai.competition.IzanagiBot.Izanagi;
import ai.competition.capivara.Capivara;
import ai.competition.capivara.CmabAssymetricMCTS;
import ai.competition.capivara.ImprovedCapivara;
import ai.competition.tiamat.ImprovedTiamat;
import ai.competition.tiamat.Tiamat;
import ai.core.AI;
import ai.core.ContinuingAI;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.mcts.informedmcts.InformedNaiveMCTS;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.mcts.uct.UCT;
import ai.montecarlo.lsi.LSI;
import ai.portfolio.portfoliogreedysearch.PGSAI;
import ai.puppet.PuppetSearchMCTS;
import ai.scv.SCV;
import ai.utalca.UTalcaBot;
import idvrv.IDVRV_Bot;
import rts.*;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import tournaments.RoundRobinTournament;
import util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Main {

    static UnitTypeTable unitTypeTable = new UnitTypeTable();
    static PhysicalGameState physicalGameState;
    static Experiments experiment;

    static String[] mapLocations = new String[]
            {"maps\\8x8\\basesWorkers8x8A.xml",
            "maps\\10x10\\basesWorkers10x10.xml",
            "maps\\12x12\\basesWorkers12x12.xml",
            "maps\\16x16\\basesWorkers16x16.xml",
            "maps\\melee14x12Mixed18.xml",
            "maps\\basesWorkers32x32A.xml",
            "maps\\24x24\\basesWorkers24x24.xml",
            "maps\\GardenOfWar64x64.xml"};

    static final int MAP8X8 = 0, MAP10X10 = 1, MAP12X12 = 2, MAP16X16 = 3, MAP14X12MELEE18X = 4, MAP32x32 = 5, MAP24x24 = 6, MAP64x64 = 7;
    static final int CYCLES8X8 = 3000, CYCLES10x10 = 3250, CYCLES12x12 = 3500, CYCLES16x16 = 4000, CYCLES24x24 = 8000, CYCLES32x32 = 12000, CYCLES64x64 = 24000;

    static AI maxPlayer = new UCTProbaInactionPruning(unitTypeTable, 0.6f), // Player at the top, Blue colored.
              minPlayer = new PlainUCT(unitTypeTable); // Player at the Bottom, Red colored.

    public static void initialize(AI maxPlayer, AI minPlayer, int mapLocationIndex, int maxCycles) throws Exception {
        physicalGameState = PhysicalGameState.load(mapLocations[mapLocationIndex], unitTypeTable);
        experiment = new Experiments(maxPlayer, minPlayer, unitTypeTable, physicalGameState, maxCycles);
//        experiment.setMaxCycles(maxCycles);

        System.out.println("Blue (Player 0 Max) : " + maxPlayer.getClass().getSimpleName());
        System.out.println("Red  (Player 1 Min) : " + minPlayer.getClass().getSimpleName());
    }

    public static void initializeNoText(AI maxPlayer, AI minPlayer, int mapLocationIndex, int maxCycles) throws Exception {
        physicalGameState = PhysicalGameState.load(mapLocations[mapLocationIndex], unitTypeTable);
        experiment = new Experiments(maxPlayer, minPlayer, unitTypeTable, physicalGameState, maxCycles);
//        experiment.setMaxCycles(maxCycles);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Start Time : " + java.time.LocalDateTime.now());
//        testAllRPPParametersVsUCT(MAP8X8, CYCLES8X8, 10, false,false);
//        testAllRFPParametersVsUCT(MAP16X16, CYCLES16x16, 50, false, false);
//        testRPPParameterVsUCT(MAP12X12, CYCLES12x12, 50, 1.0f, 1, false, false);
//        testRandomPruningParameterVsUCT(MAP8X8, CYCLES8X8, 2, 0.9f, 1, false, false);

//        for (int i = 0; i < 2; i++) {
//            System.out.println("Starting Experiment " + i + " ##################################################");
//            testAllDynamicRFPParametersVsUCT(MAP16X16, CYCLES16x16, 50, false, false);
//            testAllRandomFixedPruningParametersVsUCT(MAP16X16, CYCLES16x16, 50, false, false);
//            testAllRPPParametersVsUCT(MAP16X16, CYCLES16x16, 50, false, false);
//        }
        List<AI> competitionsAIs = new ArrayList<>();
        competitionsAIs.add(new MixedBot(unitTypeTable));
        competitionsAIs.add(new Izanagi(unitTypeTable));
        competitionsAIs.add(new POLightRush(unitTypeTable));
        competitionsAIs.add(new POWorkerRush(unitTypeTable));
        competitionsAIs.add(new NaiveMCTS(unitTypeTable));
        competitionsAIs.add(new UCT(unitTypeTable));
        competitionsAIs.add(new RandomBiasedAI(unitTypeTable));
        // Newly Added
        competitionsAIs.add(new Droplet(unitTypeTable));
        competitionsAIs.add(new Capivara(unitTypeTable));

        List<AI> AIs8x8 = new ArrayList<>();
        AIs8x8.add(new UCTDynamicFixedInactionPruning(unitTypeTable,1,0));
        AIs8x8.add(new NMCTSRandomInactivityFilteringProba(unitTypeTable, 0.9f));
        AIs8x8.addAll(competitionsAIs);

        List<AI> AIs12x12 = new ArrayList<>();
        AIs12x12.add(new UCTDynamicProbaInactionPruning(unitTypeTable, 0.2f, 0.4f));
        AIs12x12.add(new NMCTSRandomInactivityFilteringProba(unitTypeTable, 0f));
        AIs12x12.addAll(competitionsAIs);


        List<AI> AIs16x16 = new ArrayList<>();
        AIs16x16.add(new UCTFixedInactionPruning(unitTypeTable, 1));
        AIs16x16.add(new NMCTSRandomInactivityFilteringFixed(unitTypeTable, 0));
        AIs16x16.addAll(competitionsAIs);


//        runTournament(AIs16x16, 10, MAP16X16, CYCLES16x16, 100, "FinalTournament16x16++.csv");
//        testDynamicRFPParameterVsUCT(MAP8X8, CYCLES8X8, 50, 0, 10, true, false);
//        AI mP = new UCTDynamicFixedInactionPruning(unitTypeTable,0,1);
//        runOneMatch(mP, new PlainUCT(unitTypeTable), MAP8X8, 1, true, false);


//        testAllNMCTSRIFFParameters(MAP8X8, CYCLES8X8, 100, false, false);
        /*int[] params = new int[] {50, 100, 500, 1000, 5000, 10000};
        for (int param : params) {
            System.out.println("***** Experiment on : Map 12x12, n = " + param + " *************");

            runMatches(new NMCTSRandomInactivityFilteringFixed(unitTypeTable, param),
                       new NaiveMCTS(unitTypeTable),
                       MAP12X12, CYCLES12x12, 100,
                      false, false);
        }*/

        /*runMatches(new POWorkerRush(unitTypeTable),
                   new POLightRush(unitTypeTable),
                   MAP16X16, CYCLES16x16, 20,
                true, false);*/

        //BF-
        System.out.println("NaïveMCTS - MAP 16x16");
        try {
//            NMCTSRIPPBFExperiment(MAP8X8, 0.9f, 400, 100, false, 25);
//            NMCTSRIPPBFExperiment(MAP12X12, 0f, 600, 100,  false, 25);
//            NMCTSRIPPBFExperiment(MAP16X16, 0f, 1000, 100,  false, 25);

//            NMCTSRIPPBFExperiment(MAP24x24, 0f, 1800, 100,  true, 25);
//            NMCTSRIPPBFExperiment(MAP32x32, 0f, 200, 100,  false, 25);

            //BF-NMCTS
//            NMCTSRIPPBFExperiment(MAP8X8, 1f, 400, 100,  false, 25);
//          NMCTSRIPPBFExperiment(MAP12X12, 1f, 600, 100,  false, 25);
          NMCTSRIPPBFExperiment(MAP16X16, 0f, 1000, 100,  false, 25);

//          NMCTSRIPPBFExperiment(MAP24x24, 1f, 200, 100,  false, 25);
//          NMCTSRIPPBFExperiment(MAP32x32, 1f, 200, 100,  false, 25);
        } catch (Exception e) {
            e.printStackTrace();
        }






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

        System.out.println("End Time : " + java.time.LocalDateTime.now());
        System.exit(0);
    }

    public static void testAllRFPParametersVsUCT(int mapLocationIndex, int maxCycles, int totalNumberOfMatches, boolean visualize,
                                                 boolean printAIStats) throws Exception {

        AI maxPlayer = new UCTFixedInactionPruning(unitTypeTable, 0), // Player at the top, Blue colored.
           minPlayer = new PlainUCT(unitTypeTable); // Player at the Bottom, Red colored.

        int[] parameters = new int[] {0, 1, 5, 10, 50, 100, 500, 1000, 5000, 10000};
        initialize(maxPlayer, minPlayer, mapLocationIndex, maxCycles);
        for (int parameter : parameters) {
            ((UCTFixedInactionPruning)experiment.getMaxPlayer()).setAllowedInactions(parameter);
//            ((UCTProbaInactionPruning)maxPlayer).setInactionAllowProbability(parameter);
            System.out.println("** Starting Experiment with allowed inactions = " + parameter);
            experiment.runMultipleMatchesSymmetric(totalNumberOfMatches, visualize, printAIStats);
        }
    }

    public static void testRFPParameterVsUCT(int mapLocationIndex, int maxCycles, int totalNumberOfMatches, int parameter,
                                             int numberOfTests, boolean visualize, boolean printAIStats) throws Exception {

        AI maxPlayer = new UCTFixedInactionPruning(unitTypeTable, parameter),
           minPlayer = new PlainUCT(unitTypeTable);

        initialize(maxPlayer, minPlayer, mapLocationIndex, maxCycles);

        for (int testId = 0; testId < numberOfTests; testId++) {
            System.out.println("** Starting experiment " + testId +
                    " with p = " + ((UCTFixedInactionPruning)experiment.getMaxPlayer()).getAllowedInactions());
            experiment.runMultipleMatchesSymmetric(totalNumberOfMatches, visualize, printAIStats);
        }
    }

    public static void testAllRPPParametersVsUCT(int mapLocationIndex, int maxCycles, int totalNumberOfMatches, boolean visualize,
                                                 boolean printAIStats) throws Exception {

        AI maxPlayer = new UCTProbaInactionPruning(unitTypeTable, 0f), // Player at the top, Blue colored.
           minPlayer = new PlainUCT(unitTypeTable); // Player at the Bottom, Red colored.

        float[] parameters = new float[] {0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f};
        initialize(maxPlayer, minPlayer, mapLocationIndex, maxCycles);
        for (float parameter : parameters) {
            ((UCTProbaInactionPruning)experiment.getMaxPlayer()).setInactionAllowProbability(parameter);
//            ((UCTProbaInactionPruning)maxPlayer).setInactionAllowProbability(parameter);
            System.out.println("** Starting Experiment with p = " + parameter);
            experiment.runMultipleMatchesSymmetric(totalNumberOfMatches, visualize, printAIStats);
        }
    }

    public static void testRPPParameterVsUCT(int mapLocationIndex, int maxCycles, int totalNumberOfMatches, float parameter,
                                             int numberOfTests, boolean visualize, boolean printAIStats) throws Exception {

        AI maxPlayer = new UCTProbaInactionPruning(unitTypeTable, parameter),
           minPlayer = new PlainUCT(unitTypeTable);

        initialize(maxPlayer, minPlayer, mapLocationIndex, maxCycles);

        for (int testId = 0; testId < numberOfTests; testId++) {
            System.out.println("** Starting experiment " + testId +
                    " with p = " + ((UCTProbaInactionPruning)experiment.getMaxPlayer()).getInactionAllowProbability());
            experiment.runMultipleMatchesSymmetric(totalNumberOfMatches, visualize, printAIStats);
        }
    }

    public static void testAllRandomPruningParametersVsUCT(int mapLocationIndex, int maxCycles, int totalNumberOfMatches, boolean visualize,
                                                           boolean printAIStats) throws Exception {

        AI maxPlayer = new UCTRandomPruningProba(unitTypeTable, 0f),
           minPlayer = new PlainUCT(unitTypeTable);

        float[] parameters = new float[] {0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f};
        initialize(maxPlayer, minPlayer, mapLocationIndex, maxCycles);

        for (float parameter : parameters) {
            ((UCTRandomPruningProba)experiment.getMaxPlayer()).setAllowProbability(parameter);
            System.out.println("** Starting Experiment with p = " + ((UCTRandomPruningProba)experiment.getMaxPlayer()).getAllowProbability());
            experiment.runMultipleMatchesSymmetric(totalNumberOfMatches, visualize, printAIStats);
        }
    }

    public static void testRandomPruningParameterVsUCT(int mapLocationIndex, int maxCycles, int totalNumberOfMatches, float parameter,
                                                       int numberOfTests, boolean visualize, boolean printAIStats) throws Exception {

        AI maxPlayer = new UCTRandomPruningProba(unitTypeTable, parameter),
                minPlayer = new PlainUCT(unitTypeTable);

        initialize(maxPlayer, minPlayer, mapLocationIndex, maxCycles);

        for (int testId = 0; testId < numberOfTests; testId++) {
            System.out.println("** Starting experiment " + testId +
                    " with p = " + ((UCTRandomPruningProba)experiment.getMaxPlayer()).getAllowProbability());
            experiment.runMultipleMatchesSymmetric(totalNumberOfMatches, visualize, printAIStats);
        }
    }

    public static void testAllRandomFixedPruningParametersVsUCT(int mapLocationIndex, int maxCycles, int totalNumberOfMatches, boolean visualize,
                                                                boolean printAIStats) throws Exception {

        AI maxPlayer = new UCTRandomPruningFixed(unitTypeTable, 0),
           minPlayer = new PlainUCT(unitTypeTable);

        int[] parameters = new int[] {0, 1, 5, 10, 50, 100, 500, 1000, 5000, 10000};
        initialize(maxPlayer, minPlayer, mapLocationIndex, maxCycles);

        for (int parameter : parameters) {
            ((UCTRandomPruningFixed)experiment.getMaxPlayer()).setAllowedActionsCount(parameter);
            System.out.println("** Starting Experiment with p = " + ((UCTRandomPruningFixed)experiment.getMaxPlayer()).getAllowedActionsCount());
            experiment.runMultipleMatchesSymmetric(totalNumberOfMatches, visualize, printAIStats);
        }
    }

    public static void testAllNMCTSRIFFParameters(int mapLocationIndex, int maxCycles, int numberOfMatches, boolean visualize,
                                                  boolean printAIStats) throws Exception {
        AI maxPlayer = new NMCTSRandomInactivityFilteringFixed(unitTypeTable),
           minPlayer = new NaiveMCTS(unitTypeTable);

        int[] parameters = new int[] {0, 1, 5, 10, 50, 100, 500, 1000, 5000, 10000};
        initialize(maxPlayer, minPlayer, mapLocationIndex, maxCycles);

        for (int parameter : parameters) {
            ((NMCTSRandomInactivityFilteringFixed)experiment.getMaxPlayer()).setMaxAllowedInactions(parameter);
            System.out.println("** Starting Experiment with n = " +
                    ((NMCTSRandomInactivityFilteringFixed)experiment.getMaxPlayer()).getMaxAllowedInactions());
            experiment.runMultipleMatchesSymmetric(numberOfMatches, visualize, printAIStats);
        }
    }

    public static void testAllNMCTSRIFPParameters(int mapLocationIndex, int maxCycles, int numberOfMatches, boolean visualize,
                                                  boolean printAIStats) throws Exception {
        AI maxPlayer = new NMCTSRandomInactivityFilteringProba(unitTypeTable),
           minPlayer = new NaiveMCTS(unitTypeTable);


        float[] parameters = new float[] {0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f};
        initialize(maxPlayer, minPlayer, mapLocationIndex, maxCycles);

        for (float parameter : parameters) {
            ((NMCTSRandomInactivityFilteringProba)experiment.getMaxPlayer()).setAllowProbability(parameter);
            System.out.println("** Starting Experiment with n = " +
                    ((NMCTSRandomInactivityFilteringProba)experiment.getMaxPlayer()).getAllowProbability());
            experiment.runMultipleMatchesSymmetric(numberOfMatches, visualize, printAIStats);
        }
    }

    // D-RPP Parameter Test
    public static void testAllDynamicRPPParametersVsUCT(int mapLocationIndex, int maxCycles, int totalNumberOfMatches,
                                                        boolean visualize, boolean printAIStats) throws Exception {

        AI maxPlayer = new UCTDynamicProbaInactionPruning(unitTypeTable),
           minPlayer = new PlainUCT(unitTypeTable);

        // 8x8 : p1 = 0 --- -> p2 test {0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f}
        // 12x12 : p1 = 0.2 -> p2 test {0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f}
        // 16x16 : p1 = 0.3 -> p2 test {0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f}

        float[] parameters;
        initialize(maxPlayer, minPlayer, mapLocationIndex, maxCycles);
        switch (mapLocationIndex) {
            case MAP8X8 :
                parameters = new float[] {0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f};
                ((UCTDynamicProbaInactionPruning)experiment.getMaxPlayer()).setInactionAllowProbabilityOutnumbers(0f);
                break;
            case MAP12X12 :
                parameters = new float[] {0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f};
                ((UCTDynamicProbaInactionPruning)experiment.getMaxPlayer()).setInactionAllowProbabilityOutnumbers(0.2f);
                break;
            case MAP16X16 :
                parameters = new float[] {0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f};
                ((UCTDynamicProbaInactionPruning)experiment.getMaxPlayer()).setInactionAllowProbabilityOutnumbers(0.3f);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + mapLocationIndex);
        }

        for (float parameter : parameters) {
            ((UCTDynamicProbaInactionPruning)experiment.getMaxPlayer()).setInactionAllowProbabilityOutnumbered(parameter);
            System.out.println("** Starting Experiment with p1 (outnumbers) = " +
                    ((UCTDynamicProbaInactionPruning)experiment.getMaxPlayer()).getInactionAllowProbabilityOutnumbers() +
                    " and p2 (outnumbered) = " +
                    ((UCTDynamicProbaInactionPruning)experiment.getMaxPlayer()).getInactionAllowProbabilityOutnumbered());
            experiment.runMultipleMatchesSymmetric(totalNumberOfMatches, visualize, printAIStats);
        }

    }

    // D-RFP Parameter Test
    public static void testAllDynamicRFPParametersVsUCT(int mapLocationIndex, int maxCycles, int totalNumberOfMatches,
                                                        boolean visualize, boolean printAIStats) throws Exception {

        AI maxPlayer = new UCTDynamicFixedInactionPruning(unitTypeTable),
           minPlayer = new PlainUCT(unitTypeTable);

        // 8x8 : n1 = 1 - -> n2 test {5, 10, 50, 100, 500, 1000, 5000, 10000}
        // 12x12 : n1 = 5 -> n2 test {10, 50, 100, 500, 1000, 5000, 10000}
        // 16x16 : n1 = 1 -> n2 test {5, 10, 50, 100, 500, 1000, 5000, 10000}

        int[] parameters;
        initialize(maxPlayer, minPlayer, mapLocationIndex, maxCycles);
        switch (mapLocationIndex) {
            case MAP8X8 :
            case MAP16X16 :
                parameters = new int[] {5, 10, 50, 100, 500, 1000, 5000, 10000};
                ((UCTDynamicFixedInactionPruning)experiment.getMaxPlayer()).setAllowedInactionsOutnumbers(1);
                break;
            case MAP12X12 :
                parameters = new int[] {10, 50, 100, 500, 1000, 5000, 10000};
                ((UCTDynamicFixedInactionPruning)experiment.getMaxPlayer()).setAllowedInactionsOutnumbers(5);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + mapLocationIndex);
        }

        for (int parameter : parameters) {
            ((UCTDynamicFixedInactionPruning)experiment.getMaxPlayer()).setAllowedInactionsOutnumbered(parameter);
            System.out.println("** Starting Experiment with n1 (outnumbers) = " +
                    ((UCTDynamicFixedInactionPruning)experiment.getMaxPlayer()).getAllowedInactionsOutnumbers() +
                    " and n2 (outnumbered) = " +
                    ((UCTDynamicFixedInactionPruning)experiment.getMaxPlayer()).getAllowedInactionsOutnumbered());
            experiment.runMultipleMatchesSymmetric(totalNumberOfMatches, visualize, printAIStats);
        }
    }

    public static void testDynamicRPPParameterVsUCT(int mapLocationIndex, int maxCycles, int totalNumberOfMatches, float parameter,
                                             int numberOfTests, boolean visualize, boolean printAIStats) throws Exception {

        AI maxPlayer = new UCTDynamicProbaInactionPruning(unitTypeTable),
           minPlayer = new PlainUCT(unitTypeTable);

        initialize(maxPlayer, minPlayer, mapLocationIndex, maxCycles);

        ((UCTDynamicProbaInactionPruning)experiment.getMaxPlayer()).setInactionAllowProbabilityOutnumbered(parameter);

        switch (mapLocationIndex) {
            case MAP8X8 :
                ((UCTDynamicProbaInactionPruning)experiment.getMaxPlayer()).setInactionAllowProbabilityOutnumbers(0f);
                break;
            case MAP12X12 :
                ((UCTDynamicProbaInactionPruning)experiment.getMaxPlayer()).setInactionAllowProbabilityOutnumbers(0.2f);
                break;
            case MAP16X16 :
                ((UCTDynamicProbaInactionPruning)experiment.getMaxPlayer()).setInactionAllowProbabilityOutnumbers(0.3f);
                break;
        }

        for (int testId = 0; testId < numberOfTests; testId++) {
            System.out.println("** Starting Experiment " + testId + " with p1 (outnumbers) = " +
                    ((UCTDynamicProbaInactionPruning) experiment.getMaxPlayer()).getInactionAllowProbabilityOutnumbers() +
                    " and p2 (outnumbered) = " +
                    ((UCTDynamicProbaInactionPruning) experiment.getMaxPlayer()).getInactionAllowProbabilityOutnumbered());
            experiment.runMultipleMatchesSymmetric(totalNumberOfMatches, visualize, printAIStats);
        }
    }

    public static void testDynamicRFPParameterVsUCT(int mapLocationIndex, int maxCycles, int totalNumberOfMatches, int parameter,
                                             int numberOfTests, boolean visualize, boolean printAIStats) throws Exception {

        AI maxPlayer = new UCTDynamicFixedInactionPruning(unitTypeTable),
           minPlayer = new PlainUCT(unitTypeTable);

        initialize(maxPlayer, minPlayer, mapLocationIndex, maxCycles);

        ((UCTDynamicFixedInactionPruning)experiment.getMaxPlayer()).setAllowedInactionsOutnumbered(parameter);

        switch (mapLocationIndex) {
            case MAP8X8 :
            case MAP16X16 :
                ((UCTDynamicFixedInactionPruning)experiment.getMaxPlayer()).setAllowedInactionsOutnumbers(1);
                break;
            case MAP12X12 :
                ((UCTDynamicFixedInactionPruning)experiment.getMaxPlayer()).setAllowedInactionsOutnumbers(5);
                break;
        }

        for (int testId = 0; testId < numberOfTests; testId++) {
            System.out.println("** Starting Experiment " + testId + " with n1 (outnumbers) = " +
                    ((UCTDynamicFixedInactionPruning) experiment.getMaxPlayer()).getAllowedInactionsOutnumbers() +
                    " and n2 (outnumbered) = " +
                    ((UCTDynamicFixedInactionPruning) experiment.getMaxPlayer()).getAllowedInactionsOutnumbered());
            experiment.runMultipleMatchesSymmetric(totalNumberOfMatches, visualize, printAIStats);
        }
    }

    public static void runOneMatch(AI maxPlayer, AI minPlayer, int mapLocationIndex, int maxCycles, boolean visualize,
                                   boolean printAIStats) throws Exception {
        initialize(maxPlayer, minPlayer, mapLocationIndex, maxCycles);
        experiment.runSingleMatch(false, visualize, true, printAIStats, true);
    }

    public static void runMatches(AI maxPlayer, AI minPlayer, int mapLocationIndex, int maxCycles, int numberOfMatches,
                                  boolean visualize, boolean printAIStats) throws Exception {
        initialize(maxPlayer, minPlayer, mapLocationIndex, maxCycles);
        experiment.runMultipleMatchesSymmetric(numberOfMatches, visualize, printAIStats);
    }

    public static GameState runOneMatchReturnGameState(AI maxPlayer, AI minPlayer, int mapLocationIndex, int maxCycles,
                                                       boolean visualize, boolean printAIStats) throws Exception {
        initializeNoText(maxPlayer, minPlayer, mapLocationIndex, maxCycles);
        experiment.resetAll();
        experiment.runSingleMatchNoText(false, visualize, true, printAIStats, false);
        return experiment.getGameState();
    }

    public static int[] getUnitCountForEachPlayer(GameState gameState) {
        int[] counts = new int[] {0,0};
        for (Unit unit : gameState.getUnits())
            if (unit.getPlayer() >= 0)
                counts[unit.getPlayer()]++;
        return counts;
    }

    public static int getTrappedUnitsCount(GameState gameState, int player) {
        int count = 0;
        for (Unit unit : gameState.getUnits()) {
            if ((unit.getPlayer() == player) && unit.getUnitActions(gameState).size() <= 1)
                count++;
        }
        return count;
    }

    // WIP
    // ThisAIOnly : -1 for full tournament. Specific AI index, for that AI only
    public static void runTournament(List<AI> AIs, int thisAIOnly, int mapLocationIndex, int maxCycles,
                                     int iterations, String filename) throws Exception {

        RoundRobinTournament tournament = new RoundRobinTournament(AIs);
        List<String> maps = new ArrayList<>();
        maps.add(mapLocations[mapLocationIndex]);

        File outputFile = new File(filename);
        Writer output = new FileWriter(outputFile);

        tournament.setUSE_CONTINUING_ON_INTERRUPTIBLE(false);

        tournament.runTournament(thisAIOnly, maps, iterations, maxCycles, 100, -1,
                1000, 1000, true, false,
                false, false, false, unitTypeTable, null, output,
                new PrintWriter(System.out), null);
    }


    private static void simulate(GameState gameState, int simTime) throws Exception {
        boolean gameOver = false;
        AI playoutPolicy = new RandomBiasedAI(unitTypeTable);

        do {
            if (gameState.isComplete())
                gameOver = gameState.cycle();
            else {
                gameState.issue(playoutPolicy.getAction(0, gameState));
                gameState.issue(playoutPolicy.getAction(1, gameState));
            }
        } while (!gameOver && gameState.getTime() < simTime);
    }

    private static void UCTRIPFBranchingFactorTest(int mapLocationIndex, int allowedInactions, int maxCycles, int timeBudget, boolean visualize) throws Exception{

        GameState gameState = runOneMatchReturnGameState(new UCTFixedInactionPruning(unitTypeTable, allowedInactions),
                new UCT(unitTypeTable), mapLocationIndex, maxCycles, visualize, false);

        double branchingFactor = BranchingFactorCalculatorDouble.branchingFactorByResourceUsageFast(gameState.clone(),0);
        int unitsCount = getUnitCountForEachPlayer(gameState)[0];
        double averageUnitActions = Math.pow(branchingFactor, (1.0 / unitsCount));

        UCTFixedInactionPruningNode tree = new UCTFixedInactionPruningNode(gameState.clone(), null, 0);
        EvaluationFunction evaluationFunction = new SimpleSqrtEvaluationFunction3();

        CustomPlayerActionGenerator.IPAsRemoved = 0;

        long startTime = System.currentTimeMillis();
        long endTime = startTime;

        while ((endTime - startTime) <  timeBudget) {
            UCTFixedInactionPruningNode selected = tree.selectLeaf(0, startTime + timeBudget, 10,
                    evaluationFunction.upperBound(gameState), allowedInactions);

            if (selected != null) {
                GameState simGameState = selected.getGameState().clone();
                simulate(simGameState, simGameState.getTime() + 100);

                // (3) Evaluate the resulting state.
                double evaluation = evaluationFunction.evaluate(0, 1, simGameState);
                // Apply a discount factor.
                int time = simGameState.getTime() - gameState.getTime();
                evaluation = evaluation * Math.pow(0.99, time/10.0); // Discount factor.

                // (4) Backpropagate scores and visit counts.
                while (selected != null) {
                    selected.updateAccumulatedEvaluation(evaluation);
                    selected.incrementVisitCount();
                    selected = selected.getParent();
                }
            }
            endTime = System.currentTimeMillis();
        }

        System.out.println("Visited Actions: " + tree.getVisitCount());
        System.out.println("Branching Factor: " + branchingFactor);
        System.out.println("Unit Count: " + unitsCount);
        System.out.println("Avg UnitActions: " + averageUnitActions);
        System.out.println("Trapped: " + getTrappedUnitsCount(gameState,0));
        System.out.println("IPAs Removed: " + CustomPlayerActionGenerator.IPAsRemoved);

        CustomPlayerActionGenerator.IPAsRemoved = 0;
    }

    private static void UCTRIPPBranchingFactorTest(int mapLocationIndex, float allowProbability, int maxCycles, int timeBudget, boolean visualize) throws Exception {

        GameState gameState = runOneMatchReturnGameState(new UCTProbaInactionPruning(unitTypeTable, allowProbability),
                new UCT(unitTypeTable), mapLocationIndex, maxCycles, visualize, false);

        double branchingFactor = BranchingFactorCalculatorDouble.branchingFactorByResourceUsageFast(gameState.cloneIssue(new PlayerAction()),0);
        int unitsCount = getUnitCountForEachPlayer(gameState)[0];
        double averageUnitActions = Math.pow(branchingFactor, (1.0 / unitsCount));

        UCTProbaInactionPruningNode tree = new UCTProbaInactionPruningNode(gameState.clone(), null, 0);
        EvaluationFunction evaluationFunction = new SimpleSqrtEvaluationFunction3();

        CustomPlayerActionGenerator.IPAsRemoved = 0;

        List<UCTProbaInactionPruningNode> nodes = new LinkedList<>();
        List<PlayerAction> playerActions = new LinkedList<>();
        nodes.add(tree);

        long startTime = System.currentTimeMillis();
        long endTime = startTime;

        while ((endTime - startTime) <  timeBudget) {
            UCTProbaInactionPruningNode selected = tree.selectLeaf(0, startTime + timeBudget, 10,
                    evaluationFunction.upperBound(gameState), allowProbability);

            if (selected != null) {
                if (!nodes.contains(selected)) nodes.add(selected);

                // To identify allowed IPAs.
                int selectedIndex = selected.getParent().getChildren().indexOf(selected);
                playerActions.add(selected.getParent().getActions().get(selectedIndex));
                //--------------------------

                GameState simGameState = selected.getGameState().clone();
                simulate(simGameState, simGameState.getTime() + 100);

                // (3) Evaluate the resulting state.
                double evaluation = evaluationFunction.evaluate(0, 1, simGameState);
                // Apply a discount factor.
                int time = simGameState.getTime() - gameState.getTime();
                evaluation = evaluation * Math.pow(0.99, time/10.0); // Discount factor.

                // (4) Backpropagate scores and visit counts.
                while (selected != null) {
                    selected.updateAccumulatedEvaluation(evaluation);
                    selected.incrementVisitCount();
                    selected = selected.getParent();
                }
            }
            endTime = System.currentTimeMillis();
        }

        int ipasAllowed = getIpasAllowed(playerActions);


        System.out.println("Explored Actions: " + nodes.size());
        System.out.println("Visits Count: " + tree.getVisitCount());
        System.out.println("Branching Factor: " + branchingFactor);
        System.out.println("Unit Count: " + unitsCount);
        System.out.println("Avg UnitActions: " + averageUnitActions);
        System.out.println("Trapped: " + getTrappedUnitsCount(gameState,0));
        System.out.println("IPAs Removed: " + CustomPlayerActionGenerator.IPAsRemoved);
        System.out.println("IPAs Allowed: " + ipasAllowed);

        CustomPlayerActionGenerator.IPAsRemoved = 0;
    }

    private static void NMCTSRIPFBranchingFactorTest(int mapLocationIndex, int allowedInactions, int maxCycles, int timeBudget, boolean visualize) throws Exception {

        GameState gameState = runOneMatchReturnGameState(new NMCTSRandomInactivityFilteringFixed(unitTypeTable, allowedInactions),
                                                         new NaiveMCTS(unitTypeTable),
                                                         mapLocationIndex, maxCycles, visualize, false);

        double branchingFactor = BranchingFactorCalculatorDouble.branchingFactorByResourceUsageFast(gameState.cloneIssue(new PlayerAction()),0);
        int unitsCount = getUnitCountForEachPlayer(gameState)[0];
        double averageUnitActions = Math.pow(branchingFactor, (1.0 / unitsCount));


        // One MCTS iteration
        NMCTSRandomInactivityFilteringFixedNode_BFTest tree = new
                NMCTSRandomInactivityFilteringFixedNode_BFTest(0, gameState.clone(), null, 0, true);
        EvaluationFunction evaluationFunction = new SimpleSqrtEvaluationFunction3();

        NMCTSRandomInactivityFilteringFixedNode_BFTest.IPAsRemoved = 0;
        List<NMCTSRandomInactivityFilteringFixedNode_BFTest> nodes = new LinkedList<>();
//        nodes.add(tree);

        long startTime = System.currentTimeMillis();
        long endTime = startTime;

        while ((endTime - startTime) <  timeBudget) {
            NMCTSRandomInactivityFilteringFixedNode_BFTest selected = tree.selectLeaf(0, 0.4f, 0.0f, 0.3f,
                    NMCTSRandomInactivityFilteringFixedNode_BFTest.EPSILON_GREEDY, 1, 0,
                    evaluationFunction.upperBound(gameState), allowedInactions);

            if (selected != null) {
                if (!nodes.contains(selected)) nodes.add(selected);
                // (2) Simulation and evaluation
                GameState simGameState = selected.getGameState().clone();
                simulate(simGameState, simGameState.getTime() + 100);

                int time = simGameState.getTime() - gameState.getTime();
                double evaluation = evaluationFunction.evaluate(0, 1, simGameState);
                evaluation *= Math.pow(0.99, time / 10.0);

                // (3) Backpropagation
                selected.backpropagate(evaluation, null);
            }
            endTime = System.currentTimeMillis();
        }

        List<PlayerAction> playerActions = new LinkedList<>();
        for (NMCTSRandomInactivityFilteringFixedNode_BFTest node : nodes) {
            int index = node.getParent().getChildren().indexOf(node);
            playerActions.add(node.getParent().getActions().get(index));
        }
        int ipasAllowed = getIpasAllowed(playerActions);

        System.out.println("Explored Actions: " + nodes.size());
        System.out.println("Visited Actions: " + tree.getVisitCount());
        System.out.println("Branching Factor: " + branchingFactor);
        System.out.println("Unit Count: " + unitsCount);
        System.out.println("Avg UnitActions: " + averageUnitActions);
        System.out.println("Trapped: " + getTrappedUnitsCount(gameState,0));
        System.out.println("IPAs Removed: " + NMCTSRandomInactivityFilteringFixedNode_BFTest.IPAsRemoved);
        System.out.println("IPAs Allowed: " + ipasAllowed);

        NMCTSRandomInactivityFilteringFixedNode_BFTest.IPAsRemoved = 0;
    }

    private static void NMCTSRIPPBranchingFactorTest(int mapLocationIndex, float allowProbability, int maxCycles, int timeBudget, boolean visualize) throws Exception {

        GameState gameState = runOneMatchReturnGameState(new NMCTSRandomInactivityFilteringProba(unitTypeTable, allowProbability),
                new NaiveMCTS(unitTypeTable),
                mapLocationIndex, maxCycles, visualize, false);

        long branchingFactor = BranchingFactorCalculatorLong.branchingFactorByResourceUsageSeparatingFast(gameState.clone(),0);
        int unitsCount = getUnitCountForEachPlayer(gameState)[0];
        double averageUnitActions = Math.pow(branchingFactor, (1.0 / unitsCount));
        double ipasCount = Math.round(branchingFactor - Math.pow(averageUnitActions - 1, unitsCount));


        // One MCTS iteration
        NMCTSRandomInactivityFilteringProbaNode_BFTest tree = new
                NMCTSRandomInactivityFilteringProbaNode_BFTest(0, gameState.clone(), null, 0, true);
        EvaluationFunction evaluationFunction = new SimpleSqrtEvaluationFunction3();

        NMCTSRandomInactivityFilteringProbaNode_BFTest.IPAsRemoved = 0;
        List<NMCTSRandomInactivityFilteringProbaNode_BFTest> nodes = new LinkedList<>();

        long startTime = System.currentTimeMillis();
        long endTime = startTime;

        while ((endTime - startTime) <  timeBudget) {
            NMCTSRandomInactivityFilteringProbaNode_BFTest selected = tree.selectLeaf(0, 0.4f, 0.0f, 0.3f,
                    NMCTSRandomInactivityFilteringProbaNode_BFTest.EPSILON_GREEDY, 1, 0,
                    evaluationFunction.upperBound(gameState), allowProbability);

            if (selected != null) {
                if (!nodes.contains(selected)) nodes.add(selected);
                // (2) Simulation and evaluation
                GameState simGameState = selected.getGameState().clone();
                simulate(simGameState, simGameState.getTime() + 100);

                int time = simGameState.getTime() - gameState.getTime();
                double evaluation = evaluationFunction.evaluate(0, 1, simGameState);
                evaluation *= Math.pow(0.99, time / 10.0);

                // (3) Backpropagation
                selected.backpropagate(evaluation, null);
            }
            endTime = System.currentTimeMillis();
        }

        List<PlayerAction> playerActions = new LinkedList<>();
        for (NMCTSRandomInactivityFilteringProbaNode_BFTest node : nodes) {
            int index = node.getParent().getChildren().indexOf(node);
            playerActions.add(node.getParent().getActions().get(index));
        }

        int ipasAllowed = getIpasAllowed(playerActions);
        int exploredActions = nodes.size();
        int visitsCount = tree.getVisitCount();
        int trappedUnits = getTrappedUnitsCount(gameState, 0);
        long ipasRemoved = tree.getRejectedActions().size();

        System.out.print(branchingFactor + "\t");
        System.out.print(ipasCount + "\t");
        System.out.print(exploredActions + "\t");
        System.out.print(ipasRemoved + "\t");
        System.out.print(ipasAllowed + "\t");
        System.out.print(unitsCount + "\t");
        System.out.print(trappedUnits + "\t");
        System.out.print(averageUnitActions + "\t");
        System.out.println(visitsCount + "\t");

        NMCTSRandomInactivityFilteringProbaNode_BFTest.IPAsRemoved = 0;
    }

    private static void NMCTSRIPPBranchingFactorTestSwitched(int mapLocationIndex, float allowProbability, int maxCycles, int timeBudget, boolean visualize) throws Exception {

        GameState gameState = runOneMatchReturnGameState(new NaiveMCTS(unitTypeTable),
                new NMCTSRandomInactivityFilteringProba(unitTypeTable, allowProbability),
                mapLocationIndex, maxCycles, visualize, false);

        double branchingFactor = BranchingFactorCalculatorDouble.branchingFactorByResourceUsageSeparatingFast(gameState.clone(),1);
        int unitsCount = getUnitCountForEachPlayer(gameState)[1];
        double averageUnitActions = Math.pow(branchingFactor, (1.0 / unitsCount));
        double ipasCount = Math.round(branchingFactor - Math.pow(averageUnitActions - 1, unitsCount));


        // One MCTS iteration
        NMCTSRandomInactivityFilteringProbaNode_BFTest tree = new
                NMCTSRandomInactivityFilteringProbaNode_BFTest(1, gameState.clone(), null, 0, true);
        EvaluationFunction evaluationFunction = new SimpleSqrtEvaluationFunction3();

        NMCTSRandomInactivityFilteringProbaNode_BFTest.IPAsRemoved = 0;
        List<NMCTSRandomInactivityFilteringProbaNode_BFTest> nodes = new LinkedList<>();

        long startTime = System.currentTimeMillis();
        long endTime = startTime;

        while ((endTime - startTime) <  timeBudget) {
            NMCTSRandomInactivityFilteringProbaNode_BFTest selected = tree.selectLeaf(1, 0.4f, 0.0f, 0.3f,
                    NMCTSRandomInactivityFilteringProbaNode_BFTest.EPSILON_GREEDY, 1, 0,
                    evaluationFunction.upperBound(gameState), allowProbability);

            if (selected != null) {
                if (!nodes.contains(selected)) nodes.add(selected);
                // (2) Simulation and evaluation
                GameState simGameState = selected.getGameState().clone();
                simulate(simGameState, simGameState.getTime() + 100);

                int time = simGameState.getTime() - gameState.getTime();
                double evaluation = evaluationFunction.evaluate(1, 0, simGameState);
                evaluation *= Math.pow(0.99, time / 10.0);

                // (3) Backpropagation
                selected.backpropagate(evaluation, null);
            }
            endTime = System.currentTimeMillis();
        }

        List<PlayerAction> playerActions = new LinkedList<>();
        for (NMCTSRandomInactivityFilteringProbaNode_BFTest node : nodes) {
            int index = node.getParent().getChildren().indexOf(node);
            playerActions.add(node.getParent().getActions().get(index));
        }

        int ipasAllowed = getIpasAllowed(playerActions);
        int exploredActions = nodes.size();
        int visitsCount = tree.getVisitCount();
        int trappedUnits = getTrappedUnitsCount(gameState, 1);
        long ipasRemoved = NMCTSRandomInactivityFilteringProbaNode_BFTest.IPAsRemoved;

        System.out.print(branchingFactor + "\t");
        System.out.print(ipasCount + "\t");
        System.out.print(exploredActions + "\t");
        System.out.print(ipasRemoved + "\t");
        System.out.print(ipasAllowed + "\t");
        System.out.print(unitsCount + "\t");
        System.out.print(trappedUnits + "\t");
        System.out.print(averageUnitActions + "\t");
        System.out.println(visitsCount + "\t");

        NMCTSRandomInactivityFilteringProbaNode_BFTest.IPAsRemoved = 0;
    }

    private static int getIpasAllowed(List<PlayerAction> playerActions) {

        int ipasAllowed = 0;
        for (PlayerAction playerAction : playerActions) {
            for (Pair<Unit, UnitAction> unitActionPair : playerAction.getActions())
                if (unitActionPair.m_b.getType() == UnitAction.TYPE_NONE) {
                    ipasAllowed++;
                    break;
                }
        }
        return ipasAllowed;
    }

    public static void NMCTSRIPPBFExperiment(int mapLocationIndex, float allowProbability, int maxCycles, int timeBudget, boolean visualize, int nbIterations) throws Exception {

        for (int i = 0; i < nbIterations; i++)
            NMCTSRIPPBranchingFactorTest(mapLocationIndex, allowProbability, maxCycles, timeBudget, visualize);
        System.out.println("------------ Position Switch ----------------");
        for (int i = 0; i < nbIterations; i++)
            NMCTSRIPPBranchingFactorTestSwitched(mapLocationIndex, allowProbability, maxCycles, timeBudget, visualize);
    }




}