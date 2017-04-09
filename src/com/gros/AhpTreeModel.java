package com.gros;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Created by gros on 09.04.17.
 */
public class AhpTreeModel extends DefaultTreeModel {
    AhpTreeModel(TreeNode root) {
        super(root);
    }

    AhpTreeModel(TreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        System.out.println(newValue);
        DefaultMutableTreeNode obj = (DefaultMutableTreeNode)path.getLastPathComponent();
        AhpNodeGraphic root = (AhpNodeGraphic)obj.getUserObject();
        root.name = newValue.toString();
        super.valueForPathChanged(path, root);
    }

}
