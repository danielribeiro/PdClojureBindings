(ns pdclient.spec-helper
  (:use clojure.test))

; Test Helpers
(defn same [x y] (is (= x y)))

(defn- name-to-symbol [name]
  (-> name
    clojure.string/lower-case
    (clojure.string/replace #"\W" "-")
    (clojure.string/replace #"-+" "-")
    (clojure.string/replace #"-$" "")
    symbol)
  )

; Source: https://gist.github.com/mybuddymichael/4425558
(defmacro spec
  [name-string & body]
    `(clojure.test/deftest ~(name-to-symbol name-string) ~@body))
; ignoring specs
(defmacro xspec [name-string & body])

(defmacro describe [name & body] `(deftest ~name ~@body))
