(ns coinwatch.views.home
  (:require [re-frame.core :as re-frame]))

(defn home []
  [:div.row
   [:div.col-md-6
    [:div.box.box-primary
     [:div.box-header.with-border
      [:h3.box-title "Welcome"]]]]])
