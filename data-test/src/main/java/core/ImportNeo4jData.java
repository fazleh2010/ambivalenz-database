import org.neo4j.driver.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class ImportNeo4jData {
    public static void main(String[] args) throws Exception {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "password"));

        // Mapping from old exported ID to new Neo4j node ID
        Map<String, String> idMap = new HashMap<>();

        try (Session session = driver.session()) {

            // --- Import Nodes ---
            BufferedReader nodeReader = new BufferedReader(new FileReader("dataset/dumb/nodes.csv"));
            String line;
            nodeReader.readLine(); // skip header

            while ((line = nodeReader.readLine()) != null) {
                String[] parts = line.split(",", 3); // id, label, properties
                if (parts.length < 3) continue;

                String oldId = parts[0].trim();
                String label = parts[1].trim();
                String props = parts[2].trim();

                // Convert props string to map (simplified, not robust for all cases)
                Map<String, Object> properties = parseProperties(props);

                // Create node and get new ID
                String cypher = "CREATE (n:" + label + " $props) RETURN id(n) as newId";
                Result result = session.run(cypher, Values.parameters("props", properties));
                String newId = result.single().get("newId").asLong() + "";

                idMap.put(oldId, newId);
            }
            nodeReader.close();

            // --- Import Relationships ---
            BufferedReader relReader = new BufferedReader(new FileReader("dataset/dumb/rels.csv"));
            relReader.readLine(); // skip header

            while ((line = relReader.readLine()) != null) {
                String[] parts = line.split(",", 4); // start, end, type, properties
                if (parts.length < 4) continue;

                String startId = idMap.get(parts[0].trim());
                String endId = idMap.get(parts[1].trim());
                String type = parts[2].trim();
                String props = parts[3].trim();

                Map<String, Object> relProps = parseProperties(props);

                String cypher = "MATCH (a), (b) WHERE id(a) = $start AND id(b) = $end " +
                                "CREATE (a)-[r:" + type + " $props]->(b)";
                session.run(cypher, Values.parameters(
                        "start", Long.parseLong(startId),
                        "end", Long.parseLong(endId),
                        "props", relProps
                ));
            }

            relReader.close();
        }

        driver.close();
    }

    // Very simple parser for properties formatted like {key=value, key2=value2}
    private static Map<String, Object> parseProperties(String propString) {
        Map<String, Object> map = new HashMap<>();
        propString = propString.trim();
        if (propString.startsWith("{") && propString.endsWith("}")) {
            propString = propString.substring(1, propString.length() - 1);
            String[] entries = propString.split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");
            for (String entry : entries) {
                String[] kv = entry.split("=", 2);
                if (kv.length == 2) {
                    String key = kv[0].trim();
                    String value = kv[1].trim().replace("\"", "");
                    map.put(key, value);
                }
            }
        }
        return map;
    }
}
