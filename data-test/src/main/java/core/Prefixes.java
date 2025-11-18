/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package core;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author elahi
 */
public interface Prefixes {
    public static String ENTITY="entity";
    public static String PROPERTY="property";
    public static String OBJRCT="object";
    public static String GND="GND:";
    public static String DNB="DNB:";
    public static String baseUri="http://localhost:9999/digitalEnvornment/";
    public static Map<String, String> prefixes=new HashMap<>();
}
