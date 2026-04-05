package app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Testeur extends Joueur {
    private final List<TestJeu> tests;

    public Testeur(String pseudo) {
        super(pseudo);
        this.tests = new ArrayList<>();
    }

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

    public void ajouterTest(TestJeu test) {
        if (test == null) {
            throw new IllegalArgumentException("Le test ne peut pas etre null");
        }
        if (!tests.contains(test)) {
            tests.add(test);
        }
    }

    public List<TestJeu> getTests() {
        return Collections.unmodifiableList(tests);
    }

    public int getNombreTests() {
        return tests.size();
    }

    public void signalerEvaluation(Evaluation evaluation) {
        if (evaluation == null) {
            throw new IllegalArgumentException("L'evaluation ne peut pas etre null");
        }
        evaluation.signaler();
    }
}
