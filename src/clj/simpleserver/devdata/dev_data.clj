(ns simpleserver.devdata.dev-data
  (:require
    [clojure.data.csv :as csv]
    [clojure.java.io :as io]
    [clojure.string :as str]
    [next.jdbc :as jdbc]))

(defn copy-dev-data [{:keys [dev-data-dir] :as ds}]
  (with-open [con (jdbc/get-connection ds)]
    (-> (jdbc/execute-one! con ["DELETE FROM simpleserver.product_book"]))
    (-> (jdbc/execute-one! con ["DELETE FROM simpleserver.product_movie"]))
    (-> (jdbc/execute-one! con ["DELETE FROM simpleserver.product_group"]))
    (-> (jdbc/execute-one! con [(str "COPY simpleserver.product_group(id, name)"
                                     " FROM '" dev-data-dir "/product-groups.csv' "
                                     " DELIMITER '	'"
                                     " CSV")]))
    (-> (jdbc/execute-one! con [(str "COPY simpleserver.product_book(id, pg_id, title, price, author, year, country, language)"
                                     " FROM '" dev-data-dir "/products-books.csv' "
                                     " DELIMITER '	'"
                                     " CSV")]))
    (-> (jdbc/execute-one! con [(str "COPY simpleserver.product_movie(id, pg_id, title, price, director, year, country, genre)"
                                     " FROM '" dev-data-dir "/products-movies.csv' "
                                     " DELIMITER '	'"
                                     " CSV")]))))

(comment

  (copy-dev-data (user/db-super))
  (user/db)
  (user/db-super)
  (def my-opts {:dbtype "postgresql",
                :dbname "simpleserver",
                :user "simpleserver",
                :password "simpleserver",
                :host "localhost",
                :port 5512})
  (def my-ds (jdbc/get-datasource my-opts))
  (jdbc/execute-one! my-ds ["SELECT * FROM simpleserver.product_book"])

  (with-open [con (jdbc/get-connection (user/db-super))]
    (-> (jdbc/execute! con ["SELECT * FROM simpleserver.product_group"])))

  (with-open [con (jdbc/get-connection (user/db-super))]
    (-> (jdbc/execute-one! con [(str "COPY simpleserver.product_group(id, name)"
                                     " FROM '/var/lib/postgresql/dev-data/product-groups.csv' "
                                     " DELIMITER '	'"
                                     " CSV")])))

  (with-open [con (jdbc/get-connection (user/db-super))]
    (-> (jdbc/execute-one! con [(str "COPY simpleserver.product_book(id, pg_id, title, price, author, year, country, language)"
                                     " FROM '/var/lib/postgresql/dev-data/products-books.csv' "
                                     " DELIMITER '	'"
                                     " CSV")])))

  )


