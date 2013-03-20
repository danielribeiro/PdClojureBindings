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


