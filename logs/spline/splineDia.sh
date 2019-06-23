#! /bin/sh
plot() {
/usr/bin/gnuplot <<EOF
set xlabel "mm"
set ylabel "velocity"
set terminal postscript enhanced "Times Roman" 18
set output '$1velocity.eps
set xzeroaxis
plot "$1" using 1:6 with lines lw 4 title "Speed", "$1" using 1:4 with lines lw 4 title "x","$1" using 1:5 with lines lw 4 title "y"
set xlabel "x"
set ylabel "y"
set output '$1way.eps
plot "$1" using 2:3 with lines lw 4 title "driven way"
EOF
}

for FILE in $(ls *.data); do
echo $FILE
plot $FILE
done
