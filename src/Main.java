import aes.uct.PlainUCT;
import aes.uct.UCTRandomPruningFixed;
import aes.uct.UCTRandomPruningProba;
import aes.uct.emptyactions.UCTDynamicFixedInactionPruning;
import aes.uct.emptyactions.UCTDynamicProbaInactionPruning;
import aes.uct.emptyactions.UCTFixedInactionPruning;
import aes.uct.emptyactions.UCTProbaInactionPruning;
import ai.core.AI;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;
import tournaments.RoundRobinTournament;

import java.io.PrintWriter;
import java.util.ArrayList;
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
            "maps\\melee14x12Mixed18.xml"};

    static final int MAP8X8 = 0, MAP10X10 = 1, MAP12X12 = 2, MAP16X16 = 3, MAP14X12MELEE18X = 4;
    static final int CYCLES8X8 = 3000, CYCLES10x10 = 3250, CYCLES12x12 = 3500, CYCLES16x16 = 4000;

    static AI maxPlayer = new UCTProbaInactionPruning(unitTypeTable, 0.6f), // Player at the top, Blue colored.
              minPlayer = new PlainUCT(unitTypeTable); // Player at the Bottom, Red colored.

    public static void initialize(AI maxPlayer, AI minPlayer, int mapLocationIndex, int maxCycles) throws Exception {
        physicalGameState = PhysicalGameState.load(mapLocations[mapLocationIndex], unitTypeTable);
        experiment = new Experiments(maxPlayer, minPlayer, unitTypeTable, physicalGameState, maxCycles);
//        experiment.setMaxCycles(maxCycles);

        System.out.println("Blue (Player 0 Max) : " + maxPlayer.getClass().getSimpleName());
        System.out.println("Red  (Player 1 Min) : " + minPlayer.getClass().getSimpleName());
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

        testDynamicRPPParameterVsUCT(MAP16X16, CYCLES16x16, 50, 0, 10, false, false);

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

    // WIP
    public void runTournament() throws Exception {
        List<AI> AIs = new ArrayList<>();
        List<String> maps = new ArrayList<>();
        AIs.add(new PlainUCT(unitTypeTable));
        AIs.add(new UCTProbaInactionPruning(unitTypeTable));
        maps.add(mapLocations[MAP8X8]);
        RoundRobinTournament tournament = new RoundRobinTournament(AIs);

        tournament.runTournament(-1, maps, 25, CYCLES8X8, 100, -1,
                1000, 1000, true, false, false,
                false, false, unitTypeTable, null,
                new PrintWriter(System.out), new PrintWriter(System.out), null);
    }


}