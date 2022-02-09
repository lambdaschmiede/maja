(ns maja.middleware
  (:require [maja.core :refer [send-event now uuid root-trace traces]]))

(defn honey-middleware
  "Returns a middleware which automatically starts a trace with a distinct uuid for each incoming HTTP call, when applied"
  [handler honey]
    (fn [request]
    (binding [root-trace (uuid) ;; The middleware is the entry point and should always generate a new trace
              traces (conj traces (uuid))]
      (let [start (now)
            response (handler request)
            duration (- (now) start)
            span-id (last traces)]

        (send-event {"duration_ms" duration
                     "name" "honey-middleware"
                     "trace.span_id" span-id
                     "trace.trace_id" root-trace
                     "status" (:status response)
                     "method" (name (:request-method request))
                     "path" (:uri request)}
                    start
                    honey)
        response))))
