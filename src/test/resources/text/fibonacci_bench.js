function fib(n) {
    if (n < 3)
        return 1;
    return fib(n - 2) + fib(n - 1);
}
console.time('fib(30)');
var i = 0;
while(i < 50){
    fib(30);
    i++;
}
console.timeEnd('fib(30)');