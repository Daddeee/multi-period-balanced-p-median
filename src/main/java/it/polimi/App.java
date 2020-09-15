package it.polimi;

import it.polimi.domain.Problem;
import it.polimi.io.reader.RandReader;
import it.polimi.io.writer.AMPLDataWriter;

public class App {
    public static void main( String[] args ) {
        Problem p = RandReader.read("instances/n100p5t2.csv");
        AMPLDataWriter.write("models/test/multi-period-balanced-p-median.dat", p);
    }
}
