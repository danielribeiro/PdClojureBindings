(ns pdclient.demo
  (:use pdclient.core)
  (:require [clj-http.client])
  (:require [cheshire.core :refer :all])
  )

(setup-auth {:subdomain "your-subdomain"
             :user "your-username"
             :password "your-password"
             })
(use 'clojure.pprint)
(defn print-it [e]
  (let [o (:object (.getData e ))]
    (prn (:status o) (parse-string (o :body) true))
    )
  )

;(try
;  (grab (users) :id :email)
;  (catch Exception e (print-it e) )
;)

;(user-delete :PGQFWUS)
;(grab (users) :id :email)
;(grab (services) :status)
;(user-new :role "user" :name "aguy from cljure" :email "him@example.com")
;(user-update :PGQFWUS :email "notreallyhim@example.com")
;
;
;(ns pdclient.demo)
;(use 'pdclient.core)
;(use 'clojure.pprint)
(def p pprint)
;
(defn pgrab [json & args]
  (p (apply grab json args))
  )
;
(pgrab (users) :id :email)
;
;(user-delete :PGQFWUS)
;(pgrab (users) :id :email)
;(pgrab (services) :status)
;(user-new :role "user" :name "aguy from cljure" :email "him@example.com")
;(user-update :PGQFWUS :email "notreallyhim@example.com")
;)
