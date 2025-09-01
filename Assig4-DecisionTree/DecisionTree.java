// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2025T2, Assignment 4
 * Name: Tom Davey
 * Username: daveytom
 * ID: 300679670
 */

/**
 * Implements a decision tree that asks a user yes/no questions to determine a decision.
 * Eg, asks about properties of an animal to determine the type of animal.
 * 
 * A decision tree is a tree in which all the internal nodes have a question, 
 * The answer to the question determines which way the program will
 *  proceed down the tree.  
 * All the leaf nodes have the decision (the kind of animal in the example tree).
 *
 * The decision tree may be a predermined decision tree, or it can be a "growing"
 * decision tree, where the user can add questions and decisions to the tree whenever
 * the tree gives a wrong answer.
 *
 * In the growing version, when the program guesses wrong, it asks the player
 * for another question that would help it in the future, and adds it (with the
 * correct answers) to the decision tree. 
 *
 */

import ecs100.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;

public class DecisionTree {

    public DTNode theTree;    // root of the decision tree;

    /**
     * Setup the GUI and make a sample tree
     */
    public static void main(String[] args){
        DecisionTree dt = new DecisionTree();
        dt.setupGUI();
        dt.loadTree("sample-animal-tree.txt");
    }

    /**
     * Set up the interface
     */
    public void setupGUI(){
        UI.addButton("Load Tree", ()->{loadTree(UIFileChooser.open("File with a Decision Tree"));});
        UI.addButton("Print Tree", this::printTree);
        UI.addButton("Run Tree", this::runTree);
        UI.addButton("Grow Tree", this::growTree);
        UI.addButton("Save Tree", this::saveTree);  // for completion
        UI.addButton("Draw Tree", this::drawTree);  // for challenge
        UI.addButton("Reset", ()->{loadTree("sample-animal-tree.txt");});
        UI.addButton("Quit", UI::quit);
        UI.setDivider(0.5);
    }

    /**  
     * Print out the contents of the decision tree in the text pane.
     * The root node should be at the top, followed by its "yes" subtree,
     * and then its "no" subtree.
     * Needs a recursive "helper method" which is passed a node.
     * 
     * COMPLETION:
     * Each node should be indented by how deep it is in the tree.
     * The recursive "helper method" is passed a node and an indentation string.
     *  (The indentation string will be a string of space characters)
     */

    public void printTree(){
        UI.clearText();

        // If tree exists, print tree 
        if (theTree != null) {
            printSubTree(theTree, "", "");
        }
    }

    private void printSubTree(DTNode node, String indent, String branch){
        if (node == null) return;

        // first one, no yes or no yet
        UI.println(indent + branch + node.getText());

        // Print tree with indentation and y/n 
        if(!node.isAnswer()){
            printSubTree(node.getYes(), indent + " ", "Y: ");
            printSubTree(node.getNo(), indent + " ", "N: ");
        }
    }

    /**
     * Run the tree by starting at the top (of theTree), and working
     * down the tree until it gets to a leaf node (a node with no children)
     * If the node is a leaf it prints the answer in the node
     * If the node is not a leaf node, then it asks the question in the node,
     * and depending on the answer, goes to the "yes" child or the "no" child.
     */

    public void runTree() {
        // If no tree is loaded, quit
        if (theTree == null) {
            UI.println("No tree loaded.");
            return;
        }

        DTNode node = theTree;

        // While there is no answer
        while (!node.isAnswer()) {
            boolean ans = UI.askBoolean("Is it true: " + node.getText() + " (Yes/No)");

            // If true or false, 
            if (ans) {
                node = node.getYes();
            } else {
                node = node.getNo();
            }
        }

        UI.println("Answer: " + node.getText());
    }

