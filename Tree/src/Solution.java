import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.*;

import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum Color {
    RED, GREEN
}

abstract class Tree {

    private int value;
    private Color color;
    private int depth;

    public Tree(int value, Color color, int depth) {
        this.value = value;
        this.color = color;
        this.depth = depth;
    }

    public int getValue() {
        return value;
    }

    public Color getColor() {
        return color;
    }

    public int getDepth() {
        return depth;
    }

    public abstract void accept(TreeVis visitor);
}

class TreeNode extends Tree {

    private ArrayList<Tree> children = new ArrayList<>();

    public TreeNode(int value, Color color, int depth) {
        super(value, color, depth);
    }

    public void accept(TreeVis visitor) {
        visitor.visitNode(this);

        for (Tree child : children) {
            child.accept(visitor);
        }
    }

    public void addChild(Tree child) {
        children.add(child);
    }
}

class TreeLeaf extends Tree {

    public TreeLeaf(int value, Color color, int depth) {
        super(value, color, depth);
    }

    public void accept(TreeVis visitor) {
        visitor.visitLeaf(this);
    }
}

abstract class TreeVis
{
    public abstract int getResult();
    public abstract void visitNode(TreeNode node);
    public abstract void visitLeaf(TreeLeaf leaf);

}

class SumInLeavesVisitor extends TreeVis {

    int sum = 0;

    public int getResult() {
        return sum;
    }

    public void visitNode(TreeNode node) {
    }

    public void visitLeaf(TreeLeaf leaf) {
        sum += leaf.getValue();
    }
}

class ProductOfRedNodesVisitor extends TreeVis {

    long product = 1;
    final int m = 1000000007;

    public int getResult() {
        return (int)product;
    }

    public void visitNode(TreeNode node) {
        if (node.getValue() != 0 && node.getColor() == Color.RED)
            product = ((product * node.getValue()) % m + m) % m;
    }

    public void visitLeaf(TreeLeaf leaf) {
        if (leaf.getValue() != 0 && leaf.getColor() == Color.RED)
            product = ((product * leaf.getValue()) % m + m) % m;
    }
}

class FancyVisitor extends TreeVis {

    int difference = 0;
    public int getResult() {
        return Math.abs(difference);
    }

    public void visitNode(TreeNode node) {
        if (node.getDepth() % 2 == 0)
            difference += node.getValue();
    }

    public void visitLeaf(TreeLeaf leaf) {
        if (leaf.getColor() == Color.GREEN)
            difference -= leaf.getValue();
    }
}

public class Solution {

    public static Tree solve() {
        Scanner scanner = new Scanner(System.in);

        int nodeNumber = scanner.nextInt();

        Tree[] tree = new Tree[nodeNumber];

        int[] values = Stream.generate(scanner::nextInt).limit(nodeNumber).mapToInt(i -> i).toArray();

        int[] colors = Stream.generate(scanner::nextInt).limit(nodeNumber).mapToInt(i -> i).toArray();

        tree[0] = new TreeNode(
                values[0],
                colors[0] == 1 ? Color.GREEN : Color.RED,
                0
        );

        Map<Integer, List<Integer>> pairs = new HashMap<>();
        for (int i = 0; i < nodeNumber - 1; i++) {
            int[] pair = Stream.generate(scanner::nextInt).limit(2).mapToInt(l -> l).toArray();
            if(pairs.containsKey(pair[0]))
                pairs.get(pair[0]).add(pair[1]);
            else
                pairs.put(pair[0], new ArrayList<>(List.of(pair[1])));

            if(pairs.containsKey(pair[1]))
                pairs.get(pair[1]).add(pair[0]);
            else
                pairs.put(pair[1], new ArrayList<>(List.of(pair[0])));
        }

        List<Integer> parents = new ArrayList<>(List.of(1));
        while(!parents.isEmpty()){
            final int parentNumber = parents.get(0);
            List<Integer> currentPairs = pairs.get(parentNumber);

            for (int childNumber: currentPairs) {
                if (pairs.get(childNumber).size() > 1) {
                    tree[childNumber - 1] = new TreeNode(
                            values[childNumber - 1],
                            colors[childNumber - 1] == 1 ? Color.GREEN : Color.RED,
                            tree[parentNumber - 1].getDepth() + 1
                    );

                    parents.add(childNumber);
                }
                else
                    tree[childNumber - 1] = new TreeLeaf(
                            values[childNumber - 1],
                            colors[childNumber - 1] == 1 ? Color.GREEN : Color.RED,
                            tree[parentNumber - 1].getDepth() + 1
                    );

                pairs.get(childNumber).removeIf(x -> x.equals(parentNumber));

                ((TreeNode)tree[parentNumber - 1]).addChild(tree[childNumber - 1]);

            }
            parents.remove(0);
        }

        return tree[0];
    }


    public static void main(String[] args) {
        Tree root = solve();
        SumInLeavesVisitor vis1 = new SumInLeavesVisitor();
        ProductOfRedNodesVisitor vis2 = new ProductOfRedNodesVisitor();
        FancyVisitor vis3 = new FancyVisitor();

        root.accept(vis1);
        root.accept(vis2);
        root.accept(vis3);

        int res1 = vis1.getResult();
        int res2 = vis2.getResult();
        int res3 = vis3.getResult();

        System.out.println(res1);
        System.out.println(res2);
        System.out.println(res3);
    }
}