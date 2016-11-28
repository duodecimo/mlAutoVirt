/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duo.ml.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

/**
 *
 * @author duo
 */
public class NeuralNetwork {

    private final RealMatrix theta1Transpose;
    private final RealMatrix theta2Transpose;

    public NeuralNetwork(String theta1FileName, String theta2FileName) {
        this.theta1Transpose = loadMatrixFromOctaveDatFile(theta1FileName).transpose();
        this.theta2Transpose = loadMatrixFromOctaveDatFile(theta2FileName).transpose();
    }

    public double[] predict(byte[] rawBytes) {
        double[] xs = new double[rawBytes.length + 1];
        xs[0] = 1.0;
        for (int i = 0; i < rawBytes.length; i++) {
            xs[i + 1] = rawBytes[i] < 0 ? 256 + (double) rawBytes[i]
                    : (double) rawBytes[i];
        }

        RealMatrix x = new Array2DRowRealMatrix(1, rawBytes.length + 1);
        x.setRow(0, xs);

        RealMatrix h1 = sigmoidAddOnes(x.multiply(theta1Transpose));
        RealMatrix h2 = sigmoid(h1.multiply(theta2Transpose));
        double[] out = new double[h2.getColumnDimension()];
        for (int z = 0; z < h2.getColumnDimension(); z++) {
            out[z] = h2.getEntry(0, z);
        }
        return out;
    }

    RealMatrix sigmoid(RealMatrix z) {
        // g = 1.0 ./ (1.0 + exp(-z));
        RealMatrix m = z.copy();
        for (int i = 0; i < m.getRowDimension(); i++) {
            for (int j = 0; j < m.getColumnDimension(); j++) {
                double y = m.getEntry(i, j);
                double g = 1.0 / (1.0 + Math.exp(-y));
                m.setEntry(i, j, g);
            }
        }
        return m;
    }

    RealMatrix sigmoidAddOnes(RealMatrix z) {
        // g = 1.0 ./ (1.0 + exp(-z));
        RealMatrix m = new Array2DRowRealMatrix(z.getRowDimension(),
                z.getColumnDimension() + 1);
        for (int i = 0; i < m.getRowDimension(); i++) {
            for (int j = 0; j < m.getColumnDimension(); j++) {
                double g = 1.0;
                if (j != 0) {
                    double y = z.getEntry(i, j - 1);
                    g = 1.0 / (1.0 + Math.exp(-y));
                }
                m.setEntry(i, j, g);
            }
        }
        return m;
    }

    public static RealMatrix loadMatrixFromOctaveDatFile(String filename) {
        /*
         * Example file format:
         * # Created by Octave 4.0.0, Sun Nov 27 18:41:18 2016 BRST <duo@elijah>
         * # name: Theta1
         * # type: matrix
         * # rows: 64
         * # columns: 577
         *  -0.0180098003404383 3.607094817052599e-05 3.607091403535862e-05 ... 
         */
        RealMatrix realMatrix = null;
        LineNumberReader lineNumberReader = null;
        try {
            FileReader fileReader = new FileReader(filename);
            lineNumberReader = new LineNumberReader(fileReader);
            int rows = -1;
            int cols = -1;
            boolean created = false;
            String line;
            int rowsCounter = 0;
            int columnsCounter = 0;
            while (true) {
                line = lineNumberReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("#")) {
                    // header line
                    String[] tokens = line.split(" ");
                    switch (tokens[1]) {
                        case "rows:":
                            rows = Integer.parseInt(tokens[2]);
                            break;
                        case "columns:":
                            cols = Integer.parseInt(tokens[2]);
                            break;
                    }
                } else {
                    // data beginning
                    if (!created && rows > 0 && cols > 0) {
                        realMatrix = new Array2DRowRealMatrix(rows, cols);
                        created = true;
                    } else {
                        System.err.println("Unexpected non-header read at line "
                                + lineNumberReader.getLineNumber() + ":" + filename);
                        throw new IOException("Invalid file format");
                    }
                    String[] tokens = line.split(" ");
                    for (String token : tokens) {
                        if (token.equals("")) {
                            continue;
                        }
                        double d = Double.parseDouble(token);
                        realMatrix.setEntry(rowsCounter, columnsCounter, d);
                        columnsCounter++;
                    }
                    rowsCounter++;
                    columnsCounter = 0;
                }
            }

        } catch (FileNotFoundException e) {
            System.err.println(e.toString());
        } catch (IOException e) {
            System.err.println(e.toString());
        } catch (NumberFormatException e) {
            if(lineNumberReader!=null) {
            System.err.println("NumberFormatException reading line "
                    + lineNumberReader.getLineNumber() + " of " + filename);
            }
            System.err.println("NumberFormatException reading unknown line ");
            System.err.println(e.toString());
        }
        return realMatrix;
    }
}
