(ns coinwatch.middleware
  (:require [ring.util.http-response :as res]))

(defn has-auth? [handler]
  (fn [request]
    (if (get (:headers request) "authorization")
      (handler request)
      (-> (res/unauthorized {:error "missing 'Authorization' header"})
          (res/header "WWW-Authenticate" "Basic realm=\"Coinwatch\"")))))

(defn authorized? [secret]
  (fn [handler]
    (fn [request]
      (if (= secret (get (:headers request) "authorization"))
        (handler request)
        (res/unauthorized
         {:error "incorrect value for 'Authorization' header"})))))

