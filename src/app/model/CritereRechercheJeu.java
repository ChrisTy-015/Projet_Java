package app.model;

/**
 * Regroupe les filtres utilises pour rechercher des jeux
 * et des supports dans le catalogue.
 *
 * @param texteLibre texte libre applique au nom ou aux metadonnees
 * @param categorie categorie de jeu recherchee
 * @param editeur editeur recherche
 * @param rating classification age recherchee
 * @param plateforme plateforme recherchee
 * @param developpeur developpeur recherche
 * @param testDisponible presence souhaitee ou non d'un test
 */
public record CritereRechercheJeu(
        String texteLibre,
        String categorie,
        String editeur,
        String rating,
        String plateforme,
        String developpeur,
        Boolean testDisponible
) {
    /**
     * Indique si aucun filtre n'a ete renseigne.
     *
     * @return {@code true} si tous les criteres sont vides
     */
    public boolean estVide() {
        return estVide(texteLibre)
                && estVide(categorie)
                && estVide(editeur)
                && estVide(rating)
                && estVide(plateforme)
                && estVide(developpeur)
                && testDisponible == null;
    }

    private boolean estVide(String valeur) {
        return valeur == null || valeur.trim().isEmpty();
    }
}
