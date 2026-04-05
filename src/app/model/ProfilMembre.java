package app.model;

/**
 * Enumeration des profils disponibles lors de l'inscription
 * ou de l'administration des comptes.
 */
public enum ProfilMembre {
    /**
     * Profil joueur standard.
     */
    JOUEUR("joueur"),
    /**
     * Profil pouvant publier des tests.
     */
    TESTEUR("testeur"),
    /**
     * Profil disposant des droits d'administration.
     */
    ADMINISTRATEUR("administrateur");

    private final String libelle;

    ProfilMembre(String libelle) {
        this.libelle = libelle;
    }

    /**
     * Retourne le libelle utilisateur du profil.
     *
     * @return libelle du profil
     */
    public String getLibelle() {
        return libelle;
    }
}
