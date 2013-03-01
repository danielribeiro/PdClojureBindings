(ns pdclient.demo
  (:use pdclient.core)
  )

(user-delete :PGQFWUS)
(grab (users) :id :email)
(grab (services) :status)
(user-new :role "user" :name "aguy from cljure" :email "him@example.com")
(user-update :PGQFWUS :email "notreallyhim@example.com")


(ns pdclient.demo)
(use 'pdclient.core)
(use 'clojure.pprint)
(def p pprint)

(defn pgrab [json & args]
  (p (apply grab json args))
  )

(pgrab (users) :id :email)

(user-delete :PGQFWUS)
(pgrab (users) :id :email)
(pgrab (services) :status)
(user-new :role "user" :name "aguy from cljure" :email "him@example.com")
(user-update :PGQFWUS :email "notreallyhim@example.com")
