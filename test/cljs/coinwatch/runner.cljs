(ns coinwatch.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [coinwatch.core-test]))

(doo-tests 'coinwatch.core-test)
