(ns pdclient.demo
  (:use pdclient.core)
  )

(user-delete :PGQFWUS)
(grab (users) :id :email)
(grab (services) :status)
(user-new :role "user" :name "aguy from cljure" :email "him@example.com")
(user-update :PGQFWUS :email "notreallyhim@example.com")
