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
    clj -M:dev:test:common:backend -m nrepl.cmdline -i -C

# Start backend repl with my toolbox.
@backend-kari:
    clj -M:dev:test:common:backend:kari -m nrepl.cmdline -i -C

# Start backend repl with my toolbox and with debug-repl capability.
@backend-debug-kari:
    clj -M:dev:test:common:backend:kari -m nrepl.cmdline --middleware com.gfredericks.debug-repl/wrap-debug-repl -i -C

@backend-kari-all:
    clj -M:dev:test:common:backend:kari:reveal -e "(require '[com.gfredericks.debug-repl] '[hashp.core] )" -m nrepl.cmdline --middleware '[com.gfredericks.debug-repl/wrap-debug-repl vlaaad.reveal.nrepl/middleware]' -i -C

# Init node packages.
@init:
   mkdir -p target
   mkdir -p classes
   npm install

# start frontend auto-compilation
@frontend:
    npm run dev

# Update dependencies.
@outdated:
    clojure -M:outdated --upgrade --force --download

# Create uberjar.
@uberjar:
    echo "TODO"

# Lint.
@lint:
    clj -M:dev:backend:common:frontend:test -m clj-kondo.main --lint src test

# Test.
@test db:
    ./run-tests.sh

# Clean .cpcache and .shadow-cljs directories, run npm install
@clean:
    rm -rf .cpcache/*
    rm -rf .shadow-cljs/*
    rm -rf target/*
    rm -rf dev-resources/*
    rm -rf prod-resources/*
    rm -rf out/*
    npm install
