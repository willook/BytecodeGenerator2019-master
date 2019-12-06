int add(int x, int y) {
   int z;
   z = x+y;
   return z;
}

void main () {
   int t = 10;
   while(t!=0) {
       _print(add(0,t));
       --t;
   }
}