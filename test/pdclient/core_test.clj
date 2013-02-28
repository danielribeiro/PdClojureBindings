(ns pdclient.core-test
  (:use pdclient.spec-helper
        pdclient.core))



(spec "can linearize a test in a simple case"
  (same [{:element :incidents :parent nil :routes []}] (linearize [:incidents])))

; waiting for: we need to get the parent
(spec "can linearize a composite case"
  (same [{:element :incidents :parent nil :routes []} {:element :subcase :parent :element :routes []}] (linearize [:incidents [:subcase]])))

(xspec "keep arguments of the linearization"
  (same [{:element :incidents :parent nil :routes ['(get name)]}
         {:element :subcase :parent :element :routes ['(put :id subput)]}]
        (linearize [:incidents '(get name) [:subcase '(put :id subput)]])
    ))

(xspec "can define functions that ")

