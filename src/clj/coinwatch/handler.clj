(ns coinwatch.handler
  (:require [clojure.core.async :as async]
            [compojure.api.sweet :refer :all]
            [kvlt.core :as kvlt]
            [coinwatch.cfg :refer [cfg]]
            [kvlt.chan :as kchan]
            [ring.util.http-response :refer :all]
            compojure.api.async))

(def bitcoin (atom {}))

(def currency-loop-poison (async/chan 1))

(defn start-currency-loop!
  [poison loop-interval-ms url currency-atom transducer]
  (async/go-loop []
    (let [[currency chosen] (async/alts!
                             [poison
                              (async/pipe
                               (kvlt.chan/request! {:url url :as :json})
                               (async/chan 1 transducer))])]
      (when (not= chosen poison)
        (if (some? currency)
          (prn (reset! currency-atom currency)))
        (async/<! (async/timeout loop-interval-ms))
        (recur)))))

(start-currency-loop!
 currency-loop-poison
 (:timeout cfg)
 (:coindesk-bitcoin-url cfg)
 bitcoin
 (comp (filter #(or (= 200 (:status %))
                    (prn "error fecthing currency for: "
                         (-> % (:body) (:chartName)))))
       (map #(do
               {:when (-> % (:headers) (:date))
                :chart-name (-> % (:body) (:chartName))
                :symbol (-> % (:body) (:bpi) (:USD) (:code))
                :price (-> % (:body) (:bpi) (:USD) (:rate))}))))

(def app
  (api
   {:swagger
    {:ui "/"
     :spec "/swagger.json"
     :data {:info {:title "CoinWatch API Docs"
                   :description "CoinWatch API Docs"}
            :tags [{:name "CoinWatch API"}]}}}

   (context "/api" []
     :tags ["api"]

     (DELETE "/price/loop" []
       :summary "cancel fetching"
       (do
         (async/close! currency-loop-poison)
         (ok)))

     (GET "/price" []
       :summary "gets current price in US dollar"
       :return {:display-name String
                :symbol String
                :when String
                :price String}
       (async/go
         (->
          (ok @bitcoin)
          (assoc-in
           [:headers "Access-Control-Allow-Origin"]
           "*")
          (assoc-in
           [:headers "Access-Control-Allow-Methods"]
           "GET,PUT,POST,DELETE,OPTIONS")
          (assoc-in
           [:headers "Access-Control-Allow-Headers"]
           "X-Requested-With,Content-Type,Cache-Control")))))))
