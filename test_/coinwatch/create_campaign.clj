(ns coinwatch.create-campaign
  (:require [coinwatch
             [cfg :refer [cfg]]
             [test-util :as util]]
            [clojure.test :refer :all]))

(defn create-campaign [payload]
  (util/request-with-body :post "/campaign" payload))

(def campaign {:subject "Isto é um assunto de e-mail"
               :body "Corpo contendo variavel com template de valor {{var}}"
               :campaign-name "breno De Teste"
               :template-filename nil
               :sql-query
               "SELECT u.id AS idUsuario, 1 as 'var'
                FROM tb_usuario u
                WHERE email like 'pablo.botelho@vadetaxi.com.br'
                LIMIT 10"})

(deftest invalid-data
  (testing "expect status 422 when SQL query is invalid"
    (let [response (create-campaign
                    (assoc campaign
                           :sql-query
                           "seKect u.id from tb_usuario as idUsuario"))]
      (is (= (:status response) 422) (str (:body response)))))

  (testing "expect status 422 when SQL query is missing user-id alias"
    (let [response (create-campaign
                    (assoc campaign
                           :sql-query
                           "select u.id from tb_usuario u"))]
      (is (= (:status response) 422) (str (:body response))))))

(deftest valid-data
  (testing "expect status 200 and campaign-id inside body"
    (let [response (create-campaign campaign)
          status (:status response)
          body (:body response)]
     (is (= status 201) (str body))
     (is (number? (:campaign-id body)) (str (:body response)))
     (test-util/delete-campaign (:campaign-id body)))))

