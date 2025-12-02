from flask import Flask, render_template, request, redirect, url_for, flash
from flask_login import LoginManager, UserMixin, login_user, login_required, logout_user, current_user
from werkzeug.security import generate_password_hash, check_password_hash
from neo4j import GraphDatabase
from werkzeug.security import generate_password_hash
from flask import Flask, render_template, jsonify, request
from markupsafe import Markup
from neo4j import GraphDatabase
import os
import csv

# --- Flask setup
app = Flask(__name__)
app.secret_key = 'super-secret-key'

# --- Login manager
login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'login'

# --- Neo4j config
NEO4J_URI = "bolt://localhost:7687"
NEO4J_USER = "neo4j"
NEO4J_PASS = "password"

driver = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_USER, NEO4J_PASS))

@app.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        password_hash = generate_password_hash(password)

        with driver.session() as session:
            # Check if user already exists
            result = session.run(
                "MATCH (u:User {username: $username}) RETURN u",
                {"username": username}
            )
            if result.single():
                flash("Username already taken")
                return redirect(url_for('register'))

            # Create the user
            session.run(
                """
                CREATE (u:User {
                    id: toInteger(timestamp()),
                    username: $username,
                    password_hash: $password_hash
                })
                """,
                {"username": username, "password_hash": password_hash}
            )

            flash("Registration successful! You can now log in.")
            return redirect(url_for('login'))

    return render_template('register.html')


# --- User model
class User(UserMixin):
    def __init__(self, id_, username, password_hash):
        self.id = id_
        self.username = username
        self.password_hash = password_hash

# --- Load user from session
@login_manager.user_loader
def load_user(user_id):
    with driver.session() as session:
        result = session.run(
            "MATCH (u:User) WHERE u.id = $id RETURN u", {"id": int(user_id)}
        )
        record = result.single()
        if record:
            u = record["u"]
            return User(u["id"], u["username"], u["password_hash"])
    return None

# --- Routes
@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']

        with driver.session() as session:
            result = session.run(
                "MATCH (u:User {username: $username}) RETURN u",
                {"username": username}
            )
            record = result.single()
            if record:
                u = record["u"]
                if check_password_hash(u["password_hash"], password):
                    user = User(u["id"], u["username"], u["password_hash"])
                    login_user(user)
                    return redirect(url_for('dashboard'))

        flash("Invalid username or password")
    return render_template('login.html')


@app.route('/dashboard')
@login_required
def dashboard():
    return render_template('index.html', username=current_user.username)


@app.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('login'))

# --- Optional: Create a default user if none exists
def ensure_default_user():
    with driver.session() as session:
        result = session.run("MATCH (u:User {username: $username}) RETURN u", {"username": "admin"})
        if not result.single():
            hashed = generate_password_hash("secret")
            session.run(
                "CREATE (u:User {id: $id, username: $username, password_hash: $password_hash})",
                {"id": 1, "username": "admin", "password_hash": hashed}
            )

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

@app.route("/audio")
def audio():
    return render_template("audio.html")


@app.route("/video")
def video():
    return render_template("video.html")


@app.route("/book")
def book():
    keys, records, error, submitted_query = [], [], None, ""

    if request.method == "POST":
        submitted_query = request.form.get("cypher_query")
        keys, records, error = run_cypher_query(submitted_query)

    return render_template(
        "book.html",
        keys=keys,
        records=records,
        error=error,
        query=submitted_query,
    )

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


@app.route("/visual_art/Individual_page_var")
def Individual_page_var():
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
        return render_template("Individual_page_var.html")
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
        return render_template("Individual_page_var.html")

    # Render the template from templates/test.html
    return render_template(
        "Individual_page_var.html",
        title=title,
        artist_info=artist_info,
        image_path=image_path,
        section=section,
        content=content,
    )


#@app.route("/visual_art/Individual_page_var")
#def Individual_page_var():
#    return render_template("Individual_page_var.html")

def fetch_object_by_name(name):
    with driver.session() as session:
        result = session.run("MATCH (o:Objekt {name: $name}) RETURN o", name=name)
        record = result.single()
        if record:
            return dict(record["o"])
        return None

@app.route("/book/Roma_Sinti")
def Roma_Sinti():
    title = "Roma & Sinti : Zigeuner-Darstellungen der Moderne"
    artist_info = """
    Entstehung: frühes 20. Jahrhundert, kurz vor dem Ersten Weltkrieg Ungarn als Teil der Donaumonarchie 
    mit starken ethnischen Spannungen Zunehmende politische Kontrolle und Militarisierung (z. B. Haarschnitt 
    der Rekruten ab 1914 als Disziplinierungsmaßnahme) Stigmatisierung und Marginalisierung von Roma-Gruppen 
    im gesamten europäischen Raum Beginn einer staatlich regulierten Rassifizierung  Ermittlung der Daten…
    """
    image_path = "private/Roma_Sinti.png"

    try:
        with open(
                "../s1-templates/book_roma_all.html", "r", encoding="utf-8"
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
        content=content
    )



@app.route("/visual_art/Zwei_Zigeuner")
def Zwei_Zigeuner():
    title = "Zwei Zigeuner"
    artist_info = """
    Das Bild Bild ist ambivalent: Es zeigt einerseits Respekt für die Ästhetik und „Malerhaftigkeit“ der dargestellten Menschen, andererseits reproduziert es 
    stereotype und exotisierende Merkmale.Es kann sowohl als bewundernde Darstellung als auch als visuelle Festschreibung von „Andersartigkeit“ gelesen werden.
    """
    image_path = "../private/Zwei_Zigeuner.png"

    try:
        with open(
                "../s1-templates/zwei_zigeuner_obj_info_all.html", "r", encoding="utf-8"
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
        content=content
    )


@app.route("/visual_art/Ilonka")
def Ilonka():
    title = "Ilonka"
    artist_info = """
    Fremdbild vs. Selbstpräsenz: Ilonka wird mit Attributen der „Zigeunerin“ ausgestattet: dunkle Kleidung, Goldschmuck, 
    sinnlicher Blick Deutungsrahmen: Exotisierung – aber: Sie schaut selbstbewusst, konfrontativ zurück
    """
    image_path = "../private/Ilonka.png"

    try:
        with open(
                "../s1-templates/painting_ilonka_all.html", "r", encoding="utf-8"
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
        content=content
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
    try:
        with open(
                "../s1-templates/painting_zigeuner_all.html", "r", encoding="utf-8"
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
        content=content
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

    try:
        with open(
                "../s1-templates/painting_katze_all.html", "r", encoding="utf-8"
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
        content=content
    )

@app.route("/index/Metadata/")
def Metadata():
    title = "Metadata Description"
    properties_1 = []
    with open('../data/newdata/description_1.csv', newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            properties_1.append((row["Property"], row["text"]))

    properties_2= []
    with open('../data/newdata/description_2.csv', newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            properties_2.append((row["Property"], row["text"]))

    properties_3 = []
    with open('../data/newdata/description_3.csv', newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            properties_3.append((row["Property"], row["text"]))

    properties_4 = []
    with open('../data/newdata/description_4.csv', newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            properties_4.append((row["Property"], row["text"]))

    properties_5 = []
    with open('../data/newdata/description_5.csv', newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            properties_5.append((row["Property"], row["text"]))

    return render_template("Metadata_page_var.html", title=title,
                           properties_1=properties_1, properties_2=properties_2,
                           properties_3=properties_3, properties_4=properties_4,
                           properties_5=properties_5)


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


if __name__ == '__main__':
    ensure_default_user()
    app.run(host="0.0.0.0", debug=True)
