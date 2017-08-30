/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { routerReducer } from 'react-router-redux';
import { combineReducers } from 'redux';
import { reducer as formReducer } from 'redux-form';

import { packages } from '../containers/Packages/reducer'
import { PackagesModel } from '../containers/Packages/model'


export interface APP_STATE_PROPS {
    packages: PackagesModel


    form: any
    router: any
}

export const reducers = combineReducers({
    packages,

    form: formReducer,
    router: routerReducer,
});



// packages
// categories
// app
// user
// wizards