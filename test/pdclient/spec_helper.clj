(ns pdclient.spec-helper
  (:require  [clojure.string :as st])
  (:use clojure.test))

; Test Helpers
(defn same [x y] (is (= x y)))



(defn name-to-symbol [name]
  (-> name
    st/lower-case
    (st/replace #"\W" "-")
    (st/replace #"-+" "-")
    (st/replace #"-$" "")
    symbol))

(defn- arg-to-symbol [arg]
  (if (symbol? arg) arg
    (name-to-symbol arg)))

; Source: https://gist.github.com/mybuddymichael/4425558
(defmacro spec
  [name & body]
  `(deftest ~(arg-to-symbol name) ~@body))
; ignoring specs
(defmacro xspec [name-string & body])

(defmacro describe [name & body] `(do ~@body))
