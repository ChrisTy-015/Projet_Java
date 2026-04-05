package app;

import java.util.Arrays;

import app.ui.ApplicationConsole;
import app.ui.ApplicationGraphique;

/**
 * Point d'entree de l'application.
 * <p>
 * L'application demarre en mode graphique par defaut et peut etre lancee
 * en mode console via l'option {@code --console}.
 */
public class Main {
    /**
     * Constructeur prive d'une classe utilitaire.
     */
    private Main() {
    }

    /**
     * Lance l'application dans le mode approprie selon les arguments recus.
     *
     * @param args arguments de ligne de commande, dont {@code --console}
     *             pour activer l'interface console
     */
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
