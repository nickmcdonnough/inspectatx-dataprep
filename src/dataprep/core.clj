(ns dataprep.core
  (:require [dataprep.dbi :as dbi]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.data.csv :as csv]
            [semantic-csv.core :as sc]
            [clj-http.lite.client :as http]))

(def coa-site "https://data.austintexas.gov/api/views/ecmv-9xxi/rows.csv?accessType=DOWNLOAD")

(defn fetch-csv []
  (with-open [in (io/input-stream coa-site)
              out (io/output-stream "allinspections.csv")]
    (io/copy in out)))

(defn clean-latlon [angle]
  "The purpose of this function is to verify that lat long is clean."
  (s/replace angle #"[^0-9\.\-]" ""))

(defn address->address+latlong [address]
  "Takes the :Address value from a record and splits it up. Returns a map
   containing the address and two new latitude and longitude fields."
  (let [[addr-part lat-part] (s/split address #"\n\(")
        [lat long]           (s/split lat-part #",\s")]
    {:address   addr-part
     :latitude  (clean-latlon lat)
     :longitude (clean-latlon long)}))

(defn record->restaurant [record]
  (merge (address->address+latlong (get record "Address"))
         {:facility_id (get record "Facility ID")
          :name        (get record "Restaurant Name")
          :zip         (get record "Zip Code")}))

(defn record->inspection [record]
  {:date        (get record "Inspection Date")
   :score       (get record "Score")
   :facility_id (get record "Facility ID")
   :description (get record "Process Description")})

(defn read-input-csv [filename]
  (with-open [in-file (io/reader filename)]
    (doall
     (->> (csv/read-csv in-file)
          (sc/remove-comments)
          (sc/mappify {:keyify false})))))

(defn write-csv [filename dataset]
  (with-open [writer (io/writer filename)]
    (csv/write-csv writer (sc/vectorize dataset))))

(defn split-csv [in restaurant-output inspection-output]
  (let [records     (read-input-csv in)
        restaurants (map record->restaurant records)
        inspections (map record->inspection records)]
    (write-csv restaurant-output restaurants)
    (write-csv inspection-output inspections)))
