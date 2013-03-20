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
    (prn "All my 'recent' pagerduty data is:")
    (puts
      (users)
      (incidents)
      (alerts :since "2013-03-01" :until "2013-03-15")
      (schedules)
      (reports-alerts-per-time :since "2013-03-01" :until "2013-03-15")
      (reports-incidents-per-time :since "2013-03-01" :until "2013-03-15" )
      (services)
      (log-entries))))


(defn -main [& args]
  (do
    (print-all-data)
      ))
