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

;(defn get-currency [url currency]
;  (async/go
;    (let [{:keys [status body] :as req}
;          (async/<! (kvlt.chan/request! {:url url :as :json}))]
;      (if (not= status 200)
;        (prn "error requesting currency")
;        (let [display-name (:chartName body)
;              {:keys [code rate]} (-> body
;                                      (:bpi)
;                                      (:USD))]
;          (reset! currency {:display-name display-name
;                            :code code
;                            :rate rate}))))))
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

(get-currency "http://api.coindesk.com/v1/bpi/currentprice.json")

currency

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
                 (ok (get-currency "http://api.coindesk.com/v1/bpi/currentprice.json"))
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

