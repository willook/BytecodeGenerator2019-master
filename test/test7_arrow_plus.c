void main () {
	int t = 33;
    int a = 5;

	t -> + 2 -> + 5;
	_print(t);

    a -> + 5;
    t -> + a;
    t -> + t;
    _print(t);
    _print(a);
}
