package com.gros;

import Jama.Matrix;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gros on 09.04.17.
 */
public class AhpEditNodeGraphic extends JPanel {
    AhpNodeGraphic node;
    ArrayList<JTextArea> inputs;
    ArrayList<JTextField> consistencies;

    PriorityVectorMethod method;

    private JPanel consistencyPanel;
    private JPanel questionPanel;
    private JPanel weightsPanel;
    private JPanel rightPanel;

    public AhpEditNodeGraphic(AhpNodeGraphic currentNode) {
        super(new BorderLayout());
        this.node = currentNode;
        setMethod("eigenvector");

        /** Main questions panel **/
        questionPanel = new JPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
        prepareQuestions();
        JScrollPane scrollPane = new JScrollPane(questionPanel);
        scrollPane.setPreferredSize(new Dimension(400, 700));
        add(scrollPane);

        /** Right consistency panel **/
        consistencyPanel = new JPanel();
        GroupLayout layout = new GroupLayout(consistencyPanel);
        consistencyPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        prepareConsistencies(layout);

        JScrollPane scrollInfo = new JScrollPane(consistencyPanel);
        scrollInfo.setPreferredSize(new Dimension(300, 100));

        /** Right weights panel **/
        weightsPanel = new JPanel(new SpringLayout());
        prepareWeights();

        JScrollPane scrollWeights = new JScrollPane(weightsPanel);
        scrollWeights.setPreferredSize(new Dimension(300, 100));

        /** Right panel **/
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(scrollInfo);
        rightPanel.add(scrollWeights);
        JScrollPane scrollPaneRight = new JScrollPane(rightPanel);
        scrollPane.setPreferredSize(new Dimension(200, 300));
        add(scrollPaneRight, BorderLayout.EAST);

        createAndShowGUI();
    }

    private void setMethod(String method) {
        if("eigenvector".equals(method))
            this.method = new EigenvectorMethod();
        else
            this.method = new GeometricMeanMethod();
    }

    private void prepareWeights() {
        Matrix weightsThis = method.getPriorityVector(this.node);
        ArrayList<String> weightsThisNames = new ArrayList<>();
        for(AhpNode child : this.node.getChilds())
            weightsThisNames.add(child.name);

        Map<String, Double> result = new HashMap<String, Double>();
        for(int i=0; i < weightsThisNames.size(); i++)
            result.put(weightsThisNames.get(i), weightsThis.get(i, 0));
        result = AhpTree.sortByValue(result);


        ArrayList<JLabel> labels = new ArrayList<>();
        ArrayList<JTextField> values = new ArrayList<>();
        for(Map.Entry<String, Double> entry : result.entrySet()) {
            labels.add(new JLabel(entry.getKey()));
            values.add(new JTextField(String.format("%.4f", entry.getValue())));
        }

        for (int i = 0; i < labels.size(); i++) {
            JLabel l = labels.get(i);
            weightsPanel.add(l);
            l.setLabelFor(values.get(i));
            weightsPanel.add(values.get(i));
        }

        SpringUtilities.makeCompactGrid(weightsPanel,
                labels.size(), 2,
                10, 10,
                2, 2);


//        Matrix weights = this.node.getWeightsVector(method);
    }

