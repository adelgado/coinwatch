(ns coinwatch.handler
  (:require [coinwatch.cfg :refer [cfg]]
            [coinwatch.middleware :as m]
            [compojure.route :as route]
            [coinwatch.err :as err]
            [coinwatch.service
             [user :as user-service]]
            [cheshire.core :as json]
            [clojure.core.match :refer [match]]
            [compojure.api
             [swagger :as docs]
             [sweet :as ring]]
            [ring.util.http-response :as res]
            [schema.core :as s]))

(def non-empty-string #"^(?!\s*$).+")

(def app
  (ring/api
   {:exceptions {:handlers {:compojure.api.exception/default err/handler}}}

   (route/resources "/")

   (ring/context "/" []
     (ring/GET "/status" []
       :summary "Health Check Route"
       {:status 200}))

   (ring/context "/docs" req
     :middleware [;m/has-auth? (m/authorized? (:auth-secret cfg))
]
     (docs/swagger-routes
      {:ui      "/"
       :options {:ui {:swagger-docs "/docs/swagger.json"}}
       :spec    "/swagger.json"
       :data    {:info {:title "Coinwatch"}}}))

   (ring/context "/api" []
     :tags ["api"]
     ;:header-params [authorization :- non-empty-string]
     :middleware [;(m/authorized? (:auth-secret cfg))
]

     (ring/POST "/login" []
       :summary "User login"
       :body-params [email :- non-empty-string
                     password :- non-empty-string]
       :return {:id Long}
       (match (user-service/authenticate email password)
         [:ok {:id id}]
         (res/ok {:id id})

         [:not-authorized]
         (res/unauthorized))))))
