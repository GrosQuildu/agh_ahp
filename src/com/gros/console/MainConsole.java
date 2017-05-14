package com.gros.console;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import com.gros.Utils;
import org.json.JSONException;


/**
 * Created by gros on 10.03.17.
 */
public class MainConsole {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        int choice;
        while(true) {
            printMenu();
            choice = s.nextInt();
            try {
                switch (choice) {
                    case 1:
                        createRandomSample();
                        break;
                    case 2:
                        readSample();
                        break;
                    case 3:
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void printMenu() {
        System.out.println("Menu:");
        System.out.println("1) create random sample");
        System.out.println("2) parse xml");
        System.out.println("3) exit");
        System.out.print("> ");
    }

    private static void readSample() throws Exception {
        Scanner s = new Scanner(System.in);

        System.out.print("Path to XML file: ");
        String path = s.next();

        System.out.print("Method (");
        for(String method : AhpTree.implementedMethods)
            System.out.print(method+"/");
        System.out.println("): ");
        s.nextLine();
        String method = s.nextLine();
        if("".equals(method))
            method = "eigenvector";

        AhpTree tree = AhpTree.fromXml(path, method);
        System.out.println(tree);
        tree.printResult();
    }

    private static void createRandomSample() throws JSONException, FileNotFoundException{
        Scanner s = new Scanner(System.in);

        System.out.print("Number of alternatives: ");
        int alternatives = s.nextInt();

        System.out.print("(Sub)criterions deep: ");
        int criterionsDeep = s.nextInt();

        System.out.print("Max criterions on one level: ");
        int maxCriterions = s.nextInt();

        System.out.print("Inconsistency (0 or int in <1;20>): ");
        int inconsistency = s.nextInt();

        AhpNode goal = Utils.randomMatrices(alternatives, criterionsDeep, maxCriterions, inconsistency);
        ArrayList<String> altList = new ArrayList<>();
        for(int i=0; i < alternatives; i++)
            altList.add("Alt"+i);
        AhpTree tree = new AhpTree(goal, altList, "eigenvector");

        System.out.print("Path to save: ");
        String path = s.next();

        try(PrintWriter out = new PrintWriter(path)) {
            out.println(tree.toXml());
        }
        System.out.println("Saved\n");
    }


}
