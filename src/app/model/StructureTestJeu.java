package app.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record StructureTestJeu(
        Map<String, Integer> notesParCategorie,
        List<String> pointsForts,
        List<String> pointsFaibles,
        String conditionsTest,
        List<String> jeuxSimilaires,
        Map<String, Integer> notesSpecifiquesAuGenre
) {
    public StructureTestJeu {
        notesParCategorie = copierNotes(notesParCategorie, "Les notes par categorie");
        if (notesParCategorie.isEmpty()) {
            throw new IllegalArgumentException("Un test structure doit contenir au moins une note par categorie");
        }
        pointsForts = copierListe(pointsForts, "Les points forts");
        pointsFaibles = copierListe(pointsFaibles, "Les points faibles");
        conditionsTest = conditionsTest == null ? "" : conditionsTest.trim();
        jeuxSimilaires = copierListe(jeuxSimilaires, "Les jeux similaires");
        notesSpecifiquesAuGenre = copierNotes(notesSpecifiquesAuGenre, "Les notes specifiques au genre");
    }

    private static Map<String, Integer> copierNotes(Map<String, Integer> source, String libelle) {
        Objects.requireNonNull(source, libelle + " ne peuvent pas etre null");
        Map<String, Integer> copie = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entree : source.entrySet()) {
            String categorie = Objects.requireNonNull(entree.getKey(), "La categorie ne peut pas etre null").trim();
            Integer note = Objects.requireNonNull(entree.getValue(), "La note ne peut pas etre null");
            if (categorie.isEmpty()) {
                throw new IllegalArgumentException("La categorie ne peut pas etre vide");
            }
            copie.put(categorie, note);
        }
        return Map.copyOf(copie);
    }

    private static List<String> copierListe(List<String> source, String libelle) {
        Objects.requireNonNull(source, libelle + " ne peuvent pas etre null");
        List<String> copie = new ArrayList<>();
        for (String valeur : source) {
            String texte = Objects.requireNonNull(valeur, "La valeur ne peut pas etre null").trim();
            if (texte.isEmpty()) {
                throw new IllegalArgumentException("La valeur ne peut pas etre vide");
            }
            copie.add(texte);
        }
        return List.copyOf(copie);
    }
}