    /**
     * Grow the tree by allowing the user to extend the tree.
     * Like runTree, it starts at the top (of theTree), and works its way down the tree
     *  until it finally gets to a leaf node. 
     * If the current node has a question, then it asks the question in the node,
     * and depending on the answer, goes to the "yes" child or the "no" child.
     * If the current node is a leaf it prints the decision, and asks if it is right.
     * If it was wrong, it
     *  - asks the user what the decision should have been,
     *  - asks for a question to distinguish the right decision from the wrong one
     *  - changes the text in the node to be the question
     *  - adds two new children (leaf nodes) to the node with the two decisions.
     */
    public void growTree () {
        UI.clearText();
        
        // If no tree is loaded
        if (theTree == null) {
            UI.println("No tree loaded.");
            return;
        }

        DTNode node = theTree;

        // While there is no answer
        while (!node.isAnswer()) {
            boolean ans = UI.askBoolean("Is it true: " + node.getText() + " (Yes/No)");

            // If true or false
            if (ans) {
                node = node.getYes();
            } else {
                node = node.getNo();
            }
        }
        

        UI.println("I think it is: " + node.getText());
        boolean correct = UI.askBoolean("Am I right? ");

        if (!correct){
            String correctAnswer = UI.askString("What was the correct answer?" );
            
            UI.println("Oh, I can't distinguish " + correctAnswer + " from " + node.getText() + ".");
            UI.println("Tell me something that is true for for a " + correctAnswer + " and not " + node.getText() + "?");

            String property = UI.askString("Property: ");
            String oldAnswer = node.getText();

            boolean propertyAppliesToCorrect = true;

            // Update the tree node, property becomes question, old and new answers become children
            node.setText(property);

            if (propertyAppliesToCorrect) {
                node.setChildren(new DTNode(correctAnswer), new DTNode(oldAnswer));
            } else {
                node.setChildren(new DTNode(oldAnswer), new DTNode(correctAnswer));
            }

            UI.println("Got it! I’ll remember that for next time.");
        } else {
            UI.println("Answer: " + node.getText());
        }
    }

    // Completion and Challenge parts

    public void saveTree(){

        // If no tree is loaded
        if (theTree == null) {
            UI.println("No tree loaded.");
            return;
        }

        String filename = UIFileChooser.save("Save tree ");
        if(filename == null) return;

        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename));
            saveSubTree(theTree, pw);
            pw.close();

            UI.println("Tree saved to: " + filename);
        } catch (IOException e) {
            UI.println("Failed to save tree" + e);
        }
    }
    
    private void saveSubTree(DTNode node, PrintWriter pw) {
        if (node.isAnswer()) {
            pw.println("Answer: " + node.getText());
        } else {
            pw.println("Question: " + node.getText());
            saveSubTree(node.getYes(), pw);
            saveSubTree(node.getNo(), pw);
        }
    }

    public void drawTree() {
        UI.clearGraphics();

        if (theTree != null){
            //draw sub tree from root (centered at --,--)
            drawSubTree(theTree, 300, 50, 150);
        }
    }
   
    public void drawSubTree(DTNode node, double x, double y, double offset) {
        if (node == null) return;
        
        node.draw(x,y);

        if (!node.isAnswer()){
            double childY = y + 60; // vertical gap

            double yesX = x - offset; // yes offset
            double noX = x + offset; // no offset

            // yes child
            UI.drawLine(x, y + DTNode.HEIGHT / 2, yesX, childY - DTNode.HEIGHT / 2);
            drawSubTree(node.getYes(), yesX, childY, offset/2);

            // no child
            UI.drawLine(x, y + DTNode.HEIGHT / 2, noX, childY - DTNode.HEIGHT / 2);
            drawSubTree(node.getNo(), noX, childY, offset/2);
        }
    }
    
    // Written for you

    /** 
     * Loads a decision tree from a file.
     * Each line starts with either "Question:" or "Answer:" and is followed by the text
     * Calls a recursive method to load the tree and return the root node,
     *  and assigns this node to theTree.
     */
    public void loadTree (String filename) { 
        if (!Files.exists(Path.of(filename))){
            UI.println("No such file: "+filename);
            return;
        }
        try{theTree = loadSubTree(new ArrayDeque<String>(Files.readAllLines(Path.of(filename))));}
        catch(IOException e){UI.println("File reading failed: " + e);}
    }

    /**
     * Loads a tree (or subtree) from a Scanner and returns the root.
     * The first line has the text for the root node of the tree (or subtree)
     * It should make the node, and 
     *   if the first line starts with "Question:", it loads two subtrees (yes, and no)
     *    from the scanner and add them as the  children of the node,
     * Finally, it should return the  node.
     */
    public DTNode loadSubTree(Queue<String> lines){
        Scanner line = new Scanner(lines.poll());
        String type = line.next();
        String text = line.nextLine().trim();
        DTNode node = new DTNode(text);
        if (type.equals("Question:")){
            DTNode yesCh = loadSubTree(lines);
            DTNode noCh = loadSubTree(lines);
            node.setChildren(yesCh, noCh);
        }
        return node;
    }
}