package fi.vm.sade.kayttooikeus.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import fi.vm.sade.kayttooikeus.service.UnohtunutSalasanaService;
import io.swagger.annotations.ApiOperation;

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

    @PostMapping("/resetointi/{base64EncodedPoletti}")
    @ApiOperation(value = "Vaihtaa polettiin liitetyn henkilön salasanan")
    public void resetoiSalasana(@PathVariable String base64EncodedPoletti, @RequestBody String password) {
        unohtunutSalasanaService.resetoiSalasana(base64EncodedPoletti, password);
    }


}
