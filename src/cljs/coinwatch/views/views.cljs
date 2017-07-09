(ns coinwatch.views.views
  (:require [re-frame.core :as re-frame]
            [coinwatch.views.login_form :refer [login-form]]))

(defn- panels [panel-name]
  (case panel-name
    :login [login-form]
    [:div "URL not found :("]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      [:section.content
       [show-panel @active-panel]])))


