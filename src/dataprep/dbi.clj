(ns dataprep.dbi
  (:require [clojure.java.jdbc :as sql]))

;; change subname to an env var.
(def pg-db {:subprotocol "postgresql"
            :subname "//localhost:5432/atxdata"})

(defn restaurant-exists? [record]
  (let [facility-id (:facility_id record)
        query       ["select * from inspections where facility_id = ?" facility-id]]
  (not (empty? (sql/query pg-db query)))))

(defn inspection-exists? [record]
  (let [facility-id (:facility_id record)
        date        (:date record)
        query       ["select * from inspections where facility_id = ? AND date = ?" facility-id date]]
  (not (empty? (sql/query pg-db query)))))


(defn insert-restaurant [record]
  (if-not (restaurant-exists? record)
    (sql/insert! pg-db :restaurants record)))

(defn insert-inspection [record]
  (if-not (inspection-exists? record)
    (sql/insert! pg-db :inspections record)))


