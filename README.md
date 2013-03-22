# PdClojureBindings

Unoficial Clojure bindings for [PagerDuty](http://www.pagerduty.com) API. The docs for the API can be found at [http://developer.pagerduty.com](http://developer.pagerduty.com).

## Basic Usage

Import the bindings into your code:

    (ns com.my-company.using-pagerduty
      (:use pdclient.core))


Start by setting up your credentials

    (setup-auth {:subdomain "your-subdomain"
                 :token "your-accout-token"})

You can also use username/password:

    (setup-auth {:subdomain "your-subdomain"
                 :user "your-username"
                 :password "your-password"
                 })



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

http://github.com/danielribeiro/WebGLCraft