(ns coinwatch.auth-test
  (:require [coinwatch.handler :refer :all]
            [cheshire.core :as json]
            [clojure.test :refer :all]
            [ring.mock.request :as mock]))

(deftest auth-test
  (testing "should return 401 without 'authorization' header"
    (is (= 401 (:status (app (-> (mock/request :post "/api/campaign")
                                 (mock/content-type "application/json")
                                 (mock/header "authorization" "wrong value!")
                                 (mock/body "{}"))))))))
