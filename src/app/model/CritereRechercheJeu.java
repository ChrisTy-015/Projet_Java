package app.model;

public record CritereRechercheJeu(
        String texteLibre,
        String categorie,
        String editeur,
        String rating,
        String plateforme,
        String developpeur,
        Boolean testDisponible
) {
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
