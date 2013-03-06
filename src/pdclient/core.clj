(ns pdclient.core
  (:require [clj-http.client])
  (:use [clojure.string :only [join]])
  )

(use 'clojure.pprint)

(defn args-to-map [args-list]
  (if (nil? args-list) {}
  (into {} (vec (map vec (partition 2 args-list))))))

(def basic-auth-credentials nil)

(defn setup-auth [map] (def basic-auth-credentials map))

(defn auth [k]
    (if basic-auth-credentials
      (basic-auth-credentials k)
      (throw (IllegalStateException. "Please call setup-auth with the auth args before using PagerDuty API. Example:
(setup-auth {:subdomain \"your-subdomain\"
  :user \"your-username\"
  :password \"your-password\"})")))
  )

(defn set-params [method req-map params-map]
  (let [extra-key (if (= method :get) :query-params :form-params)]
    (assoc req-map extra-key params-map)))

(defn pdrequest [method path-list & args]
  (:body ((resolve (symbol "clj-http.client" (name method)))
           (str "https://" (auth :subdomain) ".pagerduty.com/api/v1/" (join "/" (map name path-list)) )
    (set-params method {:basic-auth [(auth :user) (auth :password)]
     :content-type :json
     :accept :json
     :as :json} (args-to-map args))
    )))

(defn singularize [str] (subs str 0 (- (count str) 1 )))

(defn singularize-keyword [kw]
  (->> kw name singularize keyword))

(defn simplify-single-result [path-list json]
  (let [penultimate (nth (reverse path-list) 1)
        singular-keyword (singularize-keyword penultimate)]
    (singular-keyword json)))


(defn simplify-any [path-list json]
  (if (= (count json) 1)
    (first (vals json))
    (or ((last path-list) json) json)))


(defn pdlist
  "usage example: (pdlist [:users])"
  [path-list & args]
  ((last path-list) (apply pdrequest :get path-list args)))

(defn pdshow [path-list & args]
  (simplify-single-result path-list (apply pdrequest :get path-list args)))

(defn pdcreate [path-list & args]
  ((->> path-list last singularize-keyword) (apply pdrequest :post path-list args)))

(defn pddelete [path-list & args]
  (apply pdrequest :delete path-list args))

(defn pdupdate [path-list & args]
  (simplify-single-result path-list (apply pdrequest :put path-list args)))


(defn pd-any [method path-list & args]
  (simplify-any path-list (apply pdrequest method path-list args)))

;(defn pd-req [method route simplify-fn & ids]
;  (simplify-fn )
;  )

(defn path-list-of [routespec])


(defn user [id & args] (apply pdshow [:users (name id)] args))
(defn users [& args] (apply pdlist [:users] args))
(defn incidents [& args] (apply pdlist [:incidents] args))
(defn schedules [& args] (apply pdlist [:schedules] args))
(defn services [& args] (apply pdlist [:services] args))
(defn maintenance_windows [& args] (apply pdlist [:maintenance_windows] args))
(defn user-new [& args] (apply pdcreate [:users] args))
(defn user-delete [id & args] (apply pddelete [:users (name id)] args))
(defn user-update [id & args] (apply pdupdate [:users (name id)] args))

(defn grab
  "Helper from grabing a few keys from json output. Works if json is an array or an object
  Example:
   (grab (users) :name :id :email)
   (grab (user \"PY8J5YX\") :email )
  "
  [json & args]
  (if (map? json)
    (select-keys json args)
    (map #(select-keys % args) json)
    ))

;(distinct (map :status (get-in i [:body :incidents])))
;(get-in i [:body :limit])
;(pprint (:body i))
;
;
;;crud is equal to [(get nil) (get :id nil) (post nil) (put :id nil) (delete :id nil)]
;;which equals to [list show create update delete]
(def pd
  '(
     [incidents list update show (get count) (get :id log_entries)]
     [alerts list]
     [reports (get alerts_per_time) (get incidents_per_time)]
     [schedules crud (get :id users ) (post preview) (get :id entries)
      [overrides list create delete]]
     [users crud (get :id log_entries)
      [contact_methods crud]
      [notification_rules crud]]
     [log_entries list show]
     [services crud (put :id disable) (put :id enable) (post :id regenerate_key)
        [email-filters create update delete]]
     [maintenance_windows crud]
   )

  )
;
;
;(def iss (first '([incidents list [another-sub crud] update show (get count) [sub iss] (get :id log_entries)])))
;

(defn partition-with [pred coll]
  [(filter pred coll) (remove pred coll)])

(defn complex? [expr] (some vector? expr))

(defn dsl-node [element parent routes]
  {:element element :parent parent :routes routes}
  )

(def rest-vec (comp vec rest))

; Helper function for parsing the dsl above on def pd
(defn linearize
  ([expr] (linearize expr nil) )
  ([expr parent]
    (if (complex? expr)
      (let [[subtress finalexpression] (partition-with vector? expr)
            self (first finalexpression)]
        (cons (dsl-node self parent (rest-vec finalexpression)) (mapcat #(linearize % self) subtress)))
      [(dsl-node (first expr) parent (rest-vec expr) )])
    )
  )


;(defmacro defineall [args]
;  (cons `do
;    (map (fn [a]
;           `(defn ~(symbol (str "x" a)) [] (println ~(str a)))) (map first args)
;
;      ))
;  )
;
;
;(macroexpand '(defineall ([alerts list]
;                            [reports (get alerts_per_time) (get incidents_per_time)]) ))
