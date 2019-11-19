package expr.bones;

import java.util.List;

public class MiniMax {
    MiniMaxNode tree;
    int treeDepth = 0;
    int cutOffs = 0;
    /*
     * Constructs a MiniMax Object, and constructs the minimax tree inside.
     */
    public MiniMax(int bonesInHeap, int player, boolean alphaBeta) {
        tree = new MiniMaxNode(bonesInHeap, player);
        if (alphaBeta)
            constructTreeAlphaBeta2(tree);
        else
            constructTree(tree);
    }

    /*
     * Constructs a MiniMax tree, filling all scores on the way.
     */
    public void constructTree(MiniMaxNode root) {
        List<Integer> states = GameOfBones.getPossibleStates(root.getBonesInHeap());
        for (int state : states) {
            MiniMaxNode node = new MiniMaxNode(state, 1 - root.player);
            node.setDepth(root.getDepth() + 1);
            node.setParent(root);
            root.addChild(node);
            if (node.getBonesInHeap() == 0) {
                if (root.player == 0) node.setScore(1);
                else node.setScore(-1);
                treeDepth = Math.max(node.getDepth(), treeDepth);
            } else if (node.getBonesInHeap() > 0)
                constructTree(node);
        }
        MiniMaxNode bestChild = getBestChild(root.getPlayer(), root);
        root.setScore(bestChild.getScore());
    }

    /*
     * Returns the best node in the list of child nodes in the given node.
     */
    private MiniMaxNode getBestChild(int player, MiniMaxNode node) {
        List<MiniMaxNode> children = node.getChildren();
        MiniMaxNode bestChild = null;
        int bestScore;
        if (player == 0)
            bestScore = -1;
        else
            bestScore = 1;
        for (MiniMaxNode child : children) {
            if (player == 0) {
                if (child.getScore() >= bestScore) {
                    bestScore = child.getScore();
                    bestChild = child;
                }
            } else {
                if (child.getScore() <= bestScore) {
                    bestScore = child.getScore();
                    bestChild = child;
                }
            }
        }
        return bestChild;
    }

    /*
     * Returns the best action (number of bones to take) calculated from the best child
     * of the tree root.
     */
    public int getBestAction() {
        MiniMaxNode bestChild = getBestChild(tree.getPlayer(), tree);
        return tree.getBonesInHeap() - bestChild.getBonesInHeap();
    }

    /*
     * Returns the tree depth.
     */
    public int getTreeDepth() {
        return treeDepth;
    }

    public int getCutOffs() {
        return cutOffs;
    }

    public void constructTreeAlphaBeta(MiniMaxNode root) {

        List<Integer> possibleRemainingBones = GameOfBones.getPossibleStates(root.getBonesInHeap());

        if (root.parent == null) {
            root.setAlpha(-10);
            root.setBeta(10);
        }

        for (int remainingBones : possibleRemainingBones) {
            MiniMaxNode node = new MiniMaxNode(remainingBones, 1 - root.player);
            node.setDepth(root.getDepth() + 1);
            node.setParent(root);
            node.setAlpha(root.getAlpha());
            node.setBeta(root.getBeta());
            root.addChild(node);
            if (node.getBonesInHeap() == 0) {
                if (root.player == 0) {
                    node.setScore(1 / node.getDepth());
                    root.setAlpha(Math.max(root.getAlpha(), node.getScore()));
                }
                else {
                    node.setScore(-1 / node.getDepth());
                    root.setBeta(Math.min(root.getBeta(), node.getScore()));
                }
                treeDepth = Math.max(node.getDepth(), treeDepth);
                backtrackAlphaBeta(node);
                if (root.getAlpha() >= root.getBeta()) {
                    cutOffs++;
                    break;
                }
            } else if (node.getBonesInHeap() > 0)
                constructTree(node);
        }

//        MiniMaxNode bestChild = getBestChild(root.getPlayer(), root);
        root.setScore(root.getPlayer() == 0 ? root.getAlpha() : root.getBeta());
        /*if (root.getPlayer() == 0)
            root.setAlpha(Math.max(root.getAlpha(), root.getScore()));
        else
            root.setBeta(Math.min(root.getBeta(), root.getScore()));*/

    }

