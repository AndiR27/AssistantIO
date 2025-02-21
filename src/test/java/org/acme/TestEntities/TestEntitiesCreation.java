package org.acme.TestEntities;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.acme.entity.*;
import org.acme.repository.*;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class TestEntitiesCreation {

    @Inject
    CoursRepository coursRepository;

    @Test
    @Transactional
    public void testCours(){
        Cours c = new Cours("Approfondissement de la programmation", "62-21",
                TypeSemestre.Printemps, 2025, TypeCours.Java);


    }
}
