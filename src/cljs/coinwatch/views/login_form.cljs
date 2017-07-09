(ns coinwatch.views.login_form
  (:require-macros [clojure.core :refer [doto]])
  (:require [re-frame.core :as re-frame]))

(defn login-form []
  (let [username (re-frame/subscribe [:username])
        password (re-frame/subscribe [:password])]
    (fn []
      [:div.row
       [:div.col-md-6
        [:div.box.box-primary
         [:div.box-header.with-border
          [:h3.box-title "Login"]
          [:div.box-tools
           [:div.input-group.input-group-sm
             [:a.btn.btn-default
              {:href "#/home"}
              [:i.fa.fa-arrow-left]]]]]
         [:form {:role "form"}
          [:div.box-body
           [:div.form-group
            [:label {:for "name-input"} "Username"]
            [:input#name-input.form-control
             {:type        "text"
              :value       @username
              :on-change   (fn [event]
                             (re-frame/dispatch [:set-username (-> event
                                                                   .-target
                                                                   .-value)]))
              :placeholder ""}]]
            [:div.form-group
            [:label {:for "name-input"} "Password"]
            [:input#name-input.form-control
             {:type        "password"
              :value       @password
              :on-change   (fn [event]
                             (re-frame/dispatch [:set-password (-> event
                                                                   .-target
                                                                   .-value)]))
              :placeholder ""}]]
           [:div.box-footer
            [:input.btn.btn-primary
             {:type     "submit"
              :on-click #(re-frame/dispatch [:login-request])
              :value    "Log in"}]]]]]]])))

