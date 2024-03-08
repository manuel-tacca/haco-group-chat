package project.DataStructures;

public class Tuple2<X, Y> {

    private final X x;
    private final Y y;

    public Tuple2(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    public X first(){
        return this.x;
    }

    public Y second(){
        return this.y;
    }

}
