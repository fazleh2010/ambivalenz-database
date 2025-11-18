package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.*;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author elahi
 *
 */
public class FileFolderUtils {

    public static void stringToFile(String str, File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(str);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(FileFolderUtils.class.getName()).log(Level.SEVERE, null, ex);

        }

    }
    
     public static List<String> getSpecificFiles(String propertyInputDir, String fileType,String fileExtension) {
        List<String> properties = new ArrayList<>();
        File file = new File(propertyInputDir);
        String[] propertyFiles = file.list();

        for (String propertyFile : propertyFiles) {
            if (propertyFile.contains(fileType)&&propertyFile.contains(fileExtension)) {
                properties.add(propertyInputDir+propertyFile);
            }
        }
        return properties;

    }

    public static void stringToFile(String content, String fileName)
            throws IOException {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(content);
        writer.close();

    }

    public static String fileToString(String fileName) {
        InputStream is;
        String fileAsString = null;
        try {
            is = new FileInputStream(fileName);
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line).append("\n");
                line = buf.readLine();
            }
            fileAsString = sb.toString();
            //System.out.println("Contents : " + fileAsString);
        } catch (Exception ex) {
            Logger.getLogger(FileFolderUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return fileAsString;
    }

}
