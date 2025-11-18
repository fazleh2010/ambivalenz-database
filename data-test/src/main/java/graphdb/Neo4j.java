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
import static java.lang.System.exit;
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
// Import Neo4j Driver classes
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

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


   // Method to find nodeType by Objekt-ID
    public String findNodeTypeByObjectId(String Objekt_ID, String objectId) {
        try (Session session = driver.session()) {
            return session.readTransaction(tx -> {
                String cypher =
                    "MATCH (n { `"+Objekt_ID+"`: $objectId }) " +
                    "RETURN n.nodeType AS nodeType";

                Result result = tx.run(cypher, Map.of("objectId", objectId));

                if (result.hasNext()) {
                    Record record = result.next();
                    return record.get("nodeType").asString();
                } else {
                    return null;
                }
            });
        }
    }

    public Boolean createRelationship(Entity entity) {
        String nodeType1 = null, nodeType2 = null, objectID_1 = null, objectID_2 = null;
        Relation relation = entity.getRelation();
        if (relation.isRelationExisit()) {
            objectID_1 = relation.getObject_ID_1();
            objectID_2 = relation.getObject_ID_2();
            nodeType1 = findNodeTypeByObjectId(OBJECT_ID,objectID_1);
            nodeType2 = findNodeTypeByObjectId(OBJECT_ID,objectID_2);
             System.out.println("nodeType1: " + nodeType1 + " objectID_1:" + objectID_1 +"  nodeType2: " + nodeType1 + " objectID_2:" + objectID_2);
             createRelationship(nodeType1, // label of first node
                    objectID_1, // Objekt-ID of first node
                    relation.getRelationName(),
                    nodeType2, // label of second node
                    objectID_2 // Objekt-ID of second node
            );
            /*createRelationship("Painting", // label of first node
                    "Object1", // Objekt-ID of first node
                    relation.getRelationName(),
                    "Person", // label of second node
                    "Object2" // Objekt-ID of second node
            );*/

            return true;
        }
        return false;

    }

    public void createRelationship(
            String label1, // label of Object1 (e.g. Painting or Person)
            String objectId1, // Objekt-ID of Object1
            String relationName,
            String label2, // label of Object2
            String objectId2 // Objekt-ID of Object2
    ) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {

                // IMPORTANT: labels CANNOT be passed as parameters.
                // They must be injected directly into the Cypher string.
                String cypher
                        = "MATCH (a:" + label1 + " { `Objekt-ID`: $id1 }) "
                        + "MATCH (b:" + label2 + " { `Objekt-ID`: $id2 }) "
                        + "MERGE (b)-[:" + relationName + "]->(a)";

                Map<String, Object> params = Map.of(
                        "id1", objectId1,
                        "id2", objectId2
                );

                tx.run(cypher, params);
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
