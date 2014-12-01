# PdClojureBindings

Unofficial Clojure bindings for [PagerDuty's](http://www.pagerduty.com) API.

The docs for the API can be found at [http://developer.pagerduty.com](http://developer.pagerduty.com).

[![Continuous Integration status](https://travis-ci.org/danielribeiro/PdClojureBindings.png?branch=master)](https://travis-ci.org/danielribeiro/PdClojureBindings)


# Installing

pdclient is available as a Maven artifact from [Clojars](https://clojars.org/pdclient) [![Clojars Project](http://clojars.org/pdclient/latest-version.svg)](http://clojars.org/pdclient)

-```clojure
-[pdclient "0.1.3"]
-```

## Basic Usage

*Note:* The following code is in runnable form on [demo.clj](https://github.com/danielribeiro/PdClojureBindings/blob/master/src/pdclient/demo.clj)

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

*Quick Note* Objects are returned as *unboxed* clojure data. For instance
```clojure
=> (service :PB0PZRH)

{:status "active",
 :name "Api Service",
 :email_filter_mode "all-email",
 :last_incident_timestamp "2013-03-21T17:38:26-04:00",
 :service_url "/services/PB0PZRH",
 :created_at "2013-02-27T19:42:09-05:00",
 :service_key "409a1ef79fbe453f9b0d488b00533e3a",
 :incident_counts {:triggered 0, :acknowledged 0, :resolved 40, :total 40},
 :acknowledgement_timeout 1800,
 :type "generic_events_api",
 :email_incident_creation nil,
 :id "PB0PZRH",
 :auto_resolve_timeout 14400}
```

Note that the service is not wrapped in a map with only the service key.

Getting nested resources:

```clojure
 (let [service (first (schedules))]
    (println "The overrides for the first schedule:")
    (prn (overrides service :since "2013-03-01" :until "2013-03-15")))
```

Pagination works just as in the REST api:

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
(let [new-contact-method
      (contact-method-new userid :contact_method {:type :email :address "rich_hickey@example.com"} )]
  (prn new-contact-method)
  (println "Updating the contact method")
  (contact-method-update userid new-contact-method :contact_method {:address "not_rich_hickey@example.com" })
  (println "Deleting the contact method")
  (prn (contact-method-delete userid new-contact-method))))
```

*Note:* anytime you are passing an id, you can also pass an map with the :id key. Which is why in this example we can write

```clojure
(contact-method-update userid new-contact-method :contact_method {:address "not_rich_hickey@example.com" })
```

instead of:

```clojure
(contact-method-update userid (new-contact-method :id) :contact_method {:address "not_rich_hickey@example.com" })
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

The following functions are on the pdclient.core namespace (docs on how to use them are found [here](http://developer.pagerduty.com)):

```clojure
incidents
incident-update
incident
incidents-count
incident-log-entries
notes
note-new
escalation-policie-update
escalation-policie-new
escalation-policie
escalation-policie-delete
escalation-policies
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
```

## Meta

Created by [Daniel Ribeiro](http://metaphysicaldeveloper.wordpress.com/about-me).

Released under the MIT License: http://www.opensource.org/licenses/mit-license.php

http://github.com/danielribeiro/PdClojureBindings
