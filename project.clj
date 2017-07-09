(defproject coinwatch "0.1.0-SNAPSHOT"
  :description "You get to watch coins"

  :dependencies [;; SERVER SIDE
                 [environ "1.1.0"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.7.0-alpha1"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.2"]
                 [postgresql "9.3-1102.jdbc41"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 [ring "1.6.1"]
                 [ring/ring-mock "0.3.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [metosin/compojure-api "2.0.0-alpha3"]
                 [org.slf4j/slf4j-log4j12 "1.7.12"]
                 [org.clojure/core.async "0.3.443"]
                 [log4j/log4j "1.2.17"]

                 ;; CLIENT SIDE
                 [org.clojure/clojurescript "1.9.229"]
                 [reagent "0.6.0"]
                 [re-frame "0.9.2"]
                 [cljs-http "0.1.43"]
                 [re-frisk "0.3.2"]
                 [secretary "1.2.3"]
                 [garden "1.3.2"]
                 [ns-tracker "0.3.0"]
                 [figwheel-sidecar "0.5.9"]
                 [binaryage/devtools "0.9.4"]
                 [com.cemerick/piggieback "0.2.1"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "script"]

  :ring {:handler coinwatch.handler/app
         :nrepl   {:start? true}}

  :uberjar-name "coinwatch.jar"

  :plugins [[lein-environ "1.1.0"]
            [lein-ring "0.10.0"]
            [lein-cljsbuild "1.1.4"]
            [lein-cljfmt "0.5.6"]
            [lein-figwheel "0.5.9"]
            [lein-garden "0.2.8"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"
                                    "resources/public/css/compiled"]

  :figwheel {:css-dirs ["resources/public/css"
                        "resources/public/css/compiled"]}

  :garden {:builds [{:id           "screen"
                     :source-paths ["src/clj"]
                     :stylesheet   coinwatch.css/screen
                     :compiler     {:output-to     "resources/public/css/compiled/screen.css"
                                    :pretty-print? true}}]}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :profiles
  {:dev
   {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]
                   [cheshire "5.5.0"]
                   [ring/ring-mock "0.3.0"]
                   [binaryage/devtools "0.8.2"]
                   [figwheel-sidecar "0.5.9"]
                   [com.cemerick/piggieback "0.2.1"]]

    :plugins      [[lein-ring "0.11.0"]
                   [lein-figwheel "0.5.9"]
                   [lein-doo "0.1.7"]]}}


  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "coinwatch.core/mount-root"}
     :compiler     {:main                 coinwatch.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :externs              ["resources/public/js/ace/ace.js"]
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}}}


    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main                 coinwatch.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :optimizations        :advanced
                    :externs              ["resources/public/js/ace/ace.js"]
                    :source-map-timestamp true
                    :closure-defines      {goog.DEBUG false}
                    :pretty-print         false}}

    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:main          coinwatch.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :optimizations :none}}]})


