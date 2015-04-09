(ns dataprep.core
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.data.csv :as csv]
            [semantic-csv.core :as sc]
            [clj-http.lite.client :as http]))


(defn split-point [address]
  "Takes the :Address value from a record and splits it up. Returns a map
   containing the address and two new latitude and longitude fields."
  (let [a (s/split address #"\n\(")
        b (->> (s/split (last a) #",\s")
               (assoc a 1)
               flatten)]
    {:address   (first b)
     :latitude  (second b)
     :longitude (last b)}))

(defn clean-latlon [angle]
  "The purpose of this function is to verify that lat long is clean."
  (s/replace angle #"[^0-9\.\-]" ""))

(defn rebuild-records [record]
  (let [new-fields (split-point (:Address record))]
    {:restaurant
     (array-map
       :facility_id ((keyword "Facility ID") record)
       :name        ((keyword "Restaurant Name") record)
       :zip         ((keyword "Zip Code") record)
       :address     (:address new-fields)
       :latitude    (-> new-fields :latitude clean-latlon)
       :longitude   (-> new-fields :longitude clean-latlon))
     :inspection
     (array-map
       :date        ((keyword "Inspection Date") record)
       :score       (:Score record)
       :facility_id ((keyword "Facility ID") record)
       :description ((keyword "Process Description") record))}))


(defn write-pieces [r-out i-out data]
  (let [r (sc/vectorize (map :restaurant data))
        i (sc/vectorize (map :inspection data))]
    (csv/write-csv r-out r)
    (csv/write-csv i-out i)
    true))

(defn split-csv [in out1 out2]
  (with-open [in-file (io/reader in)
              r-out (io/writer out1)
              i-out (io/writer out2)]
    (->>
      (csv/read-csv in-file)
      sc/remove-comments
      sc/mappify
      (map
        (fn [row]
          (rebuild-records row)))
      (write-pieces r-out i-out))))
