package tool;

public class OutputControl {
    private static boolean output = true;

    public static void setOutput(boolean output) {
        OutputControl.output = output;
    }

    public static void printMessage(String s) {
        if (output) {
            System.out.println(s);
        }
    }

    public static void printMessage(int s) {
        if (output) {
            System.out.println(s);
        }
    }
}
