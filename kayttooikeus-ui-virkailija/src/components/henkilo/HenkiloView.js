import './HenkiloView.css'
import React from 'react'
import Bacon from 'baconjs'
import HenkiloViewUserContent from './HenkiloViewUserContent'
import HenkiloViewContactContent from './HenkiloViewContactContent'
import HenkiloViewOrganisationContent from './HenkiloViewOrganisationContent'

import {l10nP, localeP} from '../../external/l10n'
import {henkiloNavi} from "../../external/navilists";
import {henkiloP, henkiloOrganisationsP, kayttajatietoP} from "../../external/henkiloClient";
import {koodistoKieliP, koodistoKansalaisuusP, koodistoSukupuoliP} from "../../external/koodistoClient";

const HenkiloView = React.createClass({
    render: function() {
        const henkiloResponse = this.props.henkilo;
        const henkiloOrgsResponse = this.props.henkiloOrgs;
        const L = this.props.l10n;
        const kayttajatietoResponse = this.props.kayttajatieto;
        const koodistoKieliResponse = this.props.koodistoKieli;
        const koodistoKansalaisuusResponse = this.props.koodistoKansalaisuus;
        const koodistoSukupuoliResponse = this.props.koodistoSukupuoli;
        return (
            <div>
                <div className="wrapper">
                    {
                        henkiloResponse.loaded && kayttajatietoResponse.loaded && koodistoSukupuoliResponse.loaded
                        && koodistoKieliResponse.loaded && koodistoKansalaisuusResponse.loaded
                            ? <HenkiloViewUserContent l10n={L} henkilo={henkiloResponse} kayttajatieto={kayttajatietoResponse}
                                                      readOnly={true}
                                                      koodistoKieli={koodistoKieliResponse} locale={this.props.locale}
                                                      koodistoKansalaisuus={koodistoKansalaisuusResponse}
                                                      koodistoSukupuoli={koodistoSukupuoliResponse} />
                            : L['LADATAAN']
                    }
                </div>
                <div className="wrapper">
                    {
                        henkiloResponse.loaded
                            ? <HenkiloViewContactContent l10n={L} henkilo={henkiloResponse} kayttajatieto={kayttajatietoResponse}
                                                      readOnly={true} koodistoKieli={koodistoKieliResponse}
                                                      locale={this.props.locale}
                                                      koodistoKansalaisuus={koodistoKansalaisuusResponse} />
                            : L['LADATAAN']
                    }
                </div>
                <div className="wrapper">
                    {
                        henkiloResponse.loaded && henkiloOrgsResponse.map(r => r.loaded)
                            ? <HenkiloViewOrganisationContent l10n={L}
                                                      organisations={henkiloOrgsResponse} readOnly={true}
                                                      henkilo={henkiloResponse} locale={this.props.locale} />
                            : L['LADATAAN']
                    }
                </div>
            </div>
        )
    },
});

export const henkiloViewContentP = Bacon.combineWith(l10nP, henkiloP, henkiloOrganisationsP, kayttajatietoP, koodistoKieliP,
    localeP, koodistoKansalaisuusP, koodistoSukupuoliP,
    (l10n, henkilo, henkiloOrgs, kayttajatieto, koodistoKieli, locale, koodistoKansalaisuus, koodistoSukupuoli) => {
    const props = {l10n, henkilo, henkiloOrgs, kayttajatieto, koodistoKieli, locale, koodistoKansalaisuus, koodistoSukupuoli};
    henkiloNavi.backLocation = '/henkilo';
    return {
        content: <HenkiloView {...props} />,
        navi: henkiloNavi,
        backgroundColor: "#f6f4f0"
    };
});

export default HenkiloView
