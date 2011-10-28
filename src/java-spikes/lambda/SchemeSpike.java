package lambda;

import java.util.ArrayList;
import java.util.List;

public class SchemeSpike {
   
        public static void main(String... args) {
            System.out.println(ArrayList.class.getTypeParameters()[0].getName());
        }
    
        public <T> List<?> _(T... forms) {
            return null;
        }

        public static Fn0<?> defn(Object name, List<?> arguments, Object... forms) {
            return null;
        }
        
        public static List<?> if_(List<?> test, List<?> then, List<?> else_) {
            return null;
        }

        public static List<?> if_(List<?> test, Object then, List<?> else_) {
            return null;
        }

//        class sqrt<x> {
//            class good_enough_q<guess> {{
//                _(lt, _(abs, _(minus, _(square, guess), x)), 0.001);
//                class improve<guess> {{
//                    _(average, guess, _(div, x, guess));}}
//                class sqrt_iter<guess> {{
//                    _(if_, _(good_enough_q, guess),
//                            guess,
//                            _(sqrt_iter, _(improve, guess)));}}
//                _(sqrt_iter, 1.0);}}}
//
        {
            
//  (define (sqrt x)
//          (define (good-enough? guess)
//                  (< (abs (- (square guess) x)) 0.001))
//                (define (improve guess)
//                  (average guess (/ x guess)))
//                (define (sqrt-iter guess)
//                  (if (good-enough? guess)
//                      guess
//                      (sqrt-iter (improve guess))))
//                (sqrt-iter 1.0))
      //
          
//        _(define, _(sqrt, x),
//            _(define, _(good_enough_q, guess),
//                _(lt, _(abs, _(minus, _(square, guess), x)), 0.001),
//                _(define, _(improve, guess),
//                    _(average, guess, _(div, x, guess)),
//                _(define, _(sqrt_iter, guess),
//                    _(if_, _(good_enough_q, guess),
//                             guess,
//                            _(sqrt_iter, _(improve, guess))),
//                _(sqrt_iter, 1.0)))));


//        defn(sqrt, _(x),
//                defn(good_enough_q, _(guess),
//                    _(lt, _(abs, _(minus, _(square, guess), x)), 0.001),
//                    defn(improve, _(guess),
//                        _(average, guess, _(div, x, guess)),
//                    defn(sqrt_iter, _(guess),
//                        if_(_(good_enough_q, guess),
//                                 _(guess),
//                                _(sqrt_iter, _(improve, guess))),
//                    _(sqrt_iter, 1.0)))));
//            defn(sqrt, _(x),
//                defn(good_enough_q, _(guess),
//                    _('<', _(abs, _('-', _(square, guess), x)), 0.001)),
//                defn(improve, _(guess),
//                    _(average, guess, _('/', x, guess))),
//                defn(sqrt_iter, _(guess),
//                    if_(_(good_enough_q, guess),
//                        guess,
//                        _(sqrt_iter, _(improve, guess)))),
//                _(sqrt_iter, 1.0));

          class _ {Object value;};
          class lt {Comparable x, y; {_(x.compareTo(y) > 0);}};
          class abs {Double x; {_(Math.abs(x));};
          class minus {};
          class div {};
          class square {};
          class average {};
          class if_ {};
          
          final Class<?> lt = lt.class;
          final Class<?> abs = abs.class;
          final Class<?> minus = minus.class;
          final Class<?> square = square.class;
          final Class<?> average = average.class;
          final Class<?> div = div.class;
          final Class<?> if_ = if_.class;
          

//        defn(sqrt, _(x),
//        defn(good_enough_q, _(guess),
//            _('<', _(abs, _('-', _(square, guess), x)), 0.001)),
//        defn(improve, _(guess),
//            _(average, guess, _('/', x, guess))),
//        defn(sqrt_iter, _(guess),
//            if_(_(good_enough_q, guess),
//                guess,
//                _(sqrt_iter, _(improve, guess)))),
//        _(sqrt_iter, 1.0));
          class sqrt {_ x;
              class good_enough_q {_ guess;
                  {_(lt, _(abs, _(minus, _(square, guess), x)), 0.001);}}
              class improve {_ guess;
                  {_(average, guess, _(div, x, guess));}}
              class sqrt_iter {_ guess;
                  {if_(_(good_enough_q.class, guess),
                          guess,
                          _(this, _(improve.class, guess)));}}
              {_(sqrt_iter.class, 1.0);}};
              
  
              {
                  _(sqrt.class, 8);                  
              }
              
        }
        
//        static enum NS {
//            lt, minus, div,
//            if_, define,
//            abs, square, average, good_enough_q, sqrt, sqrt_iter, improve,
//            guess, x;
        }
//
//        static String lt, minus, div;
//        static String if_, define;
//        static String abs, square, average, good_enough_q, sqrt, sqrt_iter, improve;
//        static String guess, x;

}
