# This is the Justfile for the project commands.
# See: https://github.com/casey/just

@list:
   just --list

# Start local PostgreSQL database.
@postgres-start:
    cd postgres && ./run-docker-compose.sh

# Shutdown local PostgreSQL database.
@postgres-shutdown:
    cd postgres && ./shutdown-docker-compose.sh

# Start backend repl.
@backend:
    clj -M:build:dev:profile-dev:common:backend:test -m nrepl.cmdline -i -C

# Start backend repl with my toolbox.
@backend-kari:
    clj -M:build:dev:profile-dev:common:backend:test:kari -m nrepl.cmdline -i -C

# Start backend repl with my toolbox and with debug-repl capability.
@backend-debug-kari:
    clj -M:build:dev:profile-dev:common:backend:test:kari -m nrepl.cmdline --middleware com.gfredericks.debug-repl/wrap-debug-repl -i -C

@backend-kari-all:
    clj -M:build:dev:profile-dev:common:backend:test:kari:reveal -e "(require '[com.gfredericks.debug-repl] '[hashp.core] )" -m nrepl.cmdline --middleware '[com.gfredericks.debug-repl/wrap-debug-repl vlaaad.reveal.nrepl/middleware]' -i -C

# Init node packages.
@init:
   mkdir -p target
   mkdir -p classes
   npm install

# Start frontend auto-compilation.
@frontend:
    npm run shadow:watch

# Start css auto-compilation.
# NOTE: do not use "npm run dev", since postcss breaks occasionally - run frontend and css separately.
@css:
    npm run postcss:watch

# Update dependencies.
@outdated:
    clojure -M:outdated --upgrade --force --download

# Lint.
@lint:
    clj -M:dev:backend:common:frontend:test -m clj-kondo.main --lint src test

# Test.
@test db:
    ./run-tests.sh

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

# Run uberjar.
@run-uberjar:
    # Change to target to make sure we are not using dev-resources folder but prod-resources is baked into jar.
    cd target && PROFILE=prod java -Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory -jar simpleserver.jar
    # cd target && DB_FORCE_INITIALIZE_DEV_DATA=true PROFILE=prod java -Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory -jar simpleserver.jar

# Print dependency tree.
@print-deps:
    clj -X:deps tree :aliases '[:dev :test :common :backend :frontend]'
    #clj  -Stree -M:dev:test:common:backend:frontend

# Clean .cpcache and .shadow-cljs directories, run npm install
@clean:
    rm -rf .cpcache/*
    rm -rf .shadow-cljs/*
    rm -rf target/*
    rm -rf dev-resources/*
    rm -rf prod-resources/*
    rm -rf out/*
    npm install
