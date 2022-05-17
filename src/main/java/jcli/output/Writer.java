package jcli.output;

public final class Writer {

    private Writer() {

    }

    public static void print(String message) {
        System.out.println(message);
    }

    public static void print(Object object) {
        System.out.println(object.toString());
    }

}
