(ns coinwatch.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 :username
 (fn [db _]
   (:username db)))

(re-frame/reg-sub
 :password
 (fn [db _]
   (:password db)))
