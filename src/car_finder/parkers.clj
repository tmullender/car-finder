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
              ))
  )

(defn convert-engines-to-map [engine links]
  (let [values (map #(->> %1 html/text string/trim)  (html/select engine [:td]))]
  [(zipmap ["+" :engine :power :0-60mph :fuel_economy :insurance_group :road_tax :length] values) (html/select links [:a])]))

(defn get-available-engines-for-model [spec]
  (let [engines (html/select (get-page (:link spec)) [:table.specs-table :tbody :tr.specs-table__engine])
        links (html/select (get-page (:link spec)) [:table.specs-table :tbody :tr.specs-table__derivatives])]
    (map convert-engines-to-map engines links)
    )
  )

(defn get-full-specs [engine-spec]
  (map convert-link-to-details (second engine-spec)))


(defn fetch-model-specs
  ([manufacturer model filters]
    (fetch-model-specs (html/select (get-page manufacturer model) [:a.panel__primary-link]) filters))
  ([links [model-filter engine-filter spec-filter]]
    (->> links
         (map convert-link-to-spec)
         (filter model-filter)
         (map get-available-engines-for-model)
         (into {})
         (filter engine-filter)
         get-full-specs
         (filter spec-filter)
         )
    ))

(defn get-models [path]
  (html/select (get-page path) #{[:h4 :a] [:h5 :a]}))

(defn get-manufacturers []
  (map #(get-in %1 [:attrs :href]) (html/select (get-page "/car-specs/select-manufacturer/") [:a.panel__primary-link])))




