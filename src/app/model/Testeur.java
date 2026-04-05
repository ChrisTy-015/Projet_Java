package app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represente un joueur habilite a publier des tests et a signaler
 * des evaluations.
 */
public class Testeur extends Joueur {
    private final List<TestJeu> tests;

    /**
     * Cree un testeur a partir d'un pseudo.
     *
     * @param pseudo pseudo du testeur
     */
    public Testeur(String pseudo) {
        super(pseudo);
        this.tests = new ArrayList<>();
    }

    /**
     * Cree un testeur a partir d'un joueur existant en conservant
     * son etat metier.
     *
     * @param joueur joueur a promouvoir
     */
    public Testeur(Joueur joueur) {
        super(joueur.getPseudo());
        this.tests = new ArrayList<>();
        copierEtatDe(joueur);
        if (joueur instanceof Testeur testeur) {
            this.tests.addAll(testeur.tests);
        }
    }

    @Override
    public String getTypeProfil() {
        return "testeur";
    }

    /**
     * Ajoute un test publie par le testeur.
     *
     * @param test test a associer
     */
    public void ajouterTest(TestJeu test) {
        if (test == null) {
            throw new IllegalArgumentException("Le test ne peut pas etre null");
        }
        if (!tests.contains(test)) {
            tests.add(test);
        }
    }

    /**
     * Retourne les tests publies par le testeur.
     *
     * @return liste non modifiable des tests
     */
    public List<TestJeu> getTests() {
        return Collections.unmodifiableList(tests);
    }

    /**
     * Retourne le nombre de tests publies.
     *
     * @return nombre de tests
     */
    public int getNombreTests() {
        return tests.size();
    }

    /**
     * Signale une evaluation pour moderation.
     *
     * @param evaluation evaluation a signaler
     */
    public void signalerEvaluation(Evaluation evaluation) {
        if (evaluation == null) {
            throw new IllegalArgumentException("L'evaluation ne peut pas etre null");
        }
        evaluation.signaler();
    }
}
