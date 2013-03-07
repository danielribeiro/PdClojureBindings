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
  ((last path-list) (pdrequest :get path-list args)))

(defn pdshow [path-list & args]
  (simplify-single-result path-list (pdrequest :get path-list args)))

(defn pdcreate [path-list & args]
  ((->> path-list last singularize-keyword) (pdrequest :post path-list args)))

(defn pddelete [path-list & args]
  (pdrequest :delete path-list args))

(defn pdupdate [path-list & args]
  (simplify-single-result path-list (pdrequest :put path-list args)))


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

(defn- spec-name [route-spec]
  (let [spec (:route-spec route-spec)]
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

(defn route-specs [route]
  (map #(args-to-map [:route-spec % :route route]) (:routes route)))

; Deprecated by pd-api
(defn pd-any [method path-list & args]
  (simplify-any path-list (pdrequest method path-list args)))

(defn- get-simplify-function [route-spec]
  (if (= 'show (:route-spec route-spec))
    simplify-single-result
    (throw (IllegalStateException. "to be implemented"))
    )
  ) ;TODO
; Case of show: simplify-single-result
(defn- get-method-of [route-spec]
  (let [spec (:route-spec route-spec)]
    (if (symbol? spec)
      (base-path-method-map spec)
      (first spec))))

(defn pd-api [route-spec argslist]
  (let [simplify-fn (get-simplify-function route-spec)
        method (get-method-of route-spec)
        [ids kvs] (split-at (number-of-arguments route-spec) argslist )
        path-list (path-list-of route-spec ids)
        ]
    (simplify-fn path-list (pdrequest method path-list kvs)))
  )

(defn user [& args]
  (pd-api
    {:route-spec 'show :route {:element :users :parent nil :routes ['show 'create' 'update 'delete 'list '(get :id log_entries)]}}
    args
    )
  )

;(defn user [id & args] (apply pdshow [:users (name id)] args))
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
