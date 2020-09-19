package it.polimi.io.writer;

import it.polimi.domain.Problem;
import it.polimi.io.reader.ORLIBReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AMPLDataWriter {

    /*public static void main(String[] args) {
        // prepare orlinb instances
        String basePath = "instances/orlib/pmed%d.txt";
        String amplBaseFilePath = "models/baltest/%d/balanced-p-median.dat";
        try {
            for (int i=1; i<40; i++) {
                Problem problem = ORLIBReader.read(String.format(basePath, i));
                File solutionFile = new File(String.format(amplBaseFilePath, i));
                solutionFile.getParentFile().mkdirs();
                solutionFile.createNewFile();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(solutionFile));

            writer.write("data;\n\n");

            writer.write("param n := " + problem.getN() + ";\n\n");

            writer.write("param p := " + problem.getP() + ";\n\n");

            writer.write("param m := " + problem.getM() + ";\n\n");

            writer.write("param alpha := " + problem.getAlpha() + ";\n\n");

            writer.write("param r := \n");
            for (int i=0; i<problem.getR().length; i++)
                writer.write("\t" + i + "\t" + problem.getR()[i] + "\n");
            writer.write(";\n\n");

            writer.write("param d := \n");
            for (int i=0; i<problem.getD().length; i++)
                writer.write("\t" + i + "\t" + problem.getD()[i] + "\n");
            writer.write(";\n\n");

            writer.write("param c := \n");
            float[][] c = problem.getC();
            for (int i=0; i<c.length; i++)
                for (int j=0; j<c[i].length; j++)
                    writer.write("\t" + i + "\t" + j + "\t" + c[i][j] + "\n");
            writer.write(";\n\n");

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    }*/

    public static void write(String filepath, Problem problem) {
        try {
            File solutionFile = new File(filepath);
            solutionFile.getParentFile().mkdirs();
            solutionFile.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(solutionFile));

            writer.write("data;\n\n");

            writer.write("param n := " + problem.getN() + ";\n\n");

            writer.write("param p := " + problem.getP() + ";\n\n");

            writer.write("param m := " + problem.getM() + ";\n\n");

            writer.write("param alpha := " + problem.getAlpha() + ";\n\n");

            writer.write("param r := \n");
            for (int i=0; i<problem.getR().length; i++)
                writer.write("\t" + i + "\t" + problem.getR()[i] + "\n");
            writer.write(";\n\n");

            writer.write("param d := \n");
            for (int i=0; i<problem.getD().length; i++)
                writer.write("\t" + i + "\t" + problem.getD()[i] + "\n");
            writer.write(";\n\n");

            writer.write("param c := \n");
            float[][] c = problem.getC();
            for (int i=0; i<c.length; i++)
                for (int j=0; j<c[i].length; j++)
                    writer.write("\t" + i + "\t" + j + "\t" + c[i][j] + "\n");
            writer.write(";\n\n");

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
