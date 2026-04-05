package app.model;

import java.util.Objects;

public class Membre {
    private final String pseudo;
    private boolean bloque;

    public Membre(String pseudo) {
        this.pseudo = Objects.requireNonNull(pseudo, "Le pseudo ne peut pas etre null").trim();
        if (this.pseudo.isEmpty()) {
            throw new IllegalArgumentException("Le pseudo ne peut pas etre vide");
        }
        this.bloque = false;
    }

    public String getPseudo() {
        return pseudo;
    }

    public boolean estBloque() {
        return bloque;
    }

    public void bloquer() {
        bloque = true;
    }

    public void debloquer() {
        bloque = false;
    }

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
