(ns dev-data
  (:require
    [clojure.data.csv :as csv]
    [clojure.java.io :as io]
    [clojure.string :as str]
    [next.jdbc :as jdbc]
    [hashp.core]))

(def dev-data-dir "dev-data")

(defn read-csv [data-dir file-name]
  (with-open [reader (io/reader (str data-dir "/" file-name))]
              (doall
                (csv/read-csv reader))))

(defn get-product-groups [data-dir]
  (map (fn [[item]]
         (let [[id name] (str/split item #"\t")]
           {:id id :name name}))
       (read-csv data-dir "product-groups.csv")))

(defn get-books [data-dir]
  (map (fn [[item]]
         (let [[id pg-id name price author year country language] (str/split item #"\t")]
           {:id id :pg-id pg-id :name name :price price :author author :year year :country country :language language}))
       (read-csv data-dir "products-books.csv")))

(defn get-movies [data-dir]
  (map (fn [[item]]
         (let [[id pg-id name price director year country genre] (str/split item #"\t")]
           {:id id :pg-id pg-id :name name :price price :director director :year year :country country :genre genre}))
       (read-csv data-dir "products-movies.csv")))

; TODO TÄHÄN JÄI
; Tee ensin integranttiin db ja laita se myös user, jotta voi testata täällä...

;(defn upsert-product-groups [db product-groups]
;  (for [pg product-groups]
;    (jdbc/execute! db ["INSERT INTO document (id, document) VALUES (?,?::JSONB)
;                      ON CONFLICT (id) DO UPDATE SET document=?::JSONB"
;                       id (doc->db doc) (doc->db doc)]))
;  )


(defn reset-product-groups [db]
  (-> (jdbc/execute-one! (:ds db) ["DELETE FROM simpleserver.product_group"])))


(defn copy-dev-data [ds]
  (with-open [con (jdbc/get-connection ds)]
    (-> (jdbc/execute-one! con ["DELETE FROM simpleserver.product_book"]))
    (-> (jdbc/execute-one! con ["DELETE FROM simpleserver.product_movie"]))
    (-> (jdbc/execute-one! con ["DELETE FROM simpleserver.product_group"]))
    (-> (jdbc/execute-one! con [(str "COPY simpleserver.product_group(id, name)"
                                     " FROM '/var/lib/postgresql/dev-data/product-groups.csv' "
                                     " DELIMITER '	'"
                                     " CSV")]))
    (-> (jdbc/execute-one! con [(str "COPY simpleserver.product_book(id, pg_id, title, price, author, year, country, language)"
                                     " FROM '/var/lib/postgresql/dev-data/products-books.csv' "
                                     " DELIMITER '	'"
                                     " CSV")]))
    (-> (jdbc/execute-one! con [(str "COPY simpleserver.product_movie(id, pg_id, title, price, director, year, country, genre)"
                                     " FROM '/var/lib/postgresql/dev-data/products-movies.csv' "
                                     " DELIMITER '	'"
                                     " CSV")]))))

(comment
  ;["SELECT * FROM simpleserver.product_book"]
  (get-books dev-data-dir)
  (get-movies dev-data-dir)
  (get-product-groups dev-data-dir)
  (reset-product-groups (user/db))
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


