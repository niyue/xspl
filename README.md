# xspl

This is a tiny Clojure project used to fetch Splunk saved searches conf files from github and analyze different search commands usage.

## Usage

Run `(analyze-searches (fetch-all-searches "searches_list.txt"))` to get all the results. Check out core_test.clj for details.

Once getting all the usage results, use any spreadsheet software/charting tools you can easily visualize the data set like below:

![Usage chart](/doc/usage_chart.png)

## License

Distributed under the Eclipse Public License either version 1.0 or any later version.
