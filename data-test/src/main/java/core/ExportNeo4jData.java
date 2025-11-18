import org.neo4j.driver.*;

import java.io.FileWriter;

public class ExportNeo4jData {
    public static void main(String[] args) throws Exception {
        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "password"));

        try (Session session = driver.session()) {
            FileWriter nodeWriter = new FileWriter("dataset/dumb/nodes.csv");
            nodeWriter.write("id,label,properties\n");

            session.run("MATCH (n) RETURN id(n) AS id, labels(n)[0] AS label, properties(n) AS props")
                .forEachRemaining(record -> {
                    try {
                        nodeWriter.write(record.get("id") + "," +
                            record.get("label") + "," +
                            record.get("props").toString().replace("\n", " ") + "\n");
                    } catch (Exception e) { e.printStackTrace(); }
                });

            nodeWriter.close();

            FileWriter relWriter = new FileWriter("dataset/dumb/rels.csv");
            relWriter.write("start,end,type,properties\n");

            session.run("MATCH (a)-[r]->(b) RETURN id(a) AS start, id(b) AS end, type(r) AS type, properties(r) AS props")
                .forEachRemaining(record -> {
                    try {
                        relWriter.write(record.get("start") + "," +
                            record.get("end") + "," +
                            record.get("type") + "," +
                            record.get("props").toString().replace("\n", " ") + "\n");
                    } catch (Exception e) { e.printStackTrace(); }
                });

            relWriter.close();
        }

        driver.close();
    }
}
