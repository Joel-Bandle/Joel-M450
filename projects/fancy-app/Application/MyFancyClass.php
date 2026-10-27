<?php

class MyFancyClass
{
    public function shortString($text, $length, $ending = '...') {
        if (mb_strlen($ending) > $length) {
            return false;
        }

        if (mb_strlen($text) <= $length) {
            return $text;
        }

        return mb_substr($text, 0, $length - mb_strlen($ending)) . $ending;
    }

    public function calcAverage($values) {
        if (count($values) === 0) {
            return 0;
        }

        $sum = 0;
        foreach ($values as $value) {
            if (!is_numeric($value)) {
                return false;
            }
            $sum += $value;
        }

        return $sum / count($values);
    }

    public function getOpposite($value, $delimiter = ',') {
        if (is_array($value)) {
            return implode($delimiter, $value);
        }

        if (is_string($value)) {
            return explode($delimiter, $value);
        }

        return false;
    }
}
