package ro.ase.csie.licenta.classes;

public enum Priority {
    HIGH(1), MEDIUM(2), LOW(3), ZERO(4);

    int square;

    Priority(int square){
        this.square = square;
    }
}
