package co.evelabs.beep;

import java.util.LinkedList;
import java.util.Queue;

    public class MovingAverage {
        private final Queue<Double> window = new LinkedList<Double>();
        private final int period;
        private double sum;

        public MovingAverage(int period) {
            assert period > 0 : "Period must be a positive integer";
            this.period = period;
        }

        public void newNum(double num) {
            sum += num;
            window.add(num);
            if (window.size() > period) {
                sum -= window.remove();
            }
        }

        public double getAvg() {
            if (window.isEmpty()) return 0; // technically the average is undefined
            return sum / window.size();
        }

    }
