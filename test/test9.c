int main() {
    int j = 0;
    int i = 0;
    int num[5];
    num[i] = 9;
    num[++i] = 8;
    num[3] = i + 1;
    num[2] = ++i;

    while (j < 5) {
        _print(num[j]);
        j = j + 1;
    }
}