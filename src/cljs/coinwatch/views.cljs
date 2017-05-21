(ns coinwatch.views
    (:require [re-frame.core :as re-frame]))


;; home

(defn home-panel []
  (let [price (re-frame/subscribe [:price])]
    (fn []
      [:div ""
        [:div (str "Current price is " @price)]
        [:button {:on-click #(re-frame/dispatch [:request-price])} "Update price"]
        [:div [:a {:href "#/about"} "About"]]])))

 
;; about

(defn about-panel []
  (fn []
    [:div "This is the About Page."
    [:div [:a {:href "#/"} "go to Home Page"]]]))

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
