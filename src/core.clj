(ns core
  (:use ring.adapter.jetty)
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.json :as json])
  (:gen-class)
  (:import (java.util.concurrent.atomic AtomicInteger)))

(defn batata-middleware [handler-fn]
  (fn batata-middleware' [req]
    (-> req
        (assoc "batata" "frita")
        handler-fn)))

(def times-visited (AtomicInteger. 0))

(defn visitor-middleware [handler-fn]
  (fn visitor-middleware' [req]
    (let [visitor-n (.getAndIncrement times-visited)]
      (-> req
          (assoc :visitor-n visitor-n)
          handler-fn))))

(defn handler [req]
  (let [batata-type (get req "batata")
        visitor-n (:visitor-n req)]
    {:status 200
     :body   {:data           "Hello World!"
              :batata         batata-type
              :visitor-number visitor-n}}))

(defroutes app-routes
           (GET "/" request (handler request))
           (route/not-found "bruh"))

(def app (-> app-routes
             json/wrap-json-response
             json/wrap-json-body
             batata-middleware
             visitor-middleware))

(defn -main []
  (run-jetty app {:port 3000}))
