package app.model;

public enum ProfilMembre {
    JOUEUR("joueur"),
    TESTEUR("testeur"),
    ADMINISTRATEUR("administrateur");

    private final String libelle;

    ProfilMembre(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
