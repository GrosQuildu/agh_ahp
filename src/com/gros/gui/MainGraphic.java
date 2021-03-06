package com.gros.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class MainGraphic extends JPanel implements ActionListener {
    private int newNodeSuffix = 1;
    private static String ADD_COMMAND = "add";
    private static String REMOVE_COMMAND = "remove";
    private static String CLEAR_COMMAND = "clear";
    private static String EDIT_COMMAND = "edit";
    private static String PARSE_COMMAND = "parse";
    private static String LOAD_COMMAND = "load_xml";
    private static String SAVE_COMMAND = "save_xml";
    private static String METHOD_COMMAND = "change_method";
    private static String REQ_COMMAND = "change_req";

    private JFileChooser fc;
    private AhpTreeGraphic treePanel;
    private JFrame mainFrame;

    public MainGraphic(JFrame mainFrame) {
        super(new BorderLayout());
        this.mainFrame = mainFrame;

        fc = new JFileChooser();
        File workingDirectory = new File(System.getProperty("user.dir"));
        fc.setCurrentDirectory(workingDirectory);
        treePanel = new AhpTreeGraphic();
        prepareTree();

        JButton addButton = new JButton("Add");
        addButton.setActionCommand(ADD_COMMAND);
        addButton.addActionListener(this);

        JButton removeButton = new JButton("Remove");
        removeButton.setActionCommand(REMOVE_COMMAND);
        removeButton.addActionListener(this);

        JButton editButton = new JButton("Edit");
        editButton.setActionCommand(EDIT_COMMAND);
        editButton.addActionListener(this);

        JButton parseButton = new JButton("Parse");
        parseButton.setActionCommand(PARSE_COMMAND);
        parseButton.addActionListener(this);

        JButton loadButton = new JButton("Load XML");
        loadButton.setActionCommand(LOAD_COMMAND);
        loadButton.addActionListener(this);

        JButton saveButton = new JButton("Save XML");
        saveButton.setActionCommand(SAVE_COMMAND);
        saveButton.addActionListener(this);

        JButton methodButton = new JButton("Change method");
        methodButton.setActionCommand(METHOD_COMMAND);
        methodButton.addActionListener(this);

        JButton reqButton = new JButton("Change cons. requirement");
        reqButton.setActionCommand(REQ_COMMAND);
        reqButton.addActionListener(this);

        JButton clearButton = new JButton("Clear");
        clearButton.setActionCommand(CLEAR_COMMAND);
        clearButton.addActionListener(this);

        // Lay everything out.
        treePanel.setPreferredSize(new Dimension(400, 500));
        add(treePanel, BorderLayout.CENTER);

        JPanel panel = new JPanel(new GridLayout(9, 0));
        panel.add(addButton);
        panel.add(removeButton);
        panel.add(editButton);
        panel.add(parseButton);
        panel.add(loadButton);
        panel.add(saveButton);
        panel.add(methodButton);
        panel.add(reqButton);
        panel.add(clearButton);
        add(panel, BorderLayout.EAST);
    }

    public void prepareTree() {
        AhpNodeGraphic criterions = new AhpNodeGraphic("Criterions");
        AhpNodeGraphic alternatives = new AhpNodeGraphic("Alternatives", true);
        criterions.isRoot = true;
        alternatives.isRoot = true;

        AhpNodeGraphic crit1 = new AhpNodeGraphic("Criterion1");
        AhpNodeGraphic crit2 = new AhpNodeGraphic("Criterion2");

        AhpNodeGraphic alt1 = new AhpNodeGraphic("Alt1", true);
        AhpNodeGraphic alt2 = new AhpNodeGraphic("Alt2", true);
        AhpNodeGraphic alt3 = new AhpNodeGraphic("Alt3", true);
        AhpNodeGraphic alt4 = new AhpNodeGraphic("Alt4", true);

        DefaultMutableTreeNode p1, p2;
        p1 = treePanel.addObject(treePanel.getRootNode(), criterions);
        p2 = treePanel.addObject(treePanel.getRootNode(), alternatives);

        treePanel.addObject(p1, crit1);
        treePanel.addObject(p1, crit2);

        treePanel.addObject(p2, alt1);
        treePanel.addObject(p2, alt2);
        treePanel.addObject(p2, alt3);
        treePanel.addObject(p2, alt4);

        criterions.setMatrix();
        alternatives.setMatrix();
        crit1.setMatrix();
        crit2.setMatrix();
        alt1.setMatrix();
        alt2.setMatrix();
        alt3.setMatrix();
        alt4.setMatrix();
    }


    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (ADD_COMMAND.equals(command)) {
            treePanel.addObject(new AhpNodeGraphic("New Node " + newNodeSuffix++));
        }
        else if (REMOVE_COMMAND.equals(command)) {
            treePanel.removeCurrentNode();
        }
        else if (EDIT_COMMAND.equals(command)) {
            treePanel.edit(mainFrame);
        }
        else if (PARSE_COMMAND.equals(command)) {
            treePanel.parse();
        }
        else if (CLEAR_COMMAND.equals(command)) {
            treePanel.clear();
        }
        else if (SAVE_COMMAND.equals(command)) {
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                treePanel.save(file.getAbsolutePath());
            }
        }
        else if (LOAD_COMMAND.equals(command)) {
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                treePanel.load(file.getAbsolutePath());
            }
        }
        else if (METHOD_COMMAND.equals(command)) {
            treePanel.changeMethod();
        }
        else if (REQ_COMMAND.equals(command)) {
            treePanel.changeRequirement();
        }
    }

    /**
     * Create the GUI and show it. For thread safety, this method should be
     * invoked from the event-dispatching thread.
     */
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("AHP");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MainGraphic newContentPane = new MainGraphic(frame);
        newContentPane.setOpaque(true); // content panes must be opaque
        frame.setContentPane(newContentPane);

        frame.setMinimumSize(new Dimension(500, 500));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(MainGraphic::createAndShowGUI);
    }
}