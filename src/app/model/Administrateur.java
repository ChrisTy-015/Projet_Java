package app.model;

/**
 * Represente un membre disposant des droits d'administration de la plateforme.
 */
public class Administrateur extends Testeur {
    /**
     * Cree un administrateur a partir d'un pseudo.
     *
     * @param pseudo pseudo de l'administrateur
     */
    public Administrateur(String pseudo) {
        super(pseudo);
    }

    /**
     * Cree un administrateur a partir d'un testeur existant en conservant
     * ses informations.
     *
     * @param testeur testeur a promouvoir
     */
    public Administrateur(Testeur testeur) {
        super(testeur);
    }

    @Override
    public String getTypeProfil() {
        return "administrateur";
    }

    /**
     * Marque une evaluation comme supprimee.
     *
     * @param evaluation evaluation a supprimer
     */
    public void supprimerEvaluation(Evaluation evaluation) {
        if (evaluation == null) {
            throw new IllegalArgumentException("L'evaluation ne peut pas etre null");
        }
        evaluation.supprimer();
    }

    /**
     * Bloque un membre de la plateforme.
     *
     * @param membre membre a bloquer
     */
    public void bloquerMembre(Membre membre) {
        if (membre == null) {
            throw new IllegalArgumentException("Le membre ne peut pas etre null");
        }
        membre.bloquer();
    }

    /**
     * Debloque un membre de la plateforme.
     *
     * @param membre membre a debloquer
     */
    public void debloquerMembre(Membre membre) {
        if (membre == null) {
            throw new IllegalArgumentException("Le membre ne peut pas etre null");
        }
        membre.debloquer();
    }
}
