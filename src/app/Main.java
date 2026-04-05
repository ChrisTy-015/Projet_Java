package app;

import java.util.Arrays;

import app.ui.ApplicationConsole;
import app.ui.ApplicationGraphique;

public class Main {
    private Main() {
    }

    public static void main(String[] args) {
        boolean modeConsole = Arrays.stream(args).anyMatch("--console"::equals);
        String[] autresArgs = Arrays.stream(args)
                .filter(arg -> !"--console".equals(arg))
                .toArray(String[]::new);

        if (modeConsole) {
            new ApplicationConsole(autresArgs).lancer();
            return;
        }
        new ApplicationGraphique(autresArgs).lancer();
    }
}
