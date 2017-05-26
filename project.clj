(defproject coinwatch "0.1.0-SNAPSHOT"
  :dependencies [;server side deps
                 [org.clojure/clojure "1.8.0"]
                 [environ "1.1.0"]
                 [metosin/compojure-api "1.2.0-alpha6" :exclude [compojure, metosin/muuntaja]]
                 [ring/ring "1.6.0-RC1"]
                 [compojure "1.6.0-beta3"]
                 [ring-jetty/ring-ws "0.1.0-SNAPSHOT"]
                 [manifold "0.1.6"]
                 [io.nervous/kvlt "0.1.4"]
                 [org.clojure/core.async "0.3.441"]

                 ;client side deps
                 [org.clojure/clojurescript "1.9.229"]
                 [reagent "0.6.0"]
                 [re-frame "0.9.2"]
                 [day8.re-frame/http-fx "0.1.3"]
                 [re-frisk "0.3.2"]
                 [secretary "1.2.3"]
                 [garden "1.3.2"]
                 [cljs-ajax "0.6.0"]
                 [ns-tracker "0.3.0"]]

  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-garden "0.2.8"]]
  :ring {:handler coinwatch.handler/app
         :async? true
         :websockets {"/echo" coinwatch.ws-handler/app}}

  :uberjar-name "server.jar"

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"
                                    "test/js"
                                    "resources/public/css"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :garden {:builds [{:id           "screen"
                     :source-paths ["src/clj"]
                     :stylesheet   coinwatch.css/screen
                     :compiler     {:output-to     "resources/public/css/screen.css"
                                    :pretty-print? true}}]}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.8.2"]
                   [figwheel-sidecar "0.5.9"]
                   [com.cemerick/piggieback "0.2.1"]]

    :plugins      [[lein-ring "0.11.0"]
                   [lein-figwheel "0.5.9"]
                   [lein-doo "0.1.7"]]
    }}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "coinwatch.core/mount-root"}
     :compiler     {:main                 coinwatch.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}
                    }}

    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            coinwatch.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}

    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:main          coinwatch.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :optimizations :none}}
    ]}

  )
