package com.gros.gui;

import Jama.Matrix;
import com.gros.console.AhpNode;
import com.gros.console.AhpTree;
import com.gros.methods.PriorityVector;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gros on 10.04.17.
 */
class AhpParse extends JPanel {
    private JPanel weightsPanel;
    private AhpNodeGraphic node;
    private PriorityVector method;

    AhpParse(AhpNodeGraphic node, PriorityVector method) {
        super(new BorderLayout());
        this.node = node;
        this.method = method;

        this.weightsPanel = new JPanel(new GridLayout(0,2));
        prepareWeights();

        JScrollPane scrollWeights = new JScrollPane(weightsPanel);
        scrollWeights.setPreferredSize(new Dimension(500, 500));
        add(scrollWeights);
        createAndShowGUI();
    }

    private void prepareWeights() {
        Matrix weights = this.node.getWeightsVector(method);
        ArrayList<String> weightsNames = new ArrayList<>();
        for(AhpNode child : node.tree.getAlternativesRoot().getChilds())
            weightsNames.add(child.name);

        Map<String, Double> result = new HashMap<>();
        for(int i=0; i < weightsNames.size(); i++)
            result.put(weightsNames.get(i), weights.get(i, 0));
        result = AhpTree.sortByValue(result);


        for(Map.Entry<String, Double> entry : result.entrySet()) {
            JTextField label = new JTextField(entry.getKey());
            JTextField value = new JTextField(String.format("%.4f", entry.getValue()));
            label.setEditable(false);
            value.setEnabled(false);

            label.setFont(new Font(label.getFont().getName(), Font.BOLD,15));
            value.setFont(new Font(value.getFont().getName(), Font.BOLD,15));

            label.setHorizontalAlignment(JLabel.CENTER);
            value.setHorizontalAlignment(JLabel.CENTER);

            weightsPanel.add(label);
            weightsPanel.add(value);
        }
    }


    private void createAndShowGUI() {
        JFrame frame = new JFrame("AHP Results");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        AhpParse newContentPane = this;
        newContentPane.setOpaque(true); // content panes must be opaque
        frame.setContentPane(newContentPane);

        frame.setMinimumSize(new Dimension(500, 500));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
