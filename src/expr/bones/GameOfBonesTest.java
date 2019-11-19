package expr.bones;

import expr.bones.GameOfBones;
import expr.bones.MiniMax;
import gui.PhysicalGameStateJFrame;
import gui.PhysicalGameStatePanel;
import rts.PhysicalGameState;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

import java.util.Arrays;
import java.util.Scanner;

public class GameOfBonesTest {

    public static void main(String[] args) throws Exception {
        GameOfBones game = new GameOfBones(20);
        Scanner scanner = new Scanner(System.in);
        MiniMax miniMax;

        while (game.winner() < 0) {
            System.out.println("How much would you take ? " + game.getBonesInHeap() + " Bones remaining.");
            int bones = scanner.nextInt();
            game.takeBones(bones, 0);
            int miniMaxBones = 0;
            if (game.getBonesInHeap() > 0) {
                miniMax = new MiniMax(game.getBonesInHeap(), 1, true);
                miniMaxBones = miniMax.getBestAction();
                System.out.println("I will take " + miniMaxBones + " Bones (Tree Depth : " + miniMax.getTreeDepth() +
                        " | CutOffs : " + miniMax.getCutOffs() + ")");
            }
            game.takeBones(miniMaxBones, 1);
//            game.takeBonesRandom(1);
        }
        System.out.println("Winner: " + game.winner());

//        System.out.println(game.getPossibleStates());

//        game.takeBones(1,0);
//        game.takeBones(3,1);
//        game.takeBones(1,0);
//        game.takeBones(3,1);

//        System.out.println("Remaining Bones: " + game.getBonesInHeap());
//        System.out.println("Game Winner: " + game.winner());
    }
}
