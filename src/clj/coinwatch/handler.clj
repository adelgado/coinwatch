(ns coinwatch.handler
  "Asynchronous compojure-api application."
  (:require [clojure.core.async :as async]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [manifold.deferred :as d]
            compojure.api.async
            ))

(def currency (atom {}))

(defn update-currency! [c]
  (reset! currency c))

(def app
  (api
   {:swagger
    {:ui "/"
     :spec "/swagger.json"
     :data {:info {:title "Simple"
                   :description "Compojure Api example"}
            :tags [{:name "api", :description "some apis"}]}}}

   (context "/api" []
     :tags ["api"]

     (GET "/price" []
       :return {:displayName String
                :symbol String
                :price Long}
       ;:query-params [x :- Long, y :- Long]
       :summary "returns bitcoin price in US dollar"
       (let [chan (async/chan)]
         (future
           (async/go
             (try
               (async/>!
                chan
                (->
                 (ok {:displayName "bitcoin"
                       :symbol "BTC"
                      :price 76387462})
                 (assoc-in
                  [:headers "Access-Control-Allow-Origin"]
                  "*")
                 (assoc-in
                  [:headers "Access-Control-Allow-Methods"]
                  "GET,PUT,POST,DELETE,OPTIONS")
                 (assoc-in
                  [:headers "Access-Control-Allow-Headers"]
                  "X-Requested-With,Content-Type,Cache-Control")))
               (catch Throwable e
                 (async/>! chan e))
               (finally
                 (async/close! chan)))))
         chan)))))

