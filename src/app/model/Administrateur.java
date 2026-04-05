package app.model;

public class Administrateur extends Testeur {
    public Administrateur(String pseudo) {
        super(pseudo);
    }

    public Administrateur(Testeur testeur) {
        super(testeur);
    }

    @Override
    public String getTypeProfil() {
        return "administrateur";
    }

    public void supprimerEvaluation(Evaluation evaluation) {
        if (evaluation == null) {
            throw new IllegalArgumentException("L'evaluation ne peut pas etre null");
        }
        evaluation.supprimer();
    }

    public void bloquerMembre(Membre membre) {
        if (membre == null) {
            throw new IllegalArgumentException("Le membre ne peut pas etre null");
        }
        membre.bloquer();
    }

    public void debloquerMembre(Membre membre) {
        if (membre == null) {
            throw new IllegalArgumentException("Le membre ne peut pas etre null");
        }
        membre.debloquer();
    }
}
