from flask import Flask, render_template, jsonify, request
from markupsafe import Markup
from neo4j import GraphDatabase
import os
from bs4 import BeautifulSoup


app = Flask(__name__, static_url_path="/static")

# Configure your Neo4j connection
# uri = "bolt://localhost:7687"
# username = "neo4j"
# password = "password"
# driver = GraphDatabase.driver(uri, auth=(username, password))

uri = os.getenv("NEO4J_URI", "bolt://localhost:7687")
username = os.getenv("NEpyO4J_USERNAME", "neo4j")
password = os.getenv("NEO4J_PASSWORD", "password")
driver = GraphDatabase.driver(uri, auth=(username, password))


def get_nodes():
    with driver.session() as session:
        result = session.run("MATCH (n:Person) RETURN n.name AS name LIMIT 10")
        return [record["name"] for record in result]


@app.route("/")
def index():
    return render_template("index.html")


@app.route("/api/nodes")
def nodes():
    data = get_nodes()
    return jsonify(data)


@app.route("/sections/")
def sections():
    return render_template("sections.html")  # or any logic here


@app.route("/visual_art", methods=["GET", "POST"])
def visual_art():
    keys, records, error, submitted_query = [], [], None, ""

    if request.method == "POST":
        submitted_query = request.form.get("cypher_query")
        keys, records, error = run_cypher_query(submitted_query)

    return render_template(
        "visual_art.html",
        keys=keys,
        records=records,
        error=error,
        query=submitted_query,
    )

    #    return render_template("visual_art.html")


# @app.route("/visual_art", methods=["GET", "POST"])  MATCH (n) RETURN n LIMIT 5
# def query():
#    keys, records, error, submitted_query = [], [], None, ""

#    if request.method == "POST":
#        submitted_query = request.form.get("cypher_query")
#        keys, records, error = run_cypher_query(submitted_query)

#    return render_template("visual_art.html", keys=keys, records=records, error=error, query=submitted_query)


@app.route("/audio")
def audio():
    return render_template("audio.html")


@app.route("/video")
def video():
    return render_template("video.html")


@app.route("/book")
def book():
    return render_template("book.html")


@app.route("/about/")
def about():
    return render_template("about.html")


@app.route("/context-project/")
def context_project():
    return render_template("context_project.html")


@app.route("/history-of-romarchive/")
def history_of_romarchive():
    return render_template("history_of_romarchive.html")


@app.route("/curators/")
def curators():
    return render_template("curators.html")


@app.route("/ethical-guidelines/")
def ethical_guidelines():
    return render_template("ethical_guidelines.html")


@app.route("/collection-policy/")
def collection_policy():
    return render_template("collection_policy.html")


@app.route("/faq/")
def faq():
    return render_template("faq.html")


@app.route("/search/")
def search():
    return render_template("search.html")


@app.route("/terms/")
def terms():
    return render_template("terms.html")


@app.route("/contact/")
def contact():
    return render_template("contact.html")


@app.route("/collection/politics-of-photography/")
def politics_of_photography():
    return render_template("politics_of_photography.html")  # or return some content


@app.route("/visual_art/Individual_page")
def Individual_page():
    return render_template("Individual_page.html")


@app.route("/visual_art/Individual_page_2")
def Individual_page_2():
    section = request.args.get("section", default=None)

    # title = "Title: Zwei Zigeuner"
    title = ""
    artist_info = """
                   Das Bild ist ambivalent: Es zeigt einerseits Respekt für die Ästhetik und „Malerhaftigkeit“ 
                   der dargestellten Menschen, andererseits reproduziert es stereotype und exotisierende Merkmale. 
                   Es kann sowohl als bewundernde Darstellung als auch als visuelle Festschreibung von „Andersartigkeit“ gelesen werden.
                """
    image_path = "../private/Zwei_Zigeuner.png"

    if section == "Objekt_Informationen":
        return render_template("Individual_page_2.html")
    elif section == "Inhaltliche_Beschreibung":
        try:
            with open(
                    "../s1-templates/zwei_zigeuner_inh_besc.html", "r", encoding="utf-8"
            ) as file:
                table_html = file.read()
            content = Markup(table_html)
        except FileNotFoundError:
            content = "Table file not found."
    elif section == "Semantische_Annotation":
        try:
            with open(
                    "../s1-templates/zwei_zigeuner_sem_ann.html", "r", encoding="utf-8"
            ) as file:
                table_html = file.read()
            content = Markup(table_html)
        except FileNotFoundError:
            content = "Table file not found."
    elif section == "Semantische_Relationen":
        try:
            with open(
                    "../s1-templates/zwei_zigeuner_sem_rel.html", "r", encoding="utf-8"
            ) as file:
                table_html = file.read()
            content = Markup(table_html)
        except FileNotFoundError:
            content = "Table file not found."
    elif section == "Technische_rechtliche":
        try:
            with open(
                    "../s1-templates/zwei_zigeuner_tech_rech.html", "r", encoding="utf-8"
            ) as file:
                table_html = file.read()
            content = Markup(table_html)
        except FileNotFoundError:
            content = "Table file not found."
    else:
        return render_template("Individual_page_2.html")

    # Render the template from templates/test.html
    return render_template(
        "Individual_page_var.html",
        title=title,
        artist_info=artist_info,
        image_path=image_path,
        section=section,
        content=content,
    )


@app.route("/visual_art/Individual_page_var")
def Individual_page_var():
    return render_template("Individual_page_var.html")


