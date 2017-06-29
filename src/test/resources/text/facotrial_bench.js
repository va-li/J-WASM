function fac(n) {
    if (n === 0)
        return 1;
    return n * fac(n - 1);
}
console.time('fac(20)');
var i = 0;
while(i < 10000){
    fac(20);
    i++;
}
console.timeEnd('fac(20)');