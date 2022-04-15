(ns mybuild
  (:require [clojure.tools.build.api :as b]))


(def lib 'simpleserver)
(def version (format "1.2.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"
                            :aliases [:profile-prod :common :backend :frontend]}))

;(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))
(def uber-file (format "target/%s.jar" (name lib)))


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

(comment
  (def jee (b/create-basis {:project "deps.edn"
                            :aliases [:dev :common :backend :frontend]}))
  (keys jee)
  (get-in jee [:resolve-args])

  )
