(ns coinwatch.err
  (:import java.util.Arrays)
  (:require [clojure.tools.logging :as log]
            [cheshire.core :as json]))

(defn handler [^Exception e _ request]
  (log/error
   (json/generate-string
    (merge
     (select-keys request [:headers :uri :params :request-method])
     {:error (.getMessage e)
      :body  (:body-params request)
      :stack-trace
      (-> e
          (.getStackTrace)
          (Arrays/toString)
          (clojure.string/replace #"," "\n"))})))

  {:status  520
   :headers {"Content-Type" "application/json"}
   :body    {:error "Unknown Error :("}})

