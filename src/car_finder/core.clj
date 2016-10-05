(ns car-finder.core
  (:require [car-finder.parkers :as parkers]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:gen-class))

(def YEARS #{2010 2011 2012 2013 2014})

(defn acceleration-and-efficiency [spec]
  (let [
        ]
    (and
      (< (Float/parseFloat (re-find #"\d+" (str (spec :0-60mph) "+11"))) 11)
      (>= (Float/parseFloat (re-find #"\d+" (str (spec :fuel_economy) "+44"))) 45)
      )
    )
  )

(defn manual-seven-seaters [details]
  (and
    (< 5 (Integer/parseInt (details "Seats")))
    (= (details "Transmission") "Manual")
    ))

(defn years-overlap [spec]
  (not-empty (intersection YEARS (spec "Years"))))

(defn write-csv [details]
  (let [headers (keys (first details))
        data (map #(map %1 headers) details)]
    (with-open [out-file (io/writer "out-file.csv")]
      (csv/write-csv out-file (conj data headers))
      )))

(defn fetch-specific-models [file]
  (->> file
       slurp
       read-string
       (map (partial apply parkers/fetch-model-specs [years-overlap acceleration-and-efficiency manual-seven-seaters]))
       flatten
       write-csv
       ))

(defn fetch-all-models []
  (->> (parkers/get-manufacturers)
      (map parkers/get-models)
      flatten
      (map (partial parkers/fetch-model-specs [years-overlap acceleration-and-efficiency manual-seven-seaters]))
      flatten
      write-csv
      ))

(defn -main
  "Takes a file containing a list of manufacturer, model, years and looks up the details"
  [& args]
  (if (empty? args) (fetch-all-models) (fetch-specific-models (first args)))
  )
