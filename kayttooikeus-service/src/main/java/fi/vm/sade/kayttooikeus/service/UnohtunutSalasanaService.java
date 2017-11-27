package fi.vm.sade.kayttooikeus.service;

public interface UnohtunutSalasanaService {

    void lahetaPoletti(String kayttajatunnus);
    void resetoiSalasana(String base64EncodedPoletti, String password);

}
