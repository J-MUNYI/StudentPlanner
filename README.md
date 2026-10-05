# Student Planner — Kotlin + MongoDB Full-Stack CRUD App

A single, integrated app — written in Kotlin (using the **Ktor** framework) with
**MongoDB** as the database — that helps a student track:

- **Core course work** — the units/courses they're taking
- **Number of hours** — hours per week expected vs. hours actually logged
- **Assignments per weekly submission** — whether work was submitted, and on time

It has **two parts working together**:
1. A **backend REST API** (Kotlin/Ktor) that performs CRUD operations against MongoDB.
2. A **frontend web UI** (plain HTML/CSS/JavaScript) that a real user clicks through —
   add courses, log hours, add assignments, mark them submitted — with no code
   editing or Postman required.

Both are served by the **same Ktor process on the same port**. Run one command,
open one browser tab, and you have a working app end to end.


---

## 1. Why this architecture

```
[ Browser (the UI you'll actually use) ]  <--HTTP-->  [ Kotlin/Ktor server on :8080 ]  <--Mongo driver-->  [ MongoDB ]
                                                          |
                                                          also serves the HTML/CSS/JS
                                                          files that make up the UI
```

The frontend (`src/main/resources/static/`) is just static HTML/CSS/JavaScript.
Ktor serves those files directly at `http://localhost:8080`, and the page's
JavaScript calls the same server's `/courses` and `/assignments` endpoints using
`fetch()`. There's no separate frontend server, no CORS setup, and no second port —
extracting this zip gives you one project that is both the frontend and the backend.

This is also the realistic pattern for a real Kotlin/MongoDB web app. If your unit
later asks specifically for an Android client instead of/in addition to a browser
UI, see section 8 — the backend here needs no changes to support that too.

### How the pieces fit together for the Android version

```
[ Android Emulator running android-app (WebView) ]
              |  loads http://10.0.2.2:8080
              v
[ Kotlin/Ktor backend running on YOUR laptop, port 8080 ]
              |  Mongo driver
              v
[ MongoDB — either installed locally on your laptop, OR a free Atlas cloud cluster ]
```

Important: the backend and MongoDB always run on your **laptop**, not inside the
emulator. The emulator is just a phone screen showing your app — it talks to your
laptop over the network exactly like a real app talks to a real server.

---

## 0. Step-by-step: full run guide (start here)

Do these in order, every time you want to run the whole thing:

1. **Start MongoDB** (see section 3 below for one-time setup — either local or Atlas)
2. **Run the Ktor backend** in IntelliJ IDEA (open `student-planner/`, run `Application.kt`) — leave it running
3. **Confirm it's up**: visit `http://localhost:8080` in a browser on your laptop; you should see the Student Planner web UI
4. **Open `android-app/` as a separate project in Android Studio** (not the same window as your backend — see section 8)
5. **Create/start an emulator** in Android Studio (Device Manager)
6. **Click ▶ Run** in Android Studio with the emulator selected as the target
7. The app installs and opens on the emulator, loading your backend at `http://10.0.2.2:8080` — you now have the same UI running as a native Android app, backed by MongoDB



---

## 2. Prerequisites (install these first)

| Tool | Why you need it | Link |
|---|---|---|
| **JDK 17+** | Kotlin runs on the JVM | https://adoptium.net/ |
| **IntelliJ IDEA (Community Edition)** | Best Kotlin editor, has Gradle + run buttons built in | https://www.jetbrains.com/idea/download/ |
| **MongoDB Community Server** | The actual database, running locally | https://www.mongodb.com/try/download/community |
| **MongoDB Compass** (optional but recommended) | GUI to see your collections/documents visually | https://www.mongodb.com/products/compass |
| **Gradle** | Not required separately — this project includes a Gradle wrapper (`gradlew`) that downloads the right version automatically | — |
| **Postman** (optional) | Nicer UI for testing the API than raw `curl` | https://www.postman.com/downloads/ |

### Only if you plan to extend this into an Android app next:
| Tool | Why | Link |
|---|---|---|
| **Android Studio** | IDE for building the Android client | https://developer.android.com/studio |
| **Android Emulator** (comes with Android Studio via Device Manager) | Lets you run the app without a physical phone. In Android Studio: `Tools > Device Manager > Create Device`, pick e.g. Pixel 6, download a system image (API 34 recommended), and launch it. | built into Android Studio |
| **Retrofit or Ktor Client** | Kotlin HTTP library your Android app would use to call this server's endpoints | added as a Gradle dependency in the Android project |

