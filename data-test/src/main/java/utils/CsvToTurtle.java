/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import core.Prefixes;
import core.Tripple;
import java.io.File;
import java.util.List;
import utils.CsvFile;
import utils.FileFolderUtils;

/**
 *
 * @author elahi
 */
public class CsvToTurtle {

    public CsvToTurtle(File inputFile, File outputFile) {
        CsvFile csvFile = new CsvFile(inputFile);
        List<String[]> rows = csvFile.getRows(inputFile);
        String str = "";
        Integer index = 0;
        for (String[] row : rows) {
            if (index == 0) {
                index = index + 1;
                continue;
            }

            Tripple tripple = new Tripple(row);
            //System.out.println(tripple.getSubject() + ".." + tripple.getProperty() + ".." + tripple.getObject() + ".." + tripple.getDatatype());
            //System.out.println(tripple.getTripleElement());
            String line = tripple.getTripleElement() + "\n";
            str += line;
        }
        FileFolderUtils.stringToFile(str, outputFile);
        System.out.println(str);

    }

    public static void main(String[] args) throws Exception {
        String entityDir = "dataset/english/";
        String entityFileType = ".csv";
        List<String> files = FileFolderUtils.getSpecificFiles(entityDir, Prefixes.ENTITY, entityFileType);
        for (String inputFileString : files) {
            File inputFile=new File(entityDir+inputFileString);
            File outputFile = new File(entityDir + "output_" + inputFile.getName().replace(".csv", ".ttl"));
            CsvToTurtle csvToTurtle = new CsvToTurtle(inputFile, outputFile);
        }
        //File inputFile=new File("dataset/english/entity_1.csv");

    }

}
