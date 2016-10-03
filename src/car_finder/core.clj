(ns car-finder.core
  (:require [car-finder.parkers :as parkers]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:gen-class))

(def YEARS #{2010 2011 2012 2013 2014})

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
       (map (partial apply parkers/get-model-specs))
       flatten
       write-csv
       ))

(defn fetch-all-models []
  (->> (parkers/get-manufacturers)
      (map parkers/get-models)
      flatten
      (map #(parkers/get-model-specs (list %1) YEARS))
      flatten
      write-csv
      ))

(defn -main
  "Takes a file containing a list of manufacturer, model, years and looks up the details"
  [& args]
  (if (empty? args) (fetch-all-models) (fetch-specific-models (first args)))
  )
