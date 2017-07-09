(ns coinwatch.events
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [coinwatch.config]
            [re-frame.core :as re-frame]
            [coinwatch.db :as db]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

(re-frame/reg-event-db
 :initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 :set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-db
 :login-success
 (fn [db [_ body]]
   (js/alert "Hello")
   (assoc db :user body)))

(re-frame/reg-event-db
 :set-email
 (fn [db [_ email]]
   (assoc db :email email)))

(re-frame/reg-event-db
 :set-password
 (fn [db [_ password]]
   (assoc db :password password)))

(re-frame/reg-event-db
 :http-request-failure
 (fn [db [_ response]]
   (.log js/console (clj->js response))
   (assoc db :loading? false)))

(re-frame/reg-fx
 :http
 (fn [{:keys [url method on-success on-failure json-params query-params]} _]
   (go
     (let [url     (str "http://localhost:3000/api" url)
           request (merge
                    {:url        url
                     :timeout    8000
                     :method     method}
                    (if (or (= :post method) (= :put method))
                      {:json-params json-params}
                      {:query-params query-params}))
           {:keys [success body] :as response} (<! (http/request request))]
       (if success
         (re-frame/dispatch [on-success body])
         (re-frame/dispatch [:http-request-failure response]))))))

(re-frame/reg-event-fx
 :login-request
 (fn [{:keys [db]} _]
   {:db   (assoc db :loading? true)
    :http {:json-params (select-keys db [:email :password])
           :url         "/login"
           :method      :post
           :on-success  :login-success}}))
