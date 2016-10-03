(ns car-finder.parkers
  (:require [net.cgrand.enlive-html :as html]
            [clojure.set :refer :all]
            [clojure.string :as string])
  (:import (java.net URL)))

(defn get-page
  ([manufacturer model]
    (get-page (format "/%s/%s/specs/" manufacturer model))
     )
  ([path]
    (println path)
    (html/html-resource
      (URL. (str "http://www.parkers.co.uk" path))))
  )

(defn convert-link-to-spec [link]
  (println link)
  (let [content (re-find #"^(.+) \((\d+) -? ?(\d*).*\) Specifications$" (first (:content link)))
        start (Integer/parseInt (nth content 2))
        end (if (empty? (last content)) 2018 (Integer/parseInt (last content)))]
    {:name (second content) :years (set (range start (inc end))) :link (get-in link [:attrs :href])}))

(defn remove-invalid-headers [tags]
  (filter #(nil? (get-in %1 [:attrs :colspan])) tags))

(defn to-map [details]
  (println details)
  (if (empty? details) {"Seats" "0"} (apply hash-map (conj details "Name"))))

(defn convert-link-to-details [link]
  (to-map (-> link
              (get-in [:attrs :href])
              get-page
              (html/select #{[:div.main-heading__wrapper :h1]
                             [:section.specs-detail-page__section :th]
                             [:section.specs-detail-page__section :td]})
              remove-invalid-headers
              html/texts
              )))

(defn get-available-engines-for-spec [spec]
  (apply hash-map (html/select (get-page (:link spec)) [:table.specs-table :tbody :tr])))

(defn get-full-specs [engine-spec]
  (map convert-link-to-details (html/select (second engine-spec) [:a])))

(defn acceleration-and-efficiency [spec]
  (let [values (map #(->> %1 html/text string/trim)  (html/select (first spec) [:td]))]
    (println values)
    (and
      (< (Float/parseFloat (re-find #"\d+" (str (nth values 3) "+11"))) 11)
      (>= (Float/parseFloat (re-find #"\d+" (str (nth values 4) "+44"))) 45)
      )
    )
  )

(defn manual-seven-seaters [details]
  (and
    (< 5 (Integer/parseInt (details "Seats")))
    (= (details "Transmission") "Manual")
    ))

(defn years-overlap [years spec]
  (println spec)
  (not-empty (intersection years (spec :years))))

(defn get-model-specs
  ([manufacturer model years]
    (get-model-specs (html/select (get-page manufacturer model) [:a.panel__primary-link]) years))
  ([links years]
    (->> links
         (map convert-link-to-spec)
         (filter (partial years-overlap years))
         (map get-available-engines-for-spec)
         (into {})
         (filter acceleration-and-efficiency)
         get-full-specs
         (filter manual-seven-seaters)
         )
    ))

(defn get-models [path]
  (html/select (get-page path) #{[:h4 :a] [:h5 :a]}))

(defn get-manufacturers []
  (map #(get-in %1 [:attrs :href]) (html/select (get-page "/car-specs/select-manufacturer/") [:a.panel__primary-link])))




