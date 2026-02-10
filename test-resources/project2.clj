(defproject sub-project-two "1.0.0-SNAPSHOT"
            :description "FIXME: write description"
            :url "http://example.com/FIXME"
            :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
                      :url  "https://www.eclipse.org/legal/epl-2.0/"}
            :dependencies [[org.clojure/clojure "1.11.1"]
                           [sub-project-two "0.1.0-SNAPSHOT"]]
            :profiles {:test {:dependencies [[babashka/fs "0.5.31"]
                                             [mock-clj "0.2.1"]]}}
            :repl-options {:init-ns lein-sub-bump.core})
