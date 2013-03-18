(ns pdclient.basic-helpers
  (:require [clojure.string :as string]))


(defn args-to-map [args-list]
  (if (nil? args-list) {}
    (into {} (map vec (partition 2 args-list)))))

(defn singularize [str] (subs str 0 (- (count str) 1 )))

(defn singularize-keyword "Also works for symbols" [kw]
  (->> kw name singularize keyword))

(def compact (partial remove nil?))

(defn concat-vec [coll1 coll2] (vec (concat coll1 coll2)))

(defn interleave+ [vec1 vec2]
  "Like interleave, but appends all the remaining elements to the returning vector. Always returns a vector"
  (let [ret (vec (interleave vec1 vec2))
        size1 (count vec1)
        size2 (count vec2)]
    (cond
      (< size1 size2) (concat-vec ret (subvec vec2 size1))
      (> size1 size2) (concat-vec ret (subvec vec1 size2))
      :else ret)))


(defn conj? [coll x]
  "Like conj, but will not inlcude if x is nil"
  (if (nil? x)
    coll
    (conj coll x)
    ))

(defn partition-with [pred coll]
  "Partition coll into a vec of two elements: the list of elements who who confirm to pred,
  and the list of elements that do not"
  [(filter pred coll) (remove pred coll)])

(def rest-vec (comp vec rest))

; Debugging helper. Usage: (prit + 1 2) which is simlar to (prn (+ 1 2)), but returns 3, instead of nil
(defmacro prit [& form]
  `(let [res# (~@form)]
     (prn res#)
     res#))


(defn dasherize [s] (string/replace s "_" "-"))
