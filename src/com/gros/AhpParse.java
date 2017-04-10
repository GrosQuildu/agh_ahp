package com.gros;

import Jama.Matrix;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gros on 10.04.17.
 */
public class AhpParse extends JPanel {
    private JPanel weightsPanel;
    private AhpNodeGraphic node;
    PriorityVectorMethod method;

    AhpParse(AhpNodeGraphic node, PriorityVectorMethod method) {
        super(new BorderLayout());
        this.node = node;
        this.method = method;

        weightsPanel = new JPanel(new SpringLayout());
        weightsPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
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
        AhpEditNodeGraphic.prepareWeights(weightsPanel, result);

        SpringUtilities.makeCompactGrid(weightsPanel,
                result.size(), 2,
                20, 20,
                10, 10);
    }


    private void createAndShowGUI() {
        JFrame frame = new JFrame("AHP Results");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        AhpParse newContentPane = this;
        newContentPane.setOpaque(true); // content panes must be opaque
        frame.setContentPane(newContentPane);

        frame.pack();
        frame.setVisible(true);
    }
}
