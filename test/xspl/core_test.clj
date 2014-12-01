(ns xspl.core-test
  (:require [clojure.test :refer :all]
            [xspl.core :refer :all]))

(deftest load-searches-list-test
    (is (< 100 (count (load-searches-list "searches_list.txt")))))

(deftest get-saved-searches-conf-test
    (is (not (nil? (:body (get-saved-searches-conf "https://raw.githubusercontent.com/niyue/splunking-bitcoin/master/src/vagrant/splunk/apps/bitcoin/local/savedsearches.conf"))))))

(deftest get-searches-test
  (is (< 1 (count (get-searches (slurp "/Users/sni/dev/projects/sandbox/xspl/resources/savedsearches.conf"))))))

(deftest get-commands-test
  (is (= ["search" "fields" "top"] (get-commands "index=test | fields host, sourcetype | top 10 host"))))

(deftest get-commands-with-search-macro-test
  (is (= ["search" "search_macro" "top"] (get-commands "index=test | `puppet_host_seen host | top 10 host"))))

(deftest get-commands-with-custom-command-test
  (is (= ["search" "custom_command" "top"] (get-commands "index=test | greet 'hello' | top 10 host"))))


(deftest fetch-all-searches-test
  (is (< 100 (count (fetch-all-searches "searches_list.txt")))))

(deftest analyze-all-searches-test
  (is (not (nil? (analyze-searches (fetch-all-searches "searches_list.txt"))))))