    private void backtrackAlphaBeta(MiniMaxNode node) {
        MiniMaxNode parent = node.parent;
        if (parent != null) {
            int parentPlayer = node.parent.getPlayer();
            if (parentPlayer == 0) // is max
                parent.setAlpha(Math.max(parent.getAlpha(), node.getScore()));
            else
                parent.setBeta(Math.min(parent.getBeta(), node.getScore()));
        }
    }

    private void constructTreeAlphaBeta2(MiniMaxNode root) {
        List<Integer> possibleRemainingBones = GameOfBones.getPossibleStates(root.getBonesInHeap());
        int player = root.getPlayer();

        for (int remainingBones : possibleRemainingBones) {
            MiniMaxNode newNode = new MiniMaxNode(remainingBones, 1 - player);
            newNode.setDepth(root.getDepth() + 1);
            newNode.setParent(root);
            newNode.setAlpha(root.getAlpha());
            newNode.setBeta(root.getBeta());
            root.addChild(newNode);

            if (remainingBones == 0) {
                if (player == 0) {
                    newNode.setScore(1);
                    root.setAlpha(Math.max(root.getAlpha(), 1));
//                    root.setScore(root.getAlpha());
                } else {
                    newNode.setScore(-1);
                    root.setBeta(Math.min(root.getBeta(), -1));
//                    root.setScore(root.getBeta());
                }
                treeDepth = Math.max(newNode.getDepth(), treeDepth);
                if (root.getAlpha() >= root.getBeta()) {
                    cutOffs++;
                    break;
                }
            } else {
                if (root.getAlpha() >= root.getBeta()) {
                    cutOffs++;
                    break;
                }
                constructTreeAlphaBeta2(newNode);
            }
        }

        root.setScore(getBestChild(root.getPlayer(), root).getScore());
        if (root.getParent() != null) {
            if (root.getParent().getPlayer() == 0)
                root.getParent().setAlpha(Math.max(root.getParent().getAlpha(), root.getScore()));
            else
                root.getParent().setBeta(Math.min(root.getParent().getBeta(), root.getScore()));
        }

    }



    /*private void propagateScores(MiniMaxNode node) {
        List<MiniMaxNode> children = node.getChildren();
        for (MiniMaxNode child : children) {
            if (child.getBonesInHeap() == 0) {
                if (node.player == 0) child.setScore(1);
                else child.setScore(-1);
            } else
                propagateScores(child);
        }
        MiniMaxNode bestChild = getBestChild(node.getPlayer(), node);
        node.setScore(bestChild.getScore());
    }*/


    /*public boolean checkWin() throws Exception {
        checkWin(tree);
        return tree.getScore() == 1;
    }

    private void checkWin(MiniMaxNode node) throws Exception {
        List<MiniMaxNode> children = node.getChildren();
        boolean isMaxPlayer = (node.player == 0);
        for (MiniMaxNode child : children) {
            if (child.getBonesInHeap() == 0)
                child.setScore(isMaxPlayer ? 1 : -1);
            else
                checkWin(child);
        }
        MiniMaxNode bestChild = getBestChild(node.player, children);
        node.setScore(bestChild.getScore());
    }

    private MiniMaxNode getBestChild(int player, List<MiniMaxNode> children) throws Exception {
        if (!children.isEmpty()) {
            MiniMaxNode best = children.get(0);
            for (MiniMaxNode child : children) {
                if (player == 0)
                    if (best.getScore() < child.getScore()) best = child;
                    else if (best.getScore() > child.getScore()) best = child;
            }
            return best;
        } else
            throw new Exception();
    }

    public int getOptimalAction(int player) throws Exception {
        checkWin(tree);
        MiniMaxNode bestChild = getBestChild(player, tree.getChildren());
        return tree.getBonesInHeap() - bestChild.getBonesInHeap();
    }*/
}
