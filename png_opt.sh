pngcrush -rem allb -brute -reduce -ow -z 3 zmem 9 src/*.png 
optipng -o7 src/*.png

pngcrush -rem allb -brute -reduce -ow -z 3 zmem 9 src/res/*.png 
optipng -o7 src/res/*.png

pngcrush -rem allb -brute -reduce -ow -z 3 zmem 9 src/res/groups/*.png 
optipng -o7 src/res/groups/*.png

pngcrush -rem allb -brute -reduce -ow -z 3 zmem 9 src/res/huge/*.png 
optipng -o7 src/res/huge/*.png