package expr.bones;

import java.util.LinkedList;
import java.util.List;

public class MiniMaxNode {
    int bonesInHeap;
    int player;
    int score = 0;
    int depth = 0;
    MiniMaxNode parent = null;
    int alpha = -10, beta = 10;
    List<MiniMaxNode> children = new LinkedList<>();

    public MiniMaxNode() {}

    public MiniMaxNode(int bonesInHeap, int player) {
        this.bonesInHeap = bonesInHeap;
        this.player = player;
    }

    public void setBonesInHeap(int bonesInHeap) {
        this.bonesInHeap = bonesInHeap;
    }

    public int getBonesInHeap() {
        return bonesInHeap;
    }

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void addChild(MiniMaxNode child) {
        children.add(child);
    }

    public List<MiniMaxNode> getChildren() {
        return children;
    }

    public void setParent(MiniMaxNode parent) {
        this.parent = parent;
    }

    public MiniMaxNode getParent() {
        return parent;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getBeta() {
        return beta;
    }

    public void setBeta(int beta) {
        this.beta = beta;
    }
}
