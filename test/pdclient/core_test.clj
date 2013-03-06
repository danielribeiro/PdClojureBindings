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
)

(describe route-specs
  (spec ""
    (same [{:method :get :id-count 0 }] (routes-of {:element :incidents :parent nil :routes ['(get name)]} )))

  )

(describe "result simplification functions"
  (spec "can simplify single results"
    (same {:id 1} (simplify-single-result [:users 1] {:user {:id 1}} )))

  (spec "can simplify generic results, where the result is the last path on the path list"
    (same [{:name "a name"}] (simplify-any [:incidents 2 :log_entries] {:log_entries [{:name "a name"}] :total 4})))

  (spec "can simplify generic results, where the result only has one key"
    (same {:name "a name"} (simplify-any [:schedules :preview] {:schedule {:name "a name"}})))

  (spec" will not simplify the result if none of the above aplly"
    (same {:alerts [] :total_number_of_alerts 0} (simplify-any [:reports :alerts-per-time] {:alerts [] :total_number_of_alerts 0})))
)
