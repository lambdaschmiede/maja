(defproject com.lambdaschmiede/maja "0.1.1"
  :description "A library to integrate honeycomb.io with Clojure applications"
  :url "https://github.com/lambdaschmiede/maja"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1" :scope "provided"]
                 [io.honeycomb.libhoney/libhoney-java "1.3.1"]]
  :repl-options {:init-ns maja.core})
