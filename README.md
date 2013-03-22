# PdClojureBindings

Unoficial Clojure bindings for [PagerDuty](http://www.pagerduty.com) API. The docs for the API can be found at [http://developer.pagerduty.com](http://developer.pagerduty.com).

## Basic Usage

Import the bindings into your code:

```clojure
(ns com.my-company.using-pagerduty
    (:use pdclient.core))
```


Start by setting up your credentials:

```clojure
(setup-auth {:subdomain "your-subdomain"
             :token "your-accout-token"})
 ```

You can also use username/password:

```clojure
(setup-auth {:subdomain "your-subdomain"
             :user "your-username"
             :password "your-password"
             })
 ```

Reading all your recent top level data from PagerDuty:

```clojure
(do
    (prn
      (users)
      (incidents)
      (alerts :since "2013-03-01" :until "2013-03-15")
      (schedules)
      (reports-alerts-per-time :since "2013-03-01" :until "2013-03-15")
      (reports-incidents-per-time :since "2013-03-01" :until "2013-03-15" )
      (services)
      (log-entries)))
```

Getting nested resources:

```clojure
 (let [service (first (schedules))]
    (println "The overrides for the first schedule:")
    (prn (overrides service :since "2013-03-01" :until "2013-03-15")))
```

Pagination works jsut as in the REST api:

```clojure
(incidents :limit 3 :offset 0)
```

Basic show, list, create, update and delete operations:

```clojure
(let [userid (first (users))
    first-contact-method (first (contact-methods userid))
    ]
(println "Operating on contact methods")
(println "All contact methods:")
(prn  (contact-methods userid))
(println "The details of the first one")
(prn (contact-method userid first-contact-method))
(println "Creating a new contact method")
(let [new-contact-method (contact-method-new userid :contact_method {:type :email :address "rich_hickey@example.com"} )]
  (prn new-contact-method)
  (println "Updating the contact method")
  (contact-method-update userid new-contact-method :contact_method {:address "not_rich_hickey@example.com" })
  (println "Deleting the contact method")
  (prn (contact-method-delete userid new-contact-method))))
```

Using Events API (for incident creation):

```clojure
(do
    (println "Creating an event")
    (let [new-event (event-trigger :service_key service-key :description "clojure really rocks")]
      (prn new-event)
      (println "Acknowledging the event")
      (prn (event-ack  :service_key service-key :incident_key new-event))
      (println "Resolving the event")
      (prn (event-resolve :service_key service-key :incident_key new-event))))
```

## PagerDuty Functions

The following funcitons are on the pdclient.core namespace (docs on how to use them are found [here](http://developer.pagerduty.com)):

    incidents
    incident-update
    incident
    incidents-count
    incident-log-entries
    alerts
    reports-alerts-per-time
    reports-incidents-per-time
    schedule-update
    schedule-new
    schedule
    schedule-delete
    schedules
    schedule-users
    schedules-preview
    schedule-entries
    overrides
    override-new
    override-delete
    user-update
    user-new
    user
    user-delete
    users
    user-log-entries
    contact-method-update
    contact-method-new
    contact-method
    contact-method-delete
    contact-methods
    notification-rule-update
    notification-rule-new
    notification-rule
    notification-rule-delete
    notification-rules
    log-entries
    log-entry
    service-update
    service-new
    service
    service-delete
    services
    service-disable
    service-enable
    service-regenerate-key
    email-filter-new
    email-filter-update
    email-filter-delete
    maintenance-window-update
    maintenance-window-new
    maintenance-window
    maintenance-window-delete
    maintenance-windows
    event-trigger
    event-ack
    event-resolve


## Meta

Created by Daniel Ribeiro.

Released under the MIT License: http://www.opensource.org/licenses/mit-license.php

http://github.com/danielribeiro/PdClojureBindings