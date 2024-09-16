package fi.vm.sade.cas.oppija.surrogate;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

public class SurrogateData implements Serializable {

    public final SurrogateImpersonatorData impersonatorData;
    public final SurrogateRequestData requestData;

    public SurrogateData(SurrogateImpersonatorData impersonatorData, SurrogateRequestData requestData) {
        this.impersonatorData = requireNonNull(impersonatorData);
        this.requestData = requireNonNull(requestData);
    }

}
