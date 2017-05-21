(ns coinwatch.handler
  "Asynchronous compojure-api application."
  (:require [clojure.core.async :as async]
            [compojure.api.sweet :refer :all]
            [kvlt.core :as kvlt]
            [kvlt.chan :as kchan]
            [ring.util.http-response :refer :all]
            [manifold.deferred :as d]
            compojure.api.async))

(def currency (atom {}))

(def continue-loop? (atom true))

(defn update-currency-loop [ms]
  (let [url "http://api.coindesk.com/v1/bpi/currentprice.json"]
    (async/go-loop []
      (let [{:keys [body status headers]} (async/<!
                                           (kvlt.chan/request!
                                            {:url url :as :json}))
            {:keys [code rate]} (-> body (:bpi) (:USD))]
        (when (= status 200)
          (reset! currency
                  {:displayName (:chartName body)
                   :symbol code
                   :dateTime (:date headers)
                   :price rate})
          (prn @currency))
        (async/<! (async/timeout ms))
        (if @continue-loop?
          (recur))))))

(update-currency-loop 1000)
;@currency
;(reset! continue-loop? false)


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

     (GET "/cancel" []
       :summary "cancel fetching"
       (ok (reset! continue-loop? false)))

     (GET "/price" []
       :return {:displayName String
                :symbol String
                :dateTime String
                ;:price Long}
                :price String}
       ;:query-params [x :- Long, y :- Long]
       :summary "returns bitcoin price in US dollar"
       (let [chan (async/chan)]
         (future
           (async/go
             (try
               (async/>!
                chan
                (->
                 (ok @currency)
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

