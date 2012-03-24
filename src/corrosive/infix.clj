(ns corrosive.infix
  "Infix facility; used idea from: https://github.com/jbester/cljext/blob/master/cljext/math.clj")

(defonce ^:dynamic  *precendence-table*
  {'|| 10
   '&& 20
   '== 30
   '!= 30
   '<  40
   '>  40
   '<= 40
   '>= 40
   '-  50
   '+  60
   '/  70
   '*  80
   'mod 90})

(defn- max-precedence
  "Return which is maximum value inside *precendence-table*."
  []
  (->> (vals *precendence-table*)
       (apply max) ))

(defn !=
  "Check if two numbers are different."
  [a b]
  (not (= a b)))

(defn- find-lowest-precedence
  "Find operator with lowest precedence in collection."
  [col]
  (loop [idx      0
         col      col
         lowest-idx nil
         lowest-prec (max-precedence)]

    (if (empty? col)
      lowest-idx
      (let [key  (first col)
            prec (get *precendence-table* key)]

        (if (and prec
                 (<= prec lowest-prec))
          (recur (inc idx) (rest col) idx prec)
          (recur (inc idx) (rest col) lowest-idx lowest-prec)
) ) ) ) )

(defn- infix-to-prefix
  "Convert infix-ed collection to prefix version."
  [col]
  (cond
    (not (seq? col))  col
    (= 1 (count col)) (first col)
    true
    (let [lowest (find-lowest-precedence col)]
      (if-not lowest
        col
        (let [[hd [op & tl]] (split-at lowest col)]
          (list op
                (infix-to-prefix hd)
                (infix-to-prefix tl)
) ) ) ) ) )

(defmacro $
  "Macro for evaluating infix forms."
  [& body]
  (infix-to-prefix body))
