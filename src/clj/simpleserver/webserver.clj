(ns simpleserver.webserver
  (:require [clojure.tools.logging :as log]
            [ring.util.http-response :as ring-response]
            [reitit.ring :as reitit-ring]
            [reitit.coercion.malli]
            [reitit.swagger :as reitit-swagger]
            [reitit.swagger-ui :as reitit-swagger-ui]
            [reitit.ring.malli]
            [reitit.ring.coercion :as reitit-coercion]
            [reitit.ring.middleware.muuntaja :as reitit-muuntaja]
            [reitit.ring.middleware.exception :as reitit-exception]
            [reitit.ring.middleware.parameters :as reitit-parameters]
            [reitit.ring.middleware.dev]
            [muuntaja.core :as mu-core]))

(defn version
  "Gets the version info."
  [_]
  (log/debug "ENTER version")
  ; TODO: From configuration.
  {:status 200 :body {:version "0.1.0"}})

(defn make-response [response-value]
  (if (= (:ret response-value) :ok)
      (ring-response/ok response-value)
      (ring-response/bad-request response-value)))

(defn routes
  "Routes."
  [env]
  ;; http://localhost:4265/swagger.json
  [["/swagger.json"
    {:get {:no-doc true
           :swagger {:info {:title "Simple server api"
                            :description "Simple server Api"}
                     :tags [{:name "api", :description "api"}]}
           :handler (reitit-swagger/create-swagger-handler)}}]
   ;; http://localhost:4265/api-docs/index.html
   ["/api-docs/*"
    {:get {:no-doc true
           :handler (reitit-swagger-ui/create-swagger-ui-handler
                      {:config {:validatorUrl nil}
                       :url "/swagger.json"})}}]
   ["/api"
    {:swagger {:tags ["api"]}}
    ; For development purposes. Try (install httpie): http localhost:4265/api/ping
    ["/ping" {:get {:summary "ping get"
                    ; Don't allow any query parameters.
                    :parameters {:query [:map]}
                    :responses {200 {:description "Ping success"}}
                    :handler (constantly (make-response {:ret :ok, :reply "pong"}))}
              :post {:summary "ping post"
                     :responses {200 {:description "Ping success"}}
                     ;; reitit adds mt/strip-extra-keys-transformer - probably changes in reitit 1.0,
                     ;; and therefore {:closed true} is not used with reitit < 1.0.
                     ; Try (install httpie): http POST localhost:4265/api/ping ping=jee
                     :parameters {:body [:map {:closed true} [:ping string?]]}
                     :handler (fn [req]
                                (let [body (get-in req [:parameters :body])
                                      myreq (:ping body)]
                                  (-> {:ret :ok :request myreq :reply "pong"}
                                      (make-response))))}}]
    ; Try (install httpie): http localhost:4265/api/version
    ["/version" {:get {:summary "Get version info"
                    :parameters {:query [:map]}
                    :responses {200 {:description "Version info success"}}
                    :handler (fn [{}] (version env))}}]
    ]])

(defn handler
  "Handler."
  [routes]
  (->
    (reitit-ring/ring-handler
      (reitit-ring/router routes {
                                  ; Use this to debug middleware handling:
                                  ;:reitit.middleware/transform reitit.ring.middleware.dev/print-request-diffs
                                  :data {:muuntaja mu-core/instance
                                         :coercion (reitit.coercion.malli/create
                                                     {;; set of keys to include in error messages
                                                      :error-keys #{:type :coercion :in #_:schema #_:value #_:errors :humanized #_:transformed}
                                                      ;; validate request & response
                                                      :validate true
                                                      ;; top-level short-circuit to disable request & response coercion
                                                      :enabled true
                                                      ;; strip-extra-keys (effects only predefined transformers)
                                                      :strip-extra-keys true
                                                      ;; add/set default values
                                                      :default-values true
                                                      ;; malli options
                                                      :options nil}) ;; malli
                                         :middleware [;; swagger feature
                                                      reitit-swagger/swagger-feature
                                                      ;; query-params & form-params
                                                      reitit-parameters/parameters-middleware
                                                      ;; content-negotiation
                                                      reitit-muuntaja/format-negotiate-middleware
                                                      ;; encoding response body
                                                      reitit-muuntaja/format-response-middleware
                                                      ;; exception handling
                                                      (reitit-exception/create-exception-middleware
                                                        (merge
                                                          reitit-exception/default-handlers
                                                          {::reitit-exception/wrap (fn [handler ^Exception e request]
                                                                                     (log/error e (.getMessage e))
                                                                                     (handler e request))}))
                                                      ;; decoding request body
                                                      reitit-muuntaja/format-request-middleware
                                                      ;; coercing response bodys
                                                      reitit-coercion/coerce-response-middleware
                                                      ;; coercing request parameters
                                                      reitit-coercion/coerce-request-middleware]}})
      (reitit-ring/routes
        (reitit-ring/redirect-trailing-slash-handler)
        (reitit-ring/create-file-handler {:path "/", :root "dev-resources/public"})
        (reitit-ring/create-resource-handler {:path "/"})
        (reitit-ring/create-default-handler)))))


(comment

  (require '[clj-http.client])
  (clj-http.client/get
    (str "http://localhost:4265/api/version") {:debug true :accept "application/json"})

  (clj-http.client/get
    (str "http://localhost:4265/index.html") {:debug true})

  (clj-http.client/get
    (str "http://localhost:4265") {:debug true})

  (clj-http.client/get
    (str "http://localhost:4265/index.html") {:debug true})

  (user/system)

  (handler {:routes (routes (user/env))})

  (clj-http.client/get
    (str "https://reqres.in/api/users/2") {:debug true :accept "application/json"})

  (clj-http.client/get
    ; TODO
    (str "http://localhost:4265/info") {:debug true :accept "application/transit"})

  )
