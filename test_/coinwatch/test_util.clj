(ns ^{:doc "namespace containing all common function for tests"}
    coinwatch.test-util
  (:require [cheshire.core :as json]
            [clojure.java.jdbc :as jdbc]
            [coinwatch.handler :refer [app]]
            [ring.mock.request :as mock]
            [coinwatch.db.db :as db])
  (:import (clojure.lang Keyword PersistentArrayMap)))

(def auth-secret "Basic dmR0OnZkdDYwMG1h")

(defn- parse-body [^PersistentArrayMap body]
  (if (nil? body)
    {}
    (json/parse-string (slurp body) true)))

(defn request-with-body [^Keyword verb ^String url ^PersistentArrayMap payload]
  (let [resp (app (-> (mock/request verb (str "/api" url))
                      (mock/content-type "application/json")
                      (mock/header "authorization" auth-secret)
                      (mock/body (json/generate-string payload))))]
    (update resp :body parse-body)))

(defn get-request [^String url]
  (let [resp (app (-> (mock/request :get (str "/api" url))
                      (mock/header "authorization" auth-secret)))]
    (update resp :body parse-body)))

(defn delete-campaign [db-spec ^Long campaign-id]
  (jdbc/delete! db-spec :tb_email_breno ["idbreno = ?" campaign-id])
  (jdbc/delete! db-spec :tb_breno ["id = ?" campaign-id]))
