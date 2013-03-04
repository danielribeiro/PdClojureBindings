(ns pdclient.core-test
  (:use pdclient.spec-helper
        pdclient.core
        clojure.test
        ))

(describe linearization
  (spec "can linearize a test in a simple case"
    (same [{:element :incidents :parent nil :routes []}] (linearize [:incidents])))

  (spec "can linearize a composite case"
    (same [{:element :incidents :parent nil :routes []} {:element :subcase :parent :incidents :routes []}] (linearize [:incidents [:subcase]])))

  (spec "keep arguments of the linearization"
    (same [{:element :incidents :parent nil :routes ['(get name)]}
           {:element :subcase :parent :incidents :routes ['(put :id subput)]}]
          (linearize [:incidents '(get name) [:subcase '(put :id subput)]])
      ))

  (spec "can handle crud routes"
    (same [{:element :incidents :parent nil :routes [:crud]}] (linearize [:incidents :crud])))
)

(describe "result simplification functions"
  (spec "can simplify single results"
    (same {:id 1} (simplify-single-result [:users 1] {:user {:id 1}} ))
    )

  (spec "can simplify generic results"
    (same [{:name "a name"}] (simplify-any [:incidents 2 :log_entries] {:log_entries [{:name "a name"}]}) )
    )
)