    private void prepareConsistencies(GroupLayout layout) {
        consistencies = new ArrayList<>();

        JLabel IndexLabel = new JLabel("Index: ");
        JLabel RatioLabel = new JLabel("Ratio: ");
        JLabel IndexOfDeterminantsLabel = new JLabel("Index Of Determinants: ");
        JLabel GeometricIndexLabel = new JLabel("Geometric Index: ");
        JLabel HarmonicIndexLabel = new JLabel("Harmonic Index: ");


        JTextField IndexTextField = new JTextField(String.format("%.4f", node.consistencyIndex()));
        consistencies.add(IndexTextField);
        JTextField RatioTextField = new JTextField(String.format("%.4f", node.consistencyRatio()));
        consistencies.add(RatioTextField);
        JTextField IndexOfDeterminantsTextField = new JTextField(String.format("%.4f", node.consistencyIndexOfDeterminants()));
        consistencies.add(IndexOfDeterminantsTextField);
        JTextField GeometricIndexTextField = new JTextField(String.format("%.4f", node.consistencyGeometricIndex()));
        consistencies.add(GeometricIndexTextField);
        JTextField HarmonicIndexTextField = new JTextField(String.format("%.4f", node.consistencyHarmonicIndex()));
        consistencies.add(HarmonicIndexTextField);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(IndexLabel)
                        .addComponent(RatioLabel)
                        .addComponent(IndexOfDeterminantsLabel)
                        .addComponent(GeometricIndexLabel)
                        .addComponent(HarmonicIndexLabel))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(IndexTextField)
                        .addComponent(RatioTextField)
                        .addComponent(IndexOfDeterminantsTextField)
                        .addComponent(GeometricIndexTextField)
                        .addComponent(HarmonicIndexTextField))
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(IndexLabel)
                        .addComponent(IndexTextField))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(RatioLabel)
                        .addComponent(RatioTextField))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(IndexOfDeterminantsLabel)
                        .addComponent(IndexOfDeterminantsTextField))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(GeometricIndexLabel)
                        .addComponent(GeometricIndexTextField))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(HarmonicIndexLabel)
                        .addComponent(HarmonicIndexTextField))
        );
    }

    private void prepareQuestions() {
        node.setMatrix();
        ArrayList<AhpNode> childs = node.getChilds();
        inputs = new ArrayList<>();

        for(int i=0; i<childs.size(); i++)
            for(int j=0; j<childs.size(); j++) {
                if(i == j)
                    continue;
                AhpNode first = childs.get(i);
                AhpNode second = childs.get(j);
                JTextField textField = new JTextField("Compare \""+first.name+"\" with \""+second.name+"\"");
                textField.setEditable(false);

                JTextArea editTextArea = new JTextArea(String.format("%.4f",node.matrix.get(i,j)));
                editTextArea.setBorder(BorderFactory.createCompoundBorder(null,
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
                inputs.add(editTextArea);

                JPanel groupPanel = new JPanel(new GridLayout(0,1));
                groupPanel.add(textField);
                groupPanel.add(editTextArea);
                questionPanel.add(groupPanel);

                editTextArea.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateMatrix();
                        node.updateEigen();
                        updateConsistencies();
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateMatrix();
                        node.updateEigen();
                        updateConsistencies();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateMatrix();
                        node.updateEigen();
                        updateConsistencies();
                    }

                    private void updateConsistencies() {
                        consistencies.get(0).setText(String.format("%.4f", node.consistencyIndex()));
                        consistencies.get(1).setText(String.format("%.4f", node.consistencyRatio()));
                        consistencies.get(2).setText(String.format("%.4f", node.consistencyIndexOfDeterminants()));
                        consistencies.get(3).setText(String.format("%.4f", node.consistencyGeometricIndex()));
                        consistencies.get(4).setText(String.format("%.4f", node.consistencyHarmonicIndex()));
                    }

                    private void updateMatrix() {
                        int row = -1, col = -1, counter = 0;
                        boolean founPosition = false;
                        for(int i=0; !founPosition && i<childs.size(); i++)
                            for(int j=0; !founPosition && j<childs.size(); j++) {
                                if (i == j)
                                    continue;
                                if(editTextArea == inputs.get(counter)) {
                                    row = i;
                                    col = j;
                                    founPosition = true;
                                }
                                counter++;
                            }

                        if(row != -1 && !editTextArea.getText().isEmpty()) {
                            try {
                                double value = Double.parseDouble(editTextArea.getText().replace(',','.'));
                                node.matrix.set(row, col, value);
                                System.out.println("New matrix:");
                                node.matrix.print(10,10);
                            } catch (Exception e) {}
                        }
                    }
                });
            }
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Edit node");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        AhpEditNodeGraphic newContentPane = this;
        newContentPane.setOpaque(true); // content panes must be opaque
        frame.setContentPane(newContentPane);

        frame.pack();
        frame.setVisible(true);
    }

    public void run() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}

class AhpEditNodeQuestionsGraphic extends JPanel {

}