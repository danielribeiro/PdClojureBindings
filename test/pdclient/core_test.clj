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
  (spec "path-list-of can generate a path list out of a route and a id list"
    (same [:incidents 42 'name] (path-list-of {:route-spec '(get name) :route {:element :incidents :parent nil :routes ['(get name)]}} [42])))

  (spec "path-list-of can generate a path list out of a route and a id list, given the route has parent"
    (same [:users 42 :contact_methods 266 'name]
      (path-list-of {:route-spec '(get name) :route {:element :contact_methods :parent :users :routes ['(get name)]}} [42 266])))

  (spec "can create path-list out of basic crud route: list"
    (same [:users 23 :contact_methods]
      (path-list-of {:route-spec 'list :route {:element :contact_methods :parent :users :routes ['list]}} [23])))

  (spec "can create path-list out of basic crud route: show"
    (same [:users 23]
      (path-list-of {:route-spec 'show :route {:element :users :parent nil :routes ['show]}} [23])))

  (spec "can cout how many id arguments a route spec needs"
    (same 2 (number-of-arguments {:route-spec 'show :route {:element :contact_methods :parent :users :routes ['show]}} ))
    )
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
