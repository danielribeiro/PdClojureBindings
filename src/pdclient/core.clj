(ns pdclient.core
  (:require [clj-http.client])
  (:use [clojure.string :only [join]])
  )

(use 'clojure.pprint)

(defn args-to-map [args-list]
  (if (nil? args-list) {}
  (into {} (mapv vec (partition 2 args-list)))))

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

(defn pdrequest [method path-list args]
  (:body ((resolve (symbol "clj-http.client" (name method)))
           (str "https://" (auth :subdomain) ".pagerduty.com/api/v1/" (join "/" (map name path-list)) )
    (set-params method {:basic-auth [(auth :user) (auth :password)]
     :content-type :json
     :accept :json
     :as :json} (args-to-map args))
    )))

(defn singularize [str] (subs str 0 (- (count str) 1 )))

(defn singularize-keyword "Also works for symbols" [kw]
  (->> kw name singularize keyword))

(defn simplify-single-result [path-list json]
  (let [penultimate (nth (reverse path-list) 1)
        singular-keyword (singularize-keyword penultimate)]
    (singular-keyword json)))


(defn simplify-any [path-list json]
  (if (= (count json) 1)
    (first (vals json))
    (or ((last path-list) json) json)))

(defn simplitfy-list [path-list json]
   ((last path-list) json))

(defn simplitfy-create [path-list json]
  ((->> path-list last singularize-keyword) json)
  )

(def simplify-case
    {'list simplitfy-list
     'show simplify-single-result
     'create simplitfy-create
     'update simplify-single-result
     'delete (constantly nil)})

(def compact (partial remove nil?))

(defn- parent-list [route]  (compact [(:parent route) (:element route)]))

(defn- concat-vec [coll1 coll2] (vec (concat coll1 coll2)))

(defn- interleave+ [vec1 vec2]
  "Like interleave, but appends all the remaining elements to the returning vector. Always returns a vector"
  (let [ret (vec (interleave vec1 vec2))
        size1 (count vec1)
        size2 (count vec2)]
    (cond
      (< size1 size2) (concat-vec ret (subvec vec2 size1))
      (> size1 size2) (concat-vec ret (subvec vec1 size2))
      :else ret
      )))

(defn- spec-name [routespec]
  (let [spec (:route-spec routespec)]
    (if (symbol? spec)
      nil
      (last spec)))
  )

(defn- conj? [coll x]
  (if (nil? x)
    coll
    (conj coll x)
    ))

(defn path-list-of [routespec idlist]
  (let [parents (->> routespec :route parent-list vec)]
    (interleave+ (conj? parents (spec-name routespec)) idlist)
    ))


(defn number-of-arguments [routespec]
  (let [route (:route-spec routespec)
        has-id? (or (#{'show 'update 'delete} route)
                    (and (seq? route) (= (count route) 3)))]
    (count (filter boolean [has-id? (->> routespec :route :parent)]))))

(def base-path-method-map
  {'list 'get
   'show 'get
   'create 'post
   'update 'put
   'delete 'delete})

(def crud-routes (keys base-path-method-map))

(defn- extract-routes [route spec]
  (if (= 'crud spec)
    (map #(args-to-map [:route-spec % :route route]) crud-routes)
    [(args-to-map [:route-spec spec :route route])]
  ))


(defn route-specs [route] (mapcat (partial extract-routes route) (:routes route)))

(defn- get-simplify-function [routespec]
  (if (list? (:route-spec routespec))
    simplify-any
    (simplify-case (:route-spec routespec))
    )
  )

(defn- get-method-of [routespec]
  (let [spec (:route-spec routespec)]
    (if (symbol? spec)
      (base-path-method-map spec)
      (first spec))))

(defn pd-api [routespec argslist]
  (let [simplify-fn (get-simplify-function routespec)
        method (get-method-of routespec)
        [ids kvs] (split-at (number-of-arguments routespec) argslist )
        path-list (path-list-of routespec ids)
        ]
    (simplify-fn path-list (pdrequest method path-list kvs)))
  )

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


(defn- symbol-route-to-function-name [routespec]
  (let [base-path-suffix {'list "s"
         'show ""
         'create "-new"
         'update "-update"
         'delete "-delete"}
         base (->> routespec :route :element name singularize)
        suffix (->> routespec :route-spec base-path-suffix)]
    (symbol (str base suffix))))

(defn- any-route-to-function-name [routespec]
  (let [base (->> routespec :route :element name singularize)
        suffix (->> routespec :route-spec last)
        plural-str (if (= (->> routespec :route-spec count) 3) "" "s")
        ]
    (symbol (str base plural-str "-" suffix))))

(defn- route-to-function-name [routespec]
  (if (list? (:route-spec routespec))
    (any-route-to-function-name routespec)
    (symbol-route-to-function-name routespec)
    ))


(defmacro define-pd-api [routespec]
  `(defn ~(route-to-function-name routespec) [& args#] (pd-api (quote ~routespec) args#))
  )




;; doc helper.
;(defn printroutes []
;  (let [vars (mapcat route-specs (mapcat linearize pd))]
;    (doseq [x vars]  (println (route-to-function-name x)) (prn x))
;  ))

; Create the methods

;(defmacro defineall [form]
;  (cons `do
;    (map (fn [routespec]
;           `(define-pd-api ~routespec))
;      (mapcat route-specs (mapcat linearize (quote form)))
;
;      )
;  ) )

;(defmacro defineall [form]
;     (def pd (mapcat route-specs (mapcat linearize (quote ~form)))))

(defmacro defineall [form]
  `(def pd (quote ~form)))


(defineall (
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
             ) )



; precursor of the creator of all functions


(prn pd)
