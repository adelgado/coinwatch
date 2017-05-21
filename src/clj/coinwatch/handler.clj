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

(defn get-currency* []
  (let [time (System/currentTimeMillis)
        url "http://api.coindesk.com/v1/bpi/currentprice.json"]
    (async/go
      (let [{:keys [status body] :as req}
            (async/<! (kvlt.chan/request! {:url url :as :json}))]
        (if (not= status 200)
          (prn "error requesting currency")
          (let [display-name (:chartName body)
                {:keys [code rate]} (-> body
                                        (:bpi)
                                        (:USD))]
            (reset! currency {:displayName display-name
                              :symbol code
                              :time time
                              :price rate})
            (prn @currency)))))))

(defn start-loop [ms-interval]
  (future
    (while true
      (do
        (Thread/sleep ms-interval)
        (get-currency*)))))

(def job (start-loop 1000))

(defn get-currency [url]
  (let [{:keys [status body] :as req}
        @(kvlt/request! {:url url :as :json})]
    (if (not= status 200)
      (prn "error requesting currency")
      (let [display-name (:chartName body)
            {:keys [code rate]} (-> body
                                    (:bpi)
                                    (:USD))]
        {:displayName display-name
         :symbol code
         :price rate}))))

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
       (ok (future-cancel job)))

     (GET "/price" []
       :return {:displayName String
                :symbol String
                :time Long
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

