(ns coinwatch.views
    (:require [re-frame.core :as re-frame]))

(defn currency-card [currency]
  (fn []
    [:div {}
     (:name currency)
     (:symbol currency)
     (:price currency)]))

;; home

(defn home-panel []
  (fn []
    (let [currency (re-frame/subscribe [:currency])]
      (currency-card @currency))))

;; about

(defn about-panel []
  (fn []
    [:a {:href "#/"} "Home Page"]))

;; main

(defn panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      [show-panel @active-panel])))
