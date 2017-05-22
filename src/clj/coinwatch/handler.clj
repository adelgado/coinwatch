(ns coinwatch.handler
  (:require [clojure.core.async :as async]
            [compojure.api.sweet :refer :all]
            [kvlt.core :as kvlt]
            [kvlt.chan :as kchan]
            [ring.util.http-response :refer :all]
            compojure.api.async))

(def currency (atom {}))

(defn ok-status? [req]
  (= 200 (:status req)))

; investigate other kind of buffers
(defn get-currency-chan [url]
  (async/pipe
   (kvlt.chan/request! {:url url :as :json})
   (async/chan 1 (filter ok-status?))))


;(def d (get-currency "http://api.coindesk.com/v1/bpi/currentprice.json"))


(defn update-currency-loop [ms]
  ; time out conccorrente
  ; mover logica do fetch corpo pra cima
  (let [url "http://api.coindesk.com/v1/bpi/currentprice.json"
        poison-chan (async/chan)]
    (async/go-loop []
      (let [price-chan (get-currency-chan url)
            [{:keys [body status headers] :as req} chosen-chan]

            (async/alts!
                                                                 [price-chan
                                                                  poison-chan])]
        (when (not= chosen-chan poison-chan)
          (if (some? req)
            (prn (reset! currency
                         {:displayName (:chartName body)
                          :symbol (-> body (:bpi) (:USD) (:code))
                          :dateTime (:date headers)
                          :price (-> body (:bpi) (:USD) (:rate))})))
          (async/<! (async/timeout ms))
          (recur))))
    poison-chan))

(def loop-control-chan (update-currency-loop 3000))

(async/close! loop-control-chan)

(def app
  (api
   {:swagger
    {:ui "/"
     :spec "/swagger.json"
     :data {:info {:title "CoinWatch API Docs"
                   :description "CoinWatch API Docs"}
            :tags [{:name "api", :description "foo"}]}}}

   (context "/api" []
     :tags ["api"]

     (GET "/cancel" []
       :summary "cancel fetching"
       (do
         (async/close! loop-control-chan)
         (ok {})))

     (GET "/price" []
       :summary "gets current price in US dollar"
       :return {:displayName String
                :symbol String
                :dateTime String
                :price String}
       (async/go
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
           "X-Requested-With,Content-Type,Cache-Control")))))))

(def c (async/chan 2))

(async/go
  (loop [i 0]
    (prn ">" i)
    (async/>! c i)
    (recur (inc i))))

(async/go (async/>! c 1))

(async/<!! c)

