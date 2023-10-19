(defproject xspl "0.1.0-SNAPSHOT"
  :description "A Splunk SPL analyzer"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :resource-paths ["resources"]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.logging "1.2.4"]
                 [http-kit "2.7.0"]]
  :main xspl.core)
