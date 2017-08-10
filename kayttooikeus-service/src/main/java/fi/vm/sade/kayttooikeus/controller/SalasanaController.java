package fi.vm.sade.kayttooikeus.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import fi.vm.sade.kayttooikeus.service.UnohtunutSalasanaService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/salasana")
@RequiredArgsConstructor
public class SalasanaController {

    private final UnohtunutSalasanaService unohtunutSalasanaService;

    @PostMapping("/unohtunut/{kayttajatunnus}")
    @ApiOperation(value = "Lähettää henkilölle sähköpostilla linkin salasanan vaihtamiseen.")
    public void lahetaPoletti(@PathVariable String kayttajatunnus) {
        unohtunutSalasanaService.lahetaPoletti(kayttajatunnus);
    }

}
