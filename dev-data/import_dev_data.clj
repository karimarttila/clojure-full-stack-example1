(ns import-dev-data
  (:require
    [clojure.data.csv :as csv]
    [clojure.java.io :as io]
    [clojure.string :as str]
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

(comment
  (get-books dev-data-dir)
  (get-movies dev-data-dir)
  (get-product-groups dev-data-dir)
  )


;
;(defn insert-product-group! [product-group]
;  (println "Inserting product-group: " product-group)
;  (let [[id name] product-group
;        command (str "INSERT INTO product_group VALUES ('" id "', '" name "');")]
;    (run-sql command)))
;
;(defn load-product-groups [product-groups]
;  (doseq [pg product-groups]
;    (insert-product-group! pg)))
;
;(defn delete-product-groups! []
;  (let [command (str "DELETE FROM product_group;")]
;    (println "Deleting product groups...")
;    (run-sql command))
;  )
;
;(-> (jdbc/execute-one! db ["SELECT document::TEXT FROM template WHERE id = ?" id])
;      :document
;      db->doc)



(comment
  (get-product-groups dev-data-dir)


  )
