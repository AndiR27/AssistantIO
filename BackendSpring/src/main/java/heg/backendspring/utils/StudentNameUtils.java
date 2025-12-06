package heg.backendspring.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@UtilityClass
@Slf4j
public class StudentNameUtils {

    /**
     * Vérifie si le nom d'un étudiant apparaît dans une des soumissions,
     * en normalisant tout (espaces, tirets, accents, casse, etc.).
     */
    public boolean matchesStudentNameInSubmissions(List<String> studentsSubmission, String studentName) {

        if (studentsSubmission == null || studentsSubmission.isEmpty() || studentName == null) {
            return false;
        }

        String normalizedStudent = normalizeName(studentName);
        if (normalizedStudent.isEmpty()) {
            return false;
        }

        for (String submission : studentsSubmission) {
            if (submission == null) {
                continue;
            }

            String normalizedSubmission = normalizeName(submission);

            if (normalizedSubmission.contains(normalizedStudent)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Normalise une chaîne :
     * - trim + toLowerCase
     * - supprime accents
     * - enlève espaces, tirets, underscores
     * - garde uniquement [a-z0-9]
     */
    public String normalizeName(String input) {
        if (input == null) {
            return "";
        }

        String cleaned = input.trim().toLowerCase(Locale.ROOT);

        // Remplacer multiples espaces par un seul
        cleaned = cleaned.replaceAll("\\s+", " ");

        // Supprimer les accents
        cleaned = Normalizer.normalize(cleaned, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        // Supprimer espaces, tirets, underscores
        cleaned = cleaned.replaceAll("[\\s_-]+", "");

        // Ne garder que lettres et chiffres
        cleaned = cleaned.replaceAll("[^a-z0-9]", "");

        return cleaned;
    }

    /**
     * Transforme un nom de dossier ZIP en un nom de dossier normalisé pour un étudiant.
     * Ex : "Ramiqi Andi_123456_assignment" -> "RamiqiAndi"
     * @param dirName
     * @return
     */
    public String toFolderNameFromZipDir(String dirName) {
        if (dirName == null) return "";

        // Prendre seulement ce qu’il y a avant "_"
        String[] parts = dirName.split("_", 2);
        String base = parts[0].trim();

        // Normalisation Unicode (accents -> ASCII)
        String normalized = Normalizer.normalize(base, Normalizer.Form.NFD);

        // Supprimer diacritiques (accents)
        normalized = normalized.replaceAll("\\p{M}", "");

        // Supprimer tout sauf les lettres (pas d'espaces, pas de tirets)
        normalized = normalized.replaceAll("[^\\p{L}]+", "");

        return normalized;
    }
}
