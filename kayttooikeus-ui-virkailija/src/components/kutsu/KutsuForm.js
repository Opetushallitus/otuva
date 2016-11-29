import React from "react";
import Bacon from "baconjs";
import R from 'ramda'
import Button from 'button'
import BasicInfo from "./BasicInfo";
import AddToOrganisation from "./AddToOrganisation";
import KutsuConfirmation from "./KutsuConfirmation";
import {organizationsFlatInHierarchyOrderP} from "../../external/organisations";
import {languagesP} from "../../external/languages";
import {omaOidP} from "../../external/omattiedot";
import {addedOrganizationsP} from "../../logic/organisations";
import {l10nP, localeP} from "../../external/l10n";
import {basicInfoP} from "../../logic/basicInfo";
import {navigateTo} from "../../logic/location";
import "./KutsuForm.css";

const KutsuForm = React.createClass({
    getInitialState: function () {
        return {
            confirmationModalOpen: false
        }
    },

    render: function () {
        const L = this.props.l10n;
        const uiLang = this.props.locale;
        const confirmationProps = {
            l10n: L,
            locale: uiLang,
            basicInfo: this.props.basicInfo,
            addedOrgs: this.props.addedOrgs,
            modalCloseFn: this.closeConfirmationModal,
            modalOpen: this.state.confirmationModalOpen,
            ready:(ok) => ok ? navigateTo('/kutsu/list', 'VIRKAILIJAN_LISAYS_LAHETETTY') : this.setState({
                confirmationModalOpen: false
            })
        };
        return (
            <form className="kutsuFormWrapper">

                <div className="kutsuFormHeader">
                    <h1>{L['VIRKAILIJAN_LISAYS_OTSIKKO']}</h1>
                </div>

                <BasicInfo l10n={L} locale={uiLang} basicInfo={this.props.basicInfo}
                           languages={this.props.languages}/>
                <AddToOrganisation l10n={L} uiLang={uiLang} omaOid={this.props.omaOid}
                                   orgs={this.props.organizationsFlatInHierarchyOrder} addedOrgs={this.props.addedOrgs}/>

                <div className="kutsuFormFooter row">
                    <Button className="action" action={this.openConfirmationModal} disabled={!this.isValid()}>
                        {L['VIRKAILIJAN_LISAYS_TALLENNA']}
                    </Button>
                </div>
                <KutsuConfirmation {...confirmationProps} />
            </form>
        )
    },

    isValid: function() {
        return this.isValidEmail(this.props.basicInfo.email)
            && this.props.addedOrgs.length > 0
            && R.all(org => org.id && org.selectedPermissions.length > 0)(this.props.addedOrgs);
    },
    
    isValidEmail: function(email) {
        return email != null && email.indexOf('@') > 2 && email.indexOf('@') < email.length-3
    },

    openConfirmationModal: function (e) {
        e.preventDefault();
        this.setState({
            confirmationModalOpen: true
        })
    },

    closeConfirmationModal: function (e) {
        e.preventDefault();
        this.setState({
            confirmationModalOpen: false
        })
    }
});

export const kutsuFormContentP = Bacon.combineWith(l10nP, localeP, organizationsFlatInHierarchyOrderP, addedOrganizationsP, basicInfoP, languagesP, omaOidP,
    (l10n, locale, organizationsFlatInHierarchyOrder, addedOrgs, basicInfo, languages, omaOid) => {
        const props = {l10n, locale, organizationsFlatInHierarchyOrder, addedOrgs, basicInfo, languages, omaOid};
        return {
            content: <KutsuForm {...props}/>
        };
    });

export default KutsuForm