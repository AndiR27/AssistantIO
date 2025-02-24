package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.Rendu;
import org.acme.entity.TravailPratique;
import org.acme.repository.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.InputStream;

@ApplicationScoped
public class ServiceTravailPratique {

    @Inject
    TPRepository travailPratiqueRepository;

    @Inject
    @ConfigProperty(name = "zip-storage.path")
    String zipStoragePath;


    /**
     * Methode permettant d'ajouter un rendu au TP afin de pouvoir le stocker
     */

    @Transactional
    public void creerRenduTP(TravailPratique tp, InputStream zipFile){
        String cheminVersZip =
                zipStoragePath + tp.cours.code + "/" + "TP" + tp.no + "/" + nomFichier;
        Rendu rendu = new Rendu(nomFichier, cheminVersZip);
        tp.rendu = rendu;

        travailPratiqueRepository.persist(tp);
    }


}
