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

(defn update-currency-loop [ms]
  (let [url "http://api.coindesk.com/v1/bpi/currentprice.json"
        interrupt-chan (async/chan)]
    (async/go-loop []
      (let [price-chan (kvlt.chan/request! {:url url :as :json})
            [{:keys [body status headers]} selected-chan] (async/alts!
                                                           [price-chan
                                                            interrupt-chan])
            {:keys [code rate]} (-> body (:bpi) (:USD))]
        (when (identical? selected-chan price-chan)
          (if (= status 200)
            (prn
             (reset! currency
                     {:displayName (:chartName body)
                      :symbol code
                      :dateTime (:date headers)
                      :price rate})))
          (async/<! (async/timeout ms))
          (recur))))
    interrupt-chan))

(def loop-control-chan (update-currency-loop 3000))

;(async/close! loop-control-chan)

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
       (do
         (async/close! loop-control-chan)
         (ok {})))

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

