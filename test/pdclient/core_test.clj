(ns pdclient.core-test
  (:use pdclient.spec-helper
        pdclient.core))

(spec "can linearize a test in a simple case"
  (same [[:incidents]] (linearize [:incidents])))

(spec "can linearize a composite case"
  (same [[:incidents] [:subcase]] (linearize [:incidents [:subcase]])))

(spec "keep arguments of the linearization"
  (same [[:incidents '(get name)] [:subcase '(put :id subput)]]
        (linearize [:incidents '(get name) [:subcase '(put :id subput)]])
    ))

(spec "can define functions that ")

