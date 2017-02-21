import './HenkiloView.css'
import React from 'react'
import Bacon from 'baconjs'
import HenkiloViewUserContent from './HenkiloViewUserContent'

import {l10nP, localeP} from '../../external/l10n'
import {henkiloNavi} from "../../external/navilists";
import {henkiloP, henkiloOrganisationsP, kayttajatietoP} from "../../external/henkiloClient";
import {koodistoKieliP} from "../../external/koodistoClient";

const HenkiloView = React.createClass({
    render: function() {
        const henkiloResponse = this.props.henkilo;
        const henkiloOrgsResponse = this.props.henkiloOrgs;
        const L = this.props.l10n;
        const kayttajatietoResponse = this.props.kayttajatieto;
        const koodistoKieliResponse = this.props.koodistoKieli;
        return (
            <div>
                <div className="wrapper">
                    {
                        henkiloResponse.loaded && henkiloOrgsResponse.map(r => r.loaded) && kayttajatietoResponse.loaded
                        && koodistoKieliResponse.loaded
                            ? <HenkiloViewUserContent l10n={L} henkilo={henkiloResponse} kayttajatieto={kayttajatietoResponse}
                                                      organisations={henkiloOrgsResponse} readOnly={true}
                                                      koodistoKieli={koodistoKieliResponse} locale={this.props.locale} />
                            : L['LADATAAN']
                    }
                </div>
                <div className="wrapper">Another</div>
            </div>
        )
    },
});

export const henkiloViewContentP = Bacon.combineWith(l10nP, henkiloP, henkiloOrganisationsP, kayttajatietoP, koodistoKieliP,
    localeP,
    (l10n, henkilo, henkiloOrgs, kayttajatieto, koodistoKieli, locale) => {
    const props = {l10n, henkilo, henkiloOrgs, kayttajatieto, koodistoKieli, locale};
    henkiloNavi.backLocation = '/henkilo';
    return {
        content: <HenkiloView {...props} />,
        navi: henkiloNavi,
        backgroundColor: "#f6f4f0"
    };
});

export default HenkiloView
