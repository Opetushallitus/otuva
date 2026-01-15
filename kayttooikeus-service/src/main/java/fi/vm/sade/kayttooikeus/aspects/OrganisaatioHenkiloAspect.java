package fi.vm.sade.kayttooikeus.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OrganisaatioHenkiloAspect {

    private OrganisaatioHenkiloHelper organisaatioHenkiloHelper;

    public OrganisaatioHenkiloAspect(OrganisaatioHenkiloHelper helper) {
        organisaatioHenkiloHelper = helper;
    }

    @Around(value = "execution(public * fi.vm.sade.kayttooikeus.service.OrganisaatioHenkiloService.passivoiHenkiloOrganisation(..))" +
            "&& args(oidHenkilo, henkiloOrganisationOid)", argNames = "proceedingJoinPoint, oidHenkilo, henkiloOrganisationOid")
    private Object logPassivoiHenkiloOrganisaatio(ProceedingJoinPoint proceedingJoinPoint, String oidHenkilo, String henkiloOrganisationOid) throws Throwable {
        Object result = proceedingJoinPoint.proceed();
        organisaatioHenkiloHelper.logPassivoiOrganisaatioHenkilo(oidHenkilo, henkiloOrganisationOid, result);
        return result;
    }

}
