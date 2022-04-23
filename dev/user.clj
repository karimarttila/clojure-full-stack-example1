(ns user
  (:require [integrant.repl :refer [reset]]
            ;[integrant.repl :refer [clear go halt prep init reset reset-all]]
            [integrant.repl.state :as state]
            [simpleserver.main :as main]))

(integrant.repl/set-prep! main/system-config-start)

(defn system [] (or state/system (throw (ex-info "System not running" {}))))
(defn env [] (:backend/env (system)))
(defn db [] (:backend/db (system)))
(defn db-super [] (:backend/db-super (system)))

(defn my-dummy-reset []
  (reset))

(comment
  (user/system)
  (user/env)
  )