> This repo only contains the **server**. Adding the Android client is a separate
> Android Studio project that would call `http://10.0.2.2:8080` (the emulator's
> special address for your computer's `localhost`) instead of `localhost:8080`.

---

## 3. Setting up MongoDB — local vs. Atlas cluster

**You do not need to create an Atlas cluster** unless you want to — a locally
installed MongoDB works perfectly fine, since the backend (which is the only thing
that talks to MongoDB) runs on your own laptop regardless of whether the frontend
is a browser tab or the Android emulator.

Pick whichever is easier for you:

| | Local MongoDB | MongoDB Atlas (cloud, free tier) |
|---|---|---|
| Setup effort | Install once, runs in background | Sign up, click through a wizard, no install |
| Works offline | Yes | No — needs internet |
| Good for | Quick local dev, no account needed | Not having to manage a local service; viewing your data from any browser; if your laptop already fights you on installs |
| Recommended if | You just want it working fastest | You'd rather not install/manage a database service at all |

### Option A — Local MongoDB (simpler if you just want it running now)

1. Install MongoDB Community Server: https://www.mongodb.com/try/download/community
2. Start it:
   - **Windows**: installs as a service and starts automatically.
   - **Mac (Homebrew)**: `brew tap mongodb/brew && brew install mongodb-community && brew services start mongodb-community`
   - **Linux**: `sudo systemctl start mongod`
3. That's it — the app's default connection string (`mongodb://localhost:27017`)
   already points at this. No further config needed.
4. (Optional) Install **MongoDB Compass** to visually browse the `student_planner`
   database once your app creates data: https://www.mongodb.com/products/compass

### Option B — MongoDB Atlas (free cloud cluster, no local install)

1. Go to https://www.mongodb.com/cloud/atlas/register and create a free account.
2. Click **"Build a Database"** → choose the **M0 Free** tier → pick any cloud
   provider/region close to you → **Create**.
3. **Database Access** (left sidebar) → **Add New Database User** → set a username
   and password (save these — you'll need them in the connection string). Choose
   "Password" authentication and give it read/write access to any database.
4. **Network Access** (left sidebar) → **Add IP Address** → for development, choose
   **"Allow access from anywhere"** (`0.0.0.0/0`). This is fine for a student
   project; you wouldn't do this for a production app.
5. Go back to **Database** → click **Connect** on your cluster → **Drivers** → copy
   the connection string. It looks like:
   ```
   mongodb+srv://<username>:<password>@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
   ```
   Replace `<username>` and `<password>` with the ones you created in step 3.
6. Tell your Kotlin app to use this connection string instead of localhost. In
   IntelliJ: **Run → Edit Configurations** → select your `Application.kt` run
   config → **Environment variables** → add:
   ```
   MONGO_URI=mongodb+srv://<username>:<password>@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
   MONGO_DB_NAME=student_planner
   ```
   Or, running from a terminal instead of IntelliJ's run button:
   ```bash
   export MONGO_URI="mongodb+srv://<username>:<password>@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority"
   export MONGO_DB_NAME="student_planner"
   ./gradlew run
   ```
7. Run the backend and add a course through the UI — then go back to the Atlas
   dashboard → **Browse Collections** → you'll see the `student_planner` database
   with `courses` and `assignments` collections appear, populated with your data.

Either option works with everything else in this README unchanged — the Android
app, the web UI, and `http/requests.http` don't know or care whether MongoDB is
local or on Atlas; they only ever talk to your Kotlin backend.

---

## 4. Running the project

1. Extract this zip and open the `student-planner` folder in **IntelliJ IDEA**
   (`File > Open`, select the folder). IntelliJ will detect the Gradle project and
   download dependencies automatically (first time takes a few minutes).
2. Make sure MongoDB is running (step 3 above).
3. Run it one of two ways:
   - In IntelliJ: open `Application.kt` and click the green ▶ run arrow next to `fun main()`.
   - From a terminal in the project folder:
     ```bash
     ./gradlew run
     ```
     (On Windows use `gradlew.bat run`)
4. You should see log output ending with something like:
   ```
   Responding at http://0.0.0.0:8080
   ```
5. Open **`http://localhost:8080`** in a browser — this loads the actual Student
   Planner web app (not raw JSON). From here you can:
   - Add a course (name, code, hours/week, credit units)
   - Click **"+ Log Hours"** on a course card to record hours studied that week
   - Select a course from the dropdown in the Assignments panel
   - Add weekly assignments, mark them **on time**, **late**, or delete them
   - Watch the progress summary (weekly hours %, submissions, on-time rate) update live

   All of this is calling the same REST API under the hood — open your browser's
   DevTools (F12) → Network tab while clicking around to see the actual `GET`/`POST`/
   `PUT`/`DELETE` requests hitting MongoDB in real time.

### Using a different MongoDB connection string
By default the app connects to `mongodb://localhost:27017`. To override (e.g. for
Atlas), set an environment variable before running:
```bash
export MONGO_URI="mongodb+srv://<user>:<password>@cluster0.mongodb.net"
export MONGO_DB_NAME="student_planner"
./gradlew run
```

---

## 5. Using the app (frontend) vs. testing the API directly (backend)

**As a user:** just use the web UI at `http://localhost:8080` described above — that's
the normal way to interact with this app.

**As a developer verifying the backend in isolation** (useful for your unit, to show
you understand the raw CRUD layer independent of the UI): open `http/requests.http`
in IntelliJ (it has a built-in HTTP client — click the ▶ icon next to each request),
or copy the same requests into Postman.

Example flow:
1. `POST /courses` — create a course (e.g. "Programming Languages", 6 hrs/week, 3 units)
2. `GET /courses` — see it in the list
3. `POST /assignments` — create a weekly assignment tied to that course's `id`
4. `PUT /assignments/{id}` — mark it `submitted: true, submittedOnTime: true` and log `hoursSpent`
5. `GET /courses/{id}/progress` — see the computed summary: weekly hour progress %,
   how many assignments submitted, and on-time submission rate

All endpoints (the frontend calls these too — `/api/status` gives a quick JSON health check and endpoint list, since `/` now serves the UI page instead):

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/status` | Health check / lists available endpoints as JSON |
| GET | `/courses` | List all courses |
| POST | `/courses` | Create a course |
| GET | `/courses/{id}` | Get one course |
| PUT | `/courses/{id}` | Update a course (e.g. log hours) |
| DELETE | `/courses/{id}` | Delete a course (and its assignments) |
| GET | `/courses/{id}/progress` | Weekly hours + assignment completion summary |
| GET | `/assignments` | List all assignments |
| POST | `/assignments` | Create an assignment |
| GET | `/assignments/{id}` | Get one assignment |
| GET | `/assignments/course/{courseId}` | All assignments for one course |
| PUT | `/assignments/{id}` | Update an assignment (submit it, log hours) |
| DELETE | `/assignments/{id}` | Delete an assignment |

---

## 6. Kotlin concepts walkthrough (what to actually learn from this code)

Read the files in this order — each highlights specific Kotlin/Ktor concepts:

1. **`models/Course.kt` & `models/Assignment.kt`**
   - `data class` — auto-generates `equals()`, `toString()`, `copy()`. This is
     Kotlin's answer to Java's verbose POJOs/DTOs.
   - Default parameter values (`val submitted: Boolean = false`) — no constructor
     overloading needed like in Java.
   - `@Serializable` — a compiler plugin annotation (kotlinx.serialization) that
     auto-generates JSON <-> object conversion code at compile time.
   - A member function (`weeklyProgressPercent()`) living directly on the data class.

2. **`database/MongoDatabaseProvider.kt`**
   - `object` — Kotlin's built-in singleton pattern (no manual `getInstance()` needed).
   - `by lazy { }` — a property that computes its value only once, the first time
     it's accessed. Here it avoids opening a Mongo connection before it's needed.
   - The Elvis operator `?:` (`System.getenv("MONGO_URI") ?: "mongodb://..."`) —
     "use this value, or fall back to the default if it's null."

3. **`repository/CourseRepository.kt` & `AssignmentRepository.kt`**
   - `suspend fun` — Kotlin coroutines. These functions can perform I/O (talking to
     MongoDB over the network) without blocking a thread while waiting.
   - The repository pattern — routes never touch MongoDB directly, they go through
     this class. Makes the code testable and organized.

4. **`routes/CourseRoutes.kt` & `AssignmentRoutes.kt`**
   - Extension functions (`fun Route.courseRoutes(...)`) — Kotlin lets you "add" a
     function onto an existing type (`Route`) without modifying its source. This is
     how Ktor's whole routing DSL works.
   - Trailing lambda syntax (`get { ... }`, `post { ... }`) — functions that take a
     lambda as their last argument can be written like this, which is why Ktor
     routing reads almost like plain English.
   - Null-safety in action: `call.parameters["id"] ?: return@get call.respond(...)`
     — Kotlin forces you to handle the "id wasn't provided" case explicitly.

5. **`Application.kt`**
   - Ties everything together: installs plugins (JSON handling, logging, error
     pages), creates the repositories once, and registers the routes.
   - `staticResources("/", "static")` — this single line is what serves the
     frontend. It tells Ktor "any file in `resources/static` is available over
     HTTP", and automatically serves `index.html` when someone visits `/`. This is
     how the frontend and backend end up running as one process on one port.

6. **`resources/static/app.js`**
   - Plain JavaScript `fetch()` calls to the same REST endpoints you tested
     manually in `http/requests.http` — proof that the UI isn't doing anything the
     API doesn't already support. Look at `loadCourses()`, `addCourse` (the form
     submit handler), and `markSubmitted()` to see GET/POST/PUT calls in the wild.

---

## 7. Project structure

```
student-planner/
├── README.md                     <- you are here
├── build.gradle.kts               <- dependencies (Ktor, MongoDB driver, etc.)
├── settings.gradle.kts
├── .gitignore
├── http/
│   └── requests.http              <- ready-made CRUD test requests (backend-only testing)
└── src/
    └── main/
        ├── kotlin/com/studentplanner/
        │   ├── Application.kt              <- entry point, wires up backend + serves frontend
        │   ├── models/
        │   │   ├── Course.kt                <- course data model
        │   │   └── Assignment.kt            <- assignment data model
        │   ├── database/
        │   │   └── MongoDatabaseProvider.kt <- MongoDB connection setup
        │   ├── repository/
        │   │   ├── CourseRepository.kt      <- CRUD logic for courses
        │   │   └── AssignmentRepository.kt  <- CRUD logic for assignments
        │   └── routes/
        │       ├── CourseRoutes.kt          <- /courses HTTP endpoints
        │       └── AssignmentRoutes.kt      <- /assignments HTTP endpoints
        └── resources/
            ├── application.conf   <- Ktor + Mongo config
            └── static/             <- THE FRONTEND (served at http://localhost:8080)
                ├── index.html       <- page structure (course + assignment panels)
                ├── style.css        <- dark-mode styling
                └── app.js           <- fetch() calls to the backend, renders the UI
```

Note: this project does not include the Gradle wrapper binaries (`gradlew`,
`gradlew.bat`, `gradle/wrapper/`) since those are binary/generated files. Generate
them after extracting by running, inside the project folder (with Gradle installed
once, or via IntelliJ's built-in Gradle):
```bash
gradle wrapper --gradle-version 8.7
```
Or simply open the folder in IntelliJ IDEA — it will offer to set this up for you
automatically, and you can just use the IDE's run button instead of the command line.

---

## 8. Running the Android app (android-app folder)

This zip includes a second, separate project: **`android-app/`**. It's a minimal
native Android app whose single screen is a WebView showing your existing frontend
— so you get the same UI, running as an installed app on the emulator, with zero
duplicate CRUD logic.

**Important: `android-app/` is a different kind of project than `student-planner/`.**
IntelliJ IDEA (what you're using for the backend) doesn't have the Android SDK or
emulator tooling built in. You need **Android Studio** for this part specifically.

### One-time setup
1. Download and install **Android Studio**: https://developer.android.com/studio
   (It's built on the same platform as IntelliJ IDEA, so the interface will feel
   familiar — it just adds Android-specific tools.)
2. Open Android Studio → **Tools → Device Manager** → **Create Device** → pick a
   phone (e.g. Pixel 6) → download a system image if prompted (API 34 recommended)
   → **Finish**. This creates your emulator.

### Every time you want to run the app
1. Make sure MongoDB is running and your Ktor backend is running (`http://localhost:8080`
   loads in a browser) — do this first, the Android app has nothing to talk to otherwise.
2. Open Android Studio → **File → Open** → select the `android-app` folder
   (a sibling of `student-planner`, extracted from the same zip) → let Gradle sync
   (first time takes a few minutes, downloading the Android SDK components).
3. In the toolbar, select your emulator (e.g. "Pixel 6 API 34") from the device
   dropdown.
4. Click the green ▶ **Run** button.
5. The emulator boots (first launch takes a minute or two), the app installs, and
   opens automatically — showing your Student Planner UI running as a native app.
6. Add a course, add an assignment, mark it submitted — it's hitting the exact same
   backend and MongoDB as the browser version.

### If the app shows a blank screen / can't connect
- Confirm the backend is actually running on your laptop (`http://localhost:8080`
  works in a normal browser tab).
- The app is coded to reach it at `http://10.0.2.2:8080` — this only works on the
  **emulator**, not a real physical phone (see the comment in `MainActivity.kt` for why).
- If your firewall is blocking local connections, temporarily allow Java/Ktor
  through it.

### If you want a native UI instead of a WebView
The WebView approach gets you onto the emulator fastest and reuses everything
you've already built. If your assignment specifically wants native Android UI
(Jetpack Compose or XML layouts) calling the API with Retrofit instead of loading
HTML, that's a bigger rewrite of just the `android-app/` folder — the
`student-planner/` backend needs no changes either way, since it's just a REST API
underneath.

## 9. Project structure (both projects)

```
(extracted zip)/
├── student-planner/        <- Kotlin/Ktor backend + web frontend (open in IntelliJ IDEA)
│   └── ...(see structure above)...
└── android-app/             <- Native Android WebView app (open in Android Studio)
    ├── build.gradle.kts
    ├── settings.gradle.kts
    ├── gradle.properties
    └── app/
        ├── build.gradle.kts
        └── src/main/
            ├── AndroidManifest.xml
            ├── java/com/studentplanner/android/MainActivity.kt
            └── res/
                ├── values/strings.xml
                └── xml/network_security_config.xml
```
