(ns coinwatch.views.login_form
  (:require [re-frame.core :as re-frame]))

(defn login-form []
  (let [email (re-frame/subscribe [:email])
        password (re-frame/subscribe [:password])]
    (fn []
      [:div.row
       [:div.col-md-6
        [:div.box.box-primary
         [:div.box-header.with-border
          [:h3.box-title "Login"]]
         [:form {:role "form"}
          [:div.box-body
           [:div.form-group
            [:label {:for "email-input"} "Email"]
            [:input#email-input.form-control
             {:type        "text"
              :value       @email
              :on-change   (fn [event]
                             (re-frame/dispatch [:set-email (-> event
                                                                   .-target
                                                                   .-value)]))
              :placeholder ""}]]
            [:div.form-group
            [:label {:for "password-input"} "Password"]
            [:input#password-input.form-control
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
              :on-click (fn [event]
                          (.preventDefault event)
                          (re-frame/dispatch [:login-request]))
              :value    "Log in"}]]]]]]])))
