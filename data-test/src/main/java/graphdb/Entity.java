/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package graphdb;

import core.Constants;
import core.Relation;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.*;
import org.apache.commons.csv.*;

/**
 *
 * @author melahi
 */
public final class Entity implements Constants {

    private static Integer INDEX = 1;
    private String object_id = null;
    private String subject = null;
    private String nodeType = null;
    private Relation relation = new Relation();
    private LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();

    public Entity() {
    }

    public Entity(LinkedHashMap<String, String> propertiesT) {
        this.properties = propertiesT;
        //System.out.println(this.subject+" "+this.nodeType+" "+this.object_id);
        //System.out.println( this.properties.keySet());
        this.findSubjectAndType(this.properties);
        this.relation = new Relation(this.object_id ,this.properties);
        this.properties.put(NAME, this.subject);
        this.properties.put(NODE_TYPE, this.nodeType);
        //this.properties.put(OBJECT_ID, this.object_id);
    }

    private void findSubjectAndType(LinkedHashMap<String, String> properties) {
        String id = null;
        for (String property : properties.keySet()) {
            String object = properties.get(property);
            if (property.contains(TITEL)) {
                this.subject = object;
            } else if (property.contains(NODE_TYPE)) {
                this.nodeType = object;
                //Integer number = INDEX + 1;
                //this.object_id = object + "_" + number.toString();
                //INDEX = number;

            } else if (property.contains(Entity.OBJECT_ID)) {
                this.object_id = object;
            }
           
        }

    }

    public String getObjectID() {
        return this.object_id;
    }

    public String getNodeType() {
        return nodeType;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getSubject() {
        return subject;
    }

    public Relation getRelation() {
        return relation;
    }

    @Override
    public String toString() {
        return "Entity{" + "Property=" + PROPERTY + ", Object=" + OBJECT + ", name=" + NAME + ", subject=" + subject + ", " + relation + ", properties=" + properties + '}';
    }

}
