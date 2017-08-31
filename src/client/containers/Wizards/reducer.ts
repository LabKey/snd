import { combineReducers } from 'redux'

import { packages } from './Packages/reducer'
import { PackageWizardContainer } from './Packages/model'

export interface WizardReducerProps {
    packages: PackageWizardContainer
}

export const WizardsReducer = combineReducers<WizardReducerProps>({
    packages
});