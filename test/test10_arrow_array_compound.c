void main () {
   int a[2];

   a[0] = 3;
   a[1] = 6;

   a -> + 3 -> * 2;
   _print(a[0]);  // 12
   _print(a[1]);  // 18

   a -> / 2 -> - 2;
   _print(a[0]);  // 4
   _print(a[1]);  // 7
}