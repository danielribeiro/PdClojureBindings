(ns pdclient.demo
  (:use pdclient.core clojure.pprint)
  (:require [clj-http.client])
  (:require [cheshire.core :refer :all]))


(setup-auth {:subdomain "your-subdomain"
             :token "your-accout-token"
             })

; or
;(setup-auth {:subdomain "your-subdomain"
;             :user "your-username"
;             :password "your-password"
;             })


(defn print-all-data []
  (do
    (println "All my 'recent' pagerduty data is:")
    (puts
      (users)
      (incidents)
      (alerts :since "2013-03-01" :until "2013-03-15")
      (schedules)
      (reports-alerts-per-time :since "2013-03-01" :until "2013-03-15")
      (reports-incidents-per-time :since "2013-03-01" :until "2013-03-15" )
      (services)
      (log-entries))))

(defn getting-nested-resources []
  (let [service (first (schedules))]
    (println "The overrides for the first schedule:")
    (puts (overrides service :since "2013-03-01" :until "2013-03-15"))))

(defn paginating [] (puts (incidents :limit 3 :offset 0)))

(defn -main [& args]
  (do
    (print-all-data)
    (getting-nested-resources)
    (paginating)
      ))