@app.route("/visual_art/Zwei_Zigeuner")
def Zwei_Zigeuner():
    title = "Zwei Zigeuner"
    artist_info = """
    Das Bild <a href = "https://en.wikipedia.org/wiki/Bild" target = "_blank" rel = "noopener noreferrer" > Bild </a> ist ambivalent: 
    Es zeigt einerseits Respekt für die Ästhetik und „Malerhaftigkeit“ der dargestellten Menschen, andererseits reproduziert es 
    < a href = "https://en.wikipedia.org/wiki/Stereotype" target = "_blank" rel = "noopener noreferrer" > stereotype </a> und
    exotisierende Merkmale.Es kann sowohl als bewundernde Darstellung als auch als visuelle Festschreibung von „Andersartigkeit“ gelesen werden.
    """
    image_path = "../private/Zwei_Zigeuner.png"

    section = request.args.get("section", default=None)

    if section == "Objekt_Informationen":
        try:
            with open(
                    "../s1-templates/zwei_zigeuner_obj_info.html", "r", encoding="utf-8"
            ) as file:
                table_html = file.read()
            content = Markup(table_html)
        except FileNotFoundError:
            content = "Table file not found."
    elif section == "Inhaltliche_Beschreibung":
        try:
            with open(
                    "../s1-templates/zwei_zigeuner_inh_besc.html", "r", encoding="utf-8"
            ) as file:
                table_html = file.read()
            content = Markup(table_html)
        except FileNotFoundError:
            content = "Table file not found."
    elif section == "Semantische_Annotation":
        try:
            with open(
                    "../s1-templates/zwei_zigeuner_sem_ann.html", "r", encoding="utf-8"
            ) as file:
                table_html = file.read()
            content = Markup(table_html)
        except FileNotFoundError:
            content = "Table file not found."
    elif section == "Semantische_Relationen":
        try:
            with open(
                    "../s1-templates/zwei_zigeuner_sem_rel.html", "r", encoding="utf-8"
            ) as file:
                table_html = file.read()
            content = Markup(table_html)
        except FileNotFoundError:
            content = "Table file not found."
    elif section == "Technische_rechtliche":
        try:
            with open(
                    "../s1-templates/zwei_zigeuner_tech_rech.html", "r", encoding="utf-8"
            ) as file:
                table_html = file.read()
            content = Markup(table_html)
        except FileNotFoundError:
            content = "Table file not found."


    return render_template(
        "Individual_page_var.html",
        title=title,
        artist_info=artist_info,
        image_path=image_path,
        section=section,
    )


@app.route("/visual_art/Ilonka")
def Ilonka():
    title = "Ilonka"
    artist_info = """
    Fremdbild vs. Selbstpräsenz: Ilonka wird mit Attributen der „Zigeunerin“ ausgestattet: dunkle Kleidung, Goldschmuck, 
    sinnlicher Blick Deutungsrahmen: Exotisierung – aber: Sie schaut selbstbewusst, konfrontativ zurück
    """
    image_path = "../private/Ilonka.png"
    return render_template(
        "Individual_page_var.html",
        title=title,
        artist_info=artist_info,
        image_path=image_path,
    )


@app.route("/visual_art/Zigeuner")
def Zigeuner():
    title = "Pipázó cigány (Pfeife rauchender „Zigeuner“)"
    artist_info = (
        "Im Gegensatz zu vielen Darstellungen dieser Zeit: keine offensichtlichen antiziganistischen Stereotype. "
        "Der dargestellte Mann wird nicht exotisiert oder kriminalisiert, sondern erscheint würdevoll, selbstbewusst und bürgerlich. "
        "Titel „Pipázó cigány“ (übersetzt: „Pfeife rauchender Zigeuner“) verwendet die historisch belastete Bezeichnung „Zigeuner“. "
        "Schon durch die Benennung wird eine Fremdzuschreibung vorgenommen: Statt den individuellen Namen des Modells zu nennen, wird seine ethnische Zugehörigkeit betont und stereotyp markiert. "
        "Die Wortwahl verstärkt eine folkloristische Rahmung („Zigeuner“ + Pfeife als romantisierende, exotisierende Attribute). Auch wenn das Bild selbst individuelle Würde zeigt, reproduziert der Titel eine kulturelle Distanz und eine Fremddefinition."
    )
    image_path = "../private/Zigeuner.png"
    return render_template(
        "Individual_page_var.html",
        title=title,
        artist_info=artist_info,
        image_path=image_path,
    )


@app.route("/visual_art/Katze")
def Katze():
    title = "Zwei Zigeunerin** mit Katze*"
    artist_info = (
        "•Rassistischer Werktitel (Begriff „Zigeunerinnen“). "
        "Darstellung als exotisch, erotisch, unzivilisiert. "
        "Fetischisierung weiblicher Romnja-Körper. "
        "Reproduktion kolonialer Zuschreibungen („das Andere“)"
    )
    image_path = "../private/Katze.png"
    return render_template(
        "Individual_page_var.html",
        title=title,
        artist_info=artist_info,
        image_path=image_path,
    )


@app.route("/supporters/")
def supporters():
    return "<h1>Supporters Page</h1>"


@app.route("/imprint/")
def imprint():
    return "<h1>Imprint Page</h1>"


@app.route("/privacy/")
def privacy():
    return "<h1>Privacy Statement</h1>"


@app.route("/project-detail")
def project_detail():
    # You can render a template or just return a string here
    return "Welcome to the Project Detail page!"


def run_cypher_query(cypher_query):
    with driver.session() as session:
        try:
            result = session.run(cypher_query)
            keys = result.keys()
            records = [record.data() for record in result]
            return keys, records, None
        except Exception as e:
            return [], [], str(e)


if __name__ == "__main__":
    app.run(host="0.0.0.0", debug=True)
