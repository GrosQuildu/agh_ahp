package com.gros;

import Jama.Matrix;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    private JPanel weightsPanelThis;
    private JPanel rightPanel;

    private JFrame mainFrame;

    public AhpEditNodeGraphic(AhpNodeGraphic currentNode, JFrame mainFrame) {
        super(new BorderLayout());
        this.mainFrame = mainFrame;
        this.node = currentNode;
        this.method = node.tree.method;

        /** Main questions panel **/
        questionPanel = new JPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
        prepareQuestions();
        JScrollPane scrollPane = new JScrollPane(questionPanel);
        scrollPane.setPreferredSize(new Dimension(500, 700));
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

        /** Right weights this panel **/
        weightsPanelThis = new JPanel(new SpringLayout());
        weightsPanelThis.setAlignmentX(Component.RIGHT_ALIGNMENT);
        prepareWeightsThis();

        JScrollPane scrollWeightsThis = new JScrollPane(weightsPanelThis);
        scrollWeightsThis.setPreferredSize(new Dimension(300, 100));

        JScrollPane scrollWeights = null;
        if(node.list.size() != 0) {
            /** Right weights panel **/
            weightsPanel = new JPanel(new SpringLayout());
            weightsPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
            prepareWeights();

            scrollWeights = new JScrollPane(weightsPanel);
            scrollWeights.setPreferredSize(new Dimension(300, 100));
        }

        /** Right panel **/
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(scrollInfo);
        rightPanel.add(scrollWeightsThis);
        if(scrollWeights != null)
            rightPanel.add(scrollWeights);

        JScrollPane scrollPaneRight = new JScrollPane(rightPanel);
        scrollPane.setPreferredSize(new Dimension(500, 700));
        add(scrollPaneRight, BorderLayout.EAST);

        createAndShowGUI();
    }

    static void prepareWeights(JPanel panel, Map<String, Double> result) {
        ArrayList<JLabel> labels = new ArrayList<>();
        ArrayList<JTextField> values = new ArrayList<>();
        for(Map.Entry<String, Double> entry : result.entrySet()) {
            labels.add(new JLabel(entry.getKey()));
            values.add(new JTextField(String.format("%.4f", entry.getValue())));
        }

        for (int i = 0; i < labels.size(); i++) {
            JLabel l = labels.get(i);
            values.get(i).setEditable(false);
            values.get(i).setMaximumSize( values.get(i).getPreferredSize() );
            l.setLabelFor(values.get(i));
            panel.add(l);
            panel.add(values.get(i));
        }
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
        prepareWeights(weightsPanel, result);

        SpringUtilities.makeCompactGrid(weightsPanel,
                result.size(), 2,
                20, 20,
                10, 10);
    }

    private void prepareWeightsThis() {
        JLabel methodLabel = new JLabel("Current method: ");
        JTextField methodValue = new JTextField(method.toString());
        methodValue.setEditable(false);
        methodValue.setMaximumSize( methodValue.getPreferredSize() );
        methodLabel.setLabelFor(methodValue);
        weightsPanelThis.add(methodLabel);
        weightsPanelThis.add(methodValue);

        Matrix weightsThis = method.getPriorityVector(this.node);
        ArrayList<String> weightsThisNames = new ArrayList<>();
        for(AhpNode child : this.node.getChilds())
            weightsThisNames.add(child.name);

        Map<String, Double> result = new HashMap<>();
        for(int i=0; i < weightsThisNames.size(); i++)
            result.put(weightsThisNames.get(i), weightsThis.get(i, 0));
        result = AhpTree.sortByValue(result);
        prepareWeights(weightsPanelThis, result);

        SpringUtilities.makeCompactGrid(weightsPanelThis,
                result.size()+1, 2,
                20, 20,
                10, 10);
    }

    private void prepareConsistencies(GroupLayout layout) {
        consistencies = new ArrayList<>();

        JLabel IndexLabel = new JLabel("Index: ");
        JLabel RatioLabel = new JLabel("Ratio: ");
        JLabel IndexOfDeterminantsLabel = new JLabel("Index Of Determinants: ");
        JLabel GeometricIndexLabel = new JLabel("Geometric Index: ");
        JLabel HarmonicIndexLabel = new JLabel("Harmonic Index: ");


        JTextField IndexTextField = new JTextField(String.format("%.4f", node.consistencyIndex()));
        IndexTextField.setEditable(false);
        consistencies.add(IndexTextField);
        JTextField RatioTextField = new JTextField(String.format("%.4f", node.consistencyRatio()));
        RatioTextField.setEditable(false);
        consistencies.add(RatioTextField);
        JTextField IndexOfDeterminantsTextField = new JTextField(String.format("%.4f", node.consistencyIndexOfDeterminants()));
        IndexOfDeterminantsTextField.setEditable(false);
        consistencies.add(IndexOfDeterminantsTextField);
        JTextField GeometricIndexTextField = new JTextField(String.format("%.4f", node.consistencyGeometricIndex()));
        GeometricIndexTextField.setEditable(false);
        consistencies.add(GeometricIndexTextField);
        JTextField HarmonicIndexTextField = new JTextField(String.format("%.4f", node.consistencyHarmonicIndex()));
        HarmonicIndexTextField.setEditable(false);
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
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        AhpEditNodeGraphic newContentPane = this;
        newContentPane.setOpaque(true); // content panes must be opaque
        frame.setContentPane(newContentPane);

        frame.pack();
        frame.setLocationRelativeTo(this.getParent());
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                double checkConsistencynode = node.consistencyRatio();
                if(checkConsistencynode <= 0.1) {
                    mainFrame.setEnabled(true);
                    frame.dispose();
                } else
                    JOptionPane.showMessageDialog(frame,
                            "Matrix is too inconsistent, max ratio is 0.1",
                            "Consistency error",
                            JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}