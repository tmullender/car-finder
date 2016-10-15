(ns car-finder.parkers-test
  (:require [car-finder.parkers :refer [fetch-model-specs get-models]]
            [car-finder.core :refer [years-overlap acceleration-and-efficiency manual-seven-seaters]]
            [midje.sweet :refer :all]
            [net.cgrand.enlive-html :as html])
  (:import (java.io File)))

(unfinished get-page)

(def filters [years-overlap acceleration-and-efficiency manual-seven-seaters])

(defn my-get-page [path]
  (html/html-resource (File. (str "resources" path))))

(fact "Getting specs for a manufacturer"
      '({} {} {}) => (fetch-model-specs filters (get-models "/peugeot/specs"))
      (provided (car-finder.parkers/get-page anything) => (my-get-page "/peugeot/specs")
                ;(car-finder.parkers/get-page "/peugeot/5008/specs") => (my-get-page "/peugeot/5008/specs")
                ;(car-finder.parkers/get-page "/peugeot/5008/estate-2010/12-puretech-access-5d/specs") => (my-get-page "/peugeot/5008/estate-2010/12-puretech-access-5d/specs")
                )
      )
