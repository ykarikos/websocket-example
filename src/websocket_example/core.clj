(ns websocket-example.core
  (:require [aleph.http :refer :all]
            [lamina.core :refer :all]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.java.io :as io]))

(def channels (atom []))

(defn- broadcast [message]
  (println "Received message" message)
  (doall (for [c @channels]
             (enqueue c message))))

(defn async-handler [channel handshake]
  (println "New client is here!")
  (swap! channels conj channel)
  (receive-all channel broadcast)
  (on-closed channel #(println "Client has disconnected")))

(defroutes handler
  (GET "/" [] (io/resource "public/index.html"))
  (GET "/async" [] (wrap-aleph-handler async-handler))
  (route/not-found "Not found"))

(defn -main []
  (start-http-server
   (wrap-ring-handler handler)
   {:port 3000 :websocket true})
  (println "Server started."))
