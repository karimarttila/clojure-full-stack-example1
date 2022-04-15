# Clojure Full-stack Example 1

## Introduction

I decided to create this Clojure full-stack example to update my previous Clojure full-stack exercise, [re-frame-demo](https://github.com/karimarttila/clojure/tree/master/webstore-demo/re-frame-demo), which I created for learning [re-frame](https://github.com/day8/re-frame).

This new Clojure Full-stack Example 1 has two purposes:

1. A better and smoother UI for the web-store application example. Implement a better transit/json based contract between the backend and the frontend.
2. Provide an application that I can use to experiment with various deployment possibilities in AWS, Azure, and GCP.

I also wanted to get rid of the various backend data stores I used in the previous exercise (i.e., in this exercise, just use Postgres).

## Tooling

### Justfile

I use the [just](https://github.com/casey/just) project commands starter tool. The project's just configuration is in [Justfile](Justfile). Run `just` to list the commands.

### Database

The database configuration is in [postgres](postgres) directory. I use [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/) to run the [PostgreSQL](https://www.postgresql.org/) database in a container. I use [Flyway Community version](https://flywaydb.org/) to run the database migrations. See: [run-docker-compose.sh](postgres/run-docker-compose.sh).

Start the database by running: `just postgres-start` (and shutdown by running: `just postgres-shutdown`).

### Backend

The backend uses [deps cli](https://clojure.org/guides/deps_and_cli), the configuration is in [deps.edn](deps.edn) file.

I have created various [aliases](https://clojure.org/reference/deps_and_cli#_aliases), .e.g., 

- `dev`: dependencies that are used just in development phase.
- `test`: test dependencies.
- `common`: dependencies that are common for both backend and frontend side.
- `backend`: backend dependencies.
- `frontend`: frontend dependencies.
- etc.

Start backend [Clojure REPL](https://clojure.org/guides/repl/introduction) by running: `just backend`. (Well, I actually use `just backend-kari` since I have some personal configurations in `kari` alias which is in my `~/.clojure/deps.edn` file).

### Frontend

I use [shadow-cljs](https://github.com/thheller/shadow-cljs) to build the frontend (transpiling [Clojurescript](https://clojurescript.org/) to [Javascript](https://en.wikipedia.org/wiki/JavaScript)). You can find the shadow-cljs configuration in [shadow-cljs.edn](shadow-cljs.edn) file.

First you need to install the frontend dependencies found in [package.json](package.json): `just init`.

Start the frontend build by running: `just frontend`.

### CSS

I use [Tailwind CSS](https://tailwindcss.com/) to provide various CSS utilities. You can add additional CSS classes in the [tailwind.css](src/css/tailwind.css) file.

Start the CSS build process by running: `just css`.

### Development & Debugging

TODO: hashp.

### Production Deployment Unit

There are two Just recipes: `just uberjar` for building the uberjar deployment unit and `just run-uberjar` for testing the uberjar.

There are a couple of things regarding the deployment unit building that I must emphasize since these are not that obvious. I used the new Clojure [tools.build](https://clojure.org/guides/tools_build) to create the uberjar. Let's see the build process:

```bash
# Build uberjar.
@uberjar:
    # Clean everything.
    rm -rf target
    rm -rf prod-resources
    # Frontend.
    npm run postcss:release
    clj -M:dev:shadow-cljs:common:backend:frontend:profile-prod release app
    # Backend.
    clj -T:backend:common:dev:build uber
```

So, First clean `target` and `prod-resources` to have a clean starting point. Then we create the css package and the frontend `app.js` (into the `prod-resources` folder).

Then we compile the backend and build the actual jar from the backend classes, and from the `resources` (e.g. `index.html`) and the `prod-resources` (`main.css` and `app.js`). See the `build` alias in the [deps.edn](deps.edn):

```clojure
           :build {:deps {io.github.clojure/tools.build {:git/tag "v0.8.1" :git/sha "7d40500"}}
                   :ns-default mybuild}
```

In the [mybuild.clj](dev/mybuild.clj) you can find the `uber` function:


(defn uber [_]
  (b/copy-dir {:src-dirs ["src" "resources" "prod-resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :ns-compile ['clojure.tools.logging.impl]
                  :class-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'simpleserver.main}))

Now, here comes the hairy part, so hairy that I got help from my Metosin colleagues. You have to remember two things:

Since the logging framework requires the `LoggingFactory` you need to add extra compilation phase for it:

```clojure
  (b/compile-clj {:basis basis
                  :ns-compile ['clojure.tools.logging.impl]
                  :class-dir class-dir})
```

The reason for this is explained in this ticket: [CLJ-1544](https://clojure.atlassian.net/browse/CLJ-1544).

Secondly, you must add `(:gen-class))` in your main clj file (as in [main.clj](src/clj/simpleserver/main.clj)).



## Application

### Backend

TODO: reitit, malli, aero, integrant ...
### Frontend

TODO: re-frame...




