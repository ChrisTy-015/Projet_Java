package app.model;

import java.util.Objects;

/**
 * Represente un membre de la plateforme.
 * <p>
 * Un membre est identifie par un pseudo unique et peut etre bloque
 * par un administrateur.
 */
public class Membre {
    private final String pseudo;
    private boolean bloque;

    /**
     * Cree un membre avec son pseudo.
     *
     * @param pseudo pseudo unique du membre
     */
    public Membre(String pseudo) {
        this.pseudo = Objects.requireNonNull(pseudo, "Le pseudo ne peut pas etre null").trim();
        if (this.pseudo.isEmpty()) {
            throw new IllegalArgumentException("Le pseudo ne peut pas etre vide");
        }
        this.bloque = false;
    }

    /**
     * Retourne le pseudo du membre.
     *
     * @return pseudo du membre
     */
    public String getPseudo() {
        return pseudo;
    }

    /**
     * Indique si le membre est bloque.
     *
     * @return {@code true} si le membre est bloque
     */
    public boolean estBloque() {
        return bloque;
    }

    /**
     * Bloque le membre.
     */
    public void bloquer() {
        bloque = true;
    }

    /**
     * Debloque le membre.
     */
    public void debloquer() {
        bloque = false;
    }

    /**
     * Retourne le type fonctionnel du membre.
     *
     * @return libelle du profil
     */
    public String getTypeProfil() {
        return "membre";
    }

    @Override
    public String toString() {
        return getTypeProfil() + "{" +
                "pseudo='" + pseudo + '\'' +
                ", bloque=" + bloque +
                '}';
    }
}
