package expr.bones;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class GameOfBones {
    static int DEFAULT_BONE_COUNT = 20;
    int bonesInHeap = 0;
    int lastPlayer = 1;

    public GameOfBones() {
        setBonesInHeap(DEFAULT_BONE_COUNT);
    }

    public GameOfBones(int bonesInHeap) {
        setBonesInHeap(bonesInHeap);
    }

    public void takeBones(int count, int player) {
        if ((count >= 1 && count <= 3) && bonesInHeap > 0) {
            if (bonesInHeap <= count)
                bonesInHeap = 0;
            else
                bonesInHeap -= count;
            lastPlayer = player;
        }
    }

    public void takeBonesRandom(int player) {
        Random random = new Random();
        int count = random.nextInt(3) + 1;
        takeBones(count, player);
    }

    public int winner() {
        if (bonesInHeap == 0)
            return lastPlayer;
        else return -1;
    }

    public static List<Integer> getPossibleStates(int bonesInHeap) {
        List<Integer> states = new LinkedList<>();
        for (int choice = 1; choice <= 3; choice++)
            if (bonesInHeap - choice >= 0)
                states.add(bonesInHeap - choice);
        return states;
    }

    public int getBonesInHeap() {
        return bonesInHeap;
    }

    public void setBonesInHeap(int bonesInHeap) {
        this.bonesInHeap = bonesInHeap;
    }
}
