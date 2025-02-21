package org.acme.TestEntities;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.acme.entity.Cours;
import org.acme.repository.CoursRepository;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class TestEntitiesCreation {

    @Inject
    CoursRepository coursRepository;

    @Test
    @Transactional
    public void testCours(){
        Cours c = new Cours();


    }
}
