(ns coinwatch.handler
  (:require [clojure.core.async :as async]
            [compojure.api.sweet :refer :all]
            [kvlt.core :as kvlt]
            [kvlt.chan :as kchan]
            [ring.util.http-response :refer :all]
            compojure.api.async))

(def current-currency (atom {}))

(defn ok-status? [req]
  (= 200 (:status req)))

(defn get-currency [req]
  {:when (-> req (:headers) (:date))
   :symbol (-> req (:body) (:bpi) (:USD) (:code))
   :price (-> req (:body) (:bpi) (:USD) (:rate))})

; investigate other kind of buffers
(defn get-currency-chan [url]
  (async/pipe
   (kvlt.chan/request! {:url url :as :json})
   (async/chan 1 (comp (filter ok-status?)
                       (map get-currency)))))

(defn update-currency-loop [ms]
  ; time out conccorrente
  (let [url "http://api.coindesk.com/v1/bpi/currentprice.json"
        poison-chan (async/chan)]
    (async/go-loop []
      (let [currency-chan (get-currency-chan url)
            [currency chosen-chan] (async/alts! [currency-chan poison-chan])]
        (when (not= chosen-chan poison-chan)
          (if (some? currency) ;; channel opened
            (prn (reset! current-currency currency)))
          (async/<! (async/timeout ms))
          (recur))))
    poison-chan))

(async/<!!
 (get-currency-chan
  "http://api.coindesk.com/v1/bpi/currentprice.json"))

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

