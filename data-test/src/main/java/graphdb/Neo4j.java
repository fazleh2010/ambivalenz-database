/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package graphdb;

import static core.Constants.OBJECT;
import static core.Constants.OBJECT_ID;
import static core.Constants.PROPERTY;
import core.Relation;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.driver.Result;
import org.neo4j.driver.Value;

import static org.neo4j.driver.Values.parameters;
import utils.FileFolderUtils;

public class Neo4j implements AutoCloseable {

    private final Driver driver;

    public Neo4j(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() {
        driver.close();
    }

    public void createNodeWithProperties(Entity entity) {
        String nodeType = entity.getNodeType();
        Map<String, String> properties = entity.getProperties();
        try (Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                String cypher = String.format("CREATE (n:%s) SET n += $props", nodeType);
                tx.run(cypher, parameters("props", properties));
                return null;
            });
            System.out.println("Node created with label: " + nodeType + " and properties: " + properties);
        }
    }

    public void deleteAll() {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (n) DETACH DELETE n");
                return null;
            });
            System.out.println("All nodes and relationships deleted.");
        }
    }

    public void listNodes() {
        try (Session session = driver.session()) {
            Result result = session.run("MATCH (n) RETURN n");

            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                Value nodeValue = record.get("n");
                Map<String, Object> properties = nodeValue.asNode().asMap();

                System.out.println("Node Label(s): " + nodeValue.asNode().labels());
                System.out.println("Properties: " + properties);
                System.out.println("------------");
            }
        }

    }

    public String findNode(String attribute, String value) {
        String valueName = null;
        try (Session session = driver.session()) {
            Result result = session.run("MATCH (n) RETURN n");

            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                Value nodeValue = record.get("n");
                System.out.println("Node Label(s): " + nodeValue.asNode().labels());
                System.out.println("------------");
                Map<String, Object> properties = nodeValue.asNode().asMap();
                System.out.println("Properties: " + properties);
                for (String propertyName : properties.keySet()) {
                    if (propertyName.contains(attribute)) {
                        valueName = (String) properties.get(propertyName);
                        return valueName;
                    }

                }

            }
        }
        return valueName;
    }

    public Boolean createRelationship(Entity entity) {
        String nodeTape1 = null, nodeType2 = null, objectID_1 = null, objectID_2 = null;
        Relation relation = entity.getRelation();
        if (relation.isRelationExisit()) {
            nodeTape1 = entity.getNodeType();
            objectID_1 = entity.getObjectID();
            objectID_2 = relation.getObject_id();
            System.out.println("nodeType: " + nodeTape1 + " entity_1:" + objectID_1 + " entity_2:" + objectID_2);
            /*nodeType2 = findNode(OBJECT_ID,objectID_2);
            if (nodeType2!=null) {
                createRelationship(nodeTape1, objectID_1, nodeType2, objectID_2,relation.getRelationName());
                return true;
            }*/
        }
        return false;

    }

    public void createRelationship(String node1, String name1, String node2, String name2, String relationshipName) {
        try (Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run(
                        "MATCH (a:" + node1 + " {" + Entity.OBJECT_ID + ": $" + name1 + "}), "
                        + "(b:" + node2 + " {" + Entity.OBJECT_ID + ": $" + name2 + "}) "
                        + "MERGE (a)-[r:" + relationshipName + "]->(b)",
                        parameters("name1", name1, "name2", name2)
                );
                return null;
            });
        }
    }

    /*
    public void createRelationship(String attributeName,String value1, String value2, String node1, String node2,String relationshipType) {
        try (Session session = driver.session()) {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run("MATCH (a:"+node1+" {"+attributeName+": $name1}), (b:"+node2+" {"+attributeName+": $name2}) "
                        + "MERGE (a)-[r:" + relationshipType + "]->(b)",
                        parameters("name1", value1, "name2", value2)
                );
                return null;
            });
        }
    }

     */
    public static void main(String[] args) {
        String dir = "dataset/german/input/"; // default CSV path
        List<String> files = FileFolderUtils.getSpecificFiles(dir, "entity", ".csv");
        Neo4j app = new Neo4j("bolt://localhost:7687", "neo4j", "password");
        HashMap<String, Entity> duplication = new HashMap<String, Entity>();

        try {
            for (String csvPath : files) {
                if (csvPath.contains(".~lock.")) {
                    continue;
                }
                FileReader reader = new FileReader(Paths.get(csvPath).toFile());
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
                LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();
                for (CSVRecord record : csvParser) {
                    Property property = new Property(record.get(PROPERTY), record.get(OBJECT));
                    properties.put(property.getProperty(), property.getObject());
                }
                Entity entity = new Entity(properties);
                duplication.put(entity.getSubject(), entity);
            }
            for (String subject : duplication.keySet()) {
                Entity entity = duplication.get(subject);
                System.out.println(entity);
                app.createNodeWithProperties(entity);
                app.createRelationship(entity);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
