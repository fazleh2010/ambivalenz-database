# Marburg Project 

## first run the neo4j docker container
get the neo4j container from docker hub
```
docker pull neo4j
```
If docker container already in your machine then run neo4j container 
```
docker run \
  --name neo4j \
  -p7474:7474 -p7687:7687 \
  -d \
  -e NEO4J_AUTH=neo4j/password \
  neo4j
```



This command-line tool is designed for creating nodes in graph database .
## Compile And Run
<p>The source code can be compiled and run using <em>Java 11</em> and <em>Maven</em>.</p>

```shell script
git clone https://github.com/fazleh2010/marburg-project.git 
```
build the jar file
```shell script
mvn clean package
```
- CREATE: add nodes in neo4j graph database .
- RELATION: add relation between nodes . 
- CREATE_RELATION: add nodes and relation in neo4j graph database .
- DELETE: delete nodes .

Run the system:
````shell script
java -jar <jar file> <parameter> 

java -jar target/QuestionGrammarGenerator.jar CREATE

java -jar target/QuestionGrammarGenerator.jar RELATION

java -jar target/QuestionGrammarGenerator.jar CREATE_RELATION

java -jar target/QuestionGrammarGenerator.jar DELETE


Check the output in 

- http://localhost:7474/browser/ (if the program is run in local host)
- http://137.248.186.54:7474/browser/ (if run it from server)

## Developers
* **Mohammad Fazleh Elahi**
