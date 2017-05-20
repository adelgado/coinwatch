(ns coinwatch.events
  (:require [re-frame.core :as re-frame]
            [coinwatch.db :as db]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))


(re-frame/reg-event-fx        ;; <-- note the `-fx` extension
 :request
 (fn                ;; <-- the handler function
   [{db :db} _]     ;; <-- 1st argument is coeffect, from which we extract db 

   {:http-xhrio {:method          :get
                 :uri             "http://api.coindesk.com/v1/bpi/currentprice.json"
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true}) 
                 :on-success      [:process-response]
                 :on-failure      [:bad-response]}
    :db  (assoc db :loading? true)}))


(re-frame/reg-event-db
 :process-response
 (fn
   [db [_ response]]           ;; destructure the response from the event vector
   (-> db
       (assoc :loading? false) ;; take away that "Loading ..." UI 
       (assoc :data (js->clj response)))))  ;; fairly lame processing

 (re-frame/reg-event-db
  :bad-response
  (fn
    [db [_ response]]           ;; destructure the response from the event vector
    (-> db
        (assoc :loading? false) ;; take away that "Loading ..." UI 
        (assoc :data (js->clj response)))))  ;; fairly lame processing
