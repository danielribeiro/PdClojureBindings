(ns pdclient.demo
  (:use pdclient.core clojure.pprint)
  (:require [clj-http.client]))


; CHANGE THIS:
(setup-auth {:subdomain "your-subdomain"
             :token "your-accout-token"
             })

; or
;(setup-auth {:subdomain "your-subdomain"
;             :user "your-username"
;             :password "your-password"
;             })


; CHANGE THIS TOO:
(def service-key "one-of-your-service-keys" )


(defn puts [& values]
  (doseq [value values]
    (pprint value)
    (newline)
    (newline)))

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

(defn crud-operations []
  (let [userid (first (users))
        first-contact-method (first (contact-methods userid))
        ]
    (println "Operating on contact methods")
    (println "All contact methods:")
    (puts  (contact-methods userid))
    (println "The details of the first one")
    (puts (contact-method userid first-contact-method))
    (println "Creating a new contact method")
    (let [new-contact-method (contact-method-new userid :contact_method {:type :email :address "rich_hickey@example.com"} )]
      (puts new-contact-method)
      (println "Updating the contact method")
      (contact-method-update userid new-contact-method :contact_method {:address "not_rich_hickey@example.com" })
      (println "Deleting the contact method")
      (puts (contact-method-delete userid new-contact-method)))))


(defn events-api []
  (do
    (println "Creating an event")
    (let [new-event (event-trigger :service_key service-key :description "clojure really rocks")]
      (puts new-event)
      (println "Acknowledging the event")
      (puts (event-ack  :service_key service-key :incident_key new-event))
      (println "Resolving the event")
      (puts (event-resolve :service_key service-key :incident_key new-event)))))


(defn -main [& args]
  (do
    (print-all-data)
    (getting-nested-resources)
    (paginating)
    (crud-operations)
    (events-api)))
